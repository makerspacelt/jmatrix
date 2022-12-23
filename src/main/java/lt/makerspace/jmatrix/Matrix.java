package lt.makerspace.jmatrix;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.Screen.RefreshType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import lt.makerspace.jmatrix.textupdater.LocalDateTimeText;
import lt.makerspace.jmatrix.textupdater.Subscription;
import lt.makerspace.jmatrix.textupdater.TextUpdater;
import lt.makerspace.jmatrix.util.SwitchedOutStream;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedOutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.rightPad;

public class Matrix {

    private int softCap = 500;
    private int hardCap = 2000;

    private boolean running = true;
    private boolean exited = false;

    private boolean showUpdateTime = false;

    private final Executor exec;
    private final TextUpdater textUpdater;

    public Matrix(Executor exec, TextUpdater textUpdater) {
        this.exec = exec;
        this.textUpdater = textUpdater;
    }

    public void start() {
        exec.execute(new MatrixRenderer());
    }

    public void stop() {
        running = false;
    }

    public boolean isExited() {
        return exited;
    }

    public boolean isShowUpdateTime() {
        return showUpdateTime;
    }

    public void setShowUpdateTime(boolean showUpdateTime) {
        this.showUpdateTime = showUpdateTime;
    }

    public int getSoftCap() {
        return softCap;
    }

    public void setSoftCap(int softCap) {
        this.softCap = softCap;
    }

    public int getHardCap() {
        return hardCap;
    }

    public void setHardCap(int hardCap) {
        this.hardCap = hardCap;
    }

    private class MatrixRenderer implements Runnable {
        private TerminalSize terminalSize;

        private Subscription textSubscription;

        private final Random r = new Random();

        private final List<Droplet> droplets = new ArrayList<>(1000);
        private final Set<Droplet> absolete = Collections.newSetFromMap(new IdentityHashMap<>());

        @Override

        public void run() {

            TextDisplay textDisplay;
            if (textUpdater != null) {
                textDisplay = new TextDisplay("Hello Matrix!");
                if (textUpdater instanceof LocalDateTimeText) {
                    textDisplay.setTextChangeRateMultiplier(10);
                }
                textSubscription = textUpdater.subscribe(textDisplay::setText);
            } else {
                textDisplay = null;
            }


            BufferedOutputStream bos = new BufferedOutputStream(System.out, 1024 * 1024);

            SwitchedOutStream sos = new SwitchedOutStream(System.out, bos);

            try (var t = new DefaultTerminalFactory(
                sos,
                System.in,
                Charset.defaultCharset()
            ).createTerminal()) {
                terminalSize = t.getTerminalSize();

                Screen screen = new TerminalScreen(t, SingleWidthCharacter.getChar(' '));
                screen.startScreen();

                float targetDt = 1f / 60 / (float) t.getTerminalSize().getColumns() * (float) Const.REFERENCE_TERMINAL_WIDTH;

                float dt = 1f / 60;
                float generator = 0;

                sos.second();

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

                        if (textDisplay != null) {
                            textDisplay.update(dt, terminalSize);
                            textDisplay.render(screen);
                        }

                        KeyStroke keyStroke = screen.pollInput();
                        if (keyStroke != null) {
                            if (keyStroke.getCharacter() != null && (keyStroke.getCharacter() == 'c' || keyStroke.getCharacter() == 'C') && keyStroke.isCtrlDown()) {
                                break;
                            }
                        }

                        screen.refresh(RefreshType.DELTA);
                        bos.flush();

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
                if (textSubscription != null) {
                    textSubscription.unsubscribe();
                }
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
