package lt.makerspace.jmatrix;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.Screen.RefreshType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import lombok.Getter;
import lombok.Setter;
import lt.makerspace.jmatrix.textupdater.LocalDateTimeText;
import lt.makerspace.jmatrix.textupdater.Subscription;
import lt.makerspace.jmatrix.textupdater.TextUpdater;
import lt.makerspace.jmatrix.util.MatrixTerminal;
import lt.makerspace.jmatrix.util.SwitchedOutStream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Executor;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.rightPad;

public class Matrix {

    @Getter
    @Setter
    private int softCap = 500;
    @Getter
    @Setter
    private int hardCap = 2000;

    @Getter
    @Setter
    private TerminalSize sizeOverride;


    private boolean running = true;

    @Getter
    private boolean exited = false;

    @Getter
    @Setter
    private boolean showUpdateTime = false;

    private final Executor exec;
    private final List<TextUpdater> textUpdaters;

    private final InputStream ttyIn;
    private final OutputStream ttyOut;

    public Matrix(Executor exec, List<TextUpdater> textUpdaters, InputStream ttyIn, OutputStream ttyOut) {
        this.exec = exec;
        this.textUpdaters = new ArrayList<>(textUpdaters);

        this.ttyIn = ttyIn;
        this.ttyOut = ttyOut;
    }

    public void start() {
        exec.execute(new MatrixRenderer());
    }

    public void stop() {
        running = false;
    }

    private class MatrixRenderer implements Runnable {
        private TerminalSize terminalSize;

        private final List<Subscription> textSubscriptions = new ArrayList<>();

        private final Random r = new Random();

        private final List<Droplet> droplets = new ArrayList<>(1000);
        private final Set<Droplet> absolete = Collections.newSetFromMap(new IdentityHashMap<>());

        @Override

        public void run() {

            TextDisplay[] textDisplays = textUpdaters.stream()
                .map(textUpdater -> {
                    var td = new TextDisplay("Working");
                    if (textUpdater instanceof LocalDateTimeText) {
                        td.setTextChangeRateMultiplier(10);
                    }
                    textSubscriptions.add(textUpdater.subscribe(td::setText));
                    return td;
                })
                .toArray(TextDisplay[]::new);

            BufferedOutputStream bOut = new BufferedOutputStream(ttyOut, 1024 * 1024);

            SwitchedOutStream sOut = new SwitchedOutStream(ttyOut, bOut);

            DefaultTerminalFactory factory = new DefaultTerminalFactory(
                sOut,
                ttyIn,
                Charset.defaultCharset()
            );
//            factory.setInputTimeout(100);

            try (MatrixTerminal t = new MatrixTerminal(factory.createHeadlessTerminal())) {
                if (sizeOverride != null) {
                    t.setSizeOverride(sizeOverride);
                }


                terminalSize = t.getTerminalSize();

                Screen screen = new TerminalScreen(t, SingleWidthCharacter.getChar(' '));
                screen.startScreen();

                float targetDt = 1f / 60 / (float) t.getTerminalSize().getColumns() * (float) Const.REFERENCE_TERMINAL_WIDTH;

                float dt = 1f / 60;
                float generator = 0;

                sOut.second();

                while (running) {
                    try {
                        generator = spawnDroplets(generator, dt, targetDt);

                        long start = System.nanoTime();

                        for (var droplet : droplets) {
                            droplet.update(dt, terminalSize);
                            droplet.render(screen);
                        }

                        droplets.removeIf(absolete::contains);
                        absolete.clear();

                        if (textDisplays.length > 1) {
                            int h0 = textDisplays[0].getTextHeight();
                            int h1 = textDisplays[1].getTextHeight();
                            int totalHeight = h0 + h1;

                            int o0 = -h1 / 2 - 1;
                            int o1 = h0 / 2 + 1;


                            textDisplays[0].setYShift(o0);
                            textDisplays[1].setYShift(o1);
                        }
                        for (var td : textDisplays) {
                            td.update(dt, terminalSize);
                            td.render(screen);
                        }

                        KeyStroke keyStroke = screen.pollInput();
                        if (keyStroke != null) {
                            if (keyStroke.getCharacter() != null && (keyStroke.getCharacter() == 'c' || keyStroke.getCharacter() == 'C') && keyStroke.isCtrlDown()) {
                                break;
                            }
                        }

                        screen.refresh(RefreshType.DELTA);
                        bOut.flush();

                        long end = System.nanoTime();

                        if (showUpdateTime) {
                            showUpdateRate(start, end);
                        }

                        long nanosToSleep = (16_666_666L - (end - start));
                        long millisToSleep = nanosToSleep / 1_000_000;
                        nanosToSleep -= millisToSleep * 1_000_000;
                        if (millisToSleep >= 0 && (nanosToSleep >= 0 && nanosToSleep <= 999999)) {
                            Thread.sleep(millisToSleep, (int) nanosToSleep);
                        }

                        end = System.nanoTime();
                        dt = (end - start) / Const.NANOS_IN_SECOND;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                screen.stopScreen();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                exited = true;
                textSubscriptions.forEach(Subscription::unsubscribe);
            }
        }

        private float spawnDroplets(float generator, float dt, float targetDt) {
            generator += dt;

            int dropletCount = droplets.size();
            if (dropletCount < hardCap) {
                while (generator > 0) {
                    generator -= targetDt;

                    int count = droplets.size();
                    float odds = 0.99f;
                    if (count > softCap) {
                        odds -= 0.99f * (count - softCap) / (hardCap - softCap);
                    }

                    if (r.nextDouble() < odds) {
                        droplets.add(
                            new Droplet(
                                r.nextInt(terminalSize.getColumns()),
                                r.nextInt(5, 15),
                                getRandomizedVelocity(r)
                            )
                                .onExit(absolete::add)
                        );
                    }
                }
            } else {
                generator = 0;
            }

            return generator;
        }

        private void showUpdateRate(long start, long end) {
            float dt = (end - start) / Const.NANOS_IN_SECOND;
//                            char[] chars = valueOf(dt * 1000).toCharArray();
//                            for (int i = 0; i < chars.length; i++) {
//                                screen.setCharacter(i, 0, Characters.fromCharacter(chars[i]));
//                            }
//                            screen.refresh();
//                            bos.flush();

            String statusString = rightPad(valueOf(dt * 1000), 10)
                + " "
                + rightPad(valueOf(droplets.size()), 10);

            System.err.println(statusString);
        }
    }

    private static float getRandomizedVelocity(Random r) {
        float velocity = (float) r.nextGaussian();
        velocity += 1f;
        if (velocity < 0.3f) {
            velocity = 0.3f;
        } else if (velocity > 2f) {
            velocity = 2f;
        }

        return velocity;
    }

}
