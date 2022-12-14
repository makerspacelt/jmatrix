package lt.makerspace.jmatrix;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import lt.makerspace.jmatrix.textupdater.LocalDateTimeText;
import lt.makerspace.jmatrix.textupdater.Subscription;
import lt.makerspace.jmatrix.textupdater.TextUpdater;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

public class Matrix {

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

    private class MatrixRenderer implements Runnable {
        private TerminalSize terminalSize;

        private Subscription textSubscription;

        @Override

        public void run() {
            List<Droplet> droplets = new ArrayList<>(1000);
            Set<Droplet> absolete = Collections.newSetFromMap(new IdentityHashMap<>());

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


            try (var t = new DefaultTerminalFactory().createTerminal()) {
                terminalSize = t.getTerminalSize();

                Screen screen = new TerminalScreen(t);
                screen.startScreen();

                float targetDt = 1f / 60 / (float) t.getTerminalSize().getColumns() * (float) Const.REFERENCE_TERMINAL_WIDTH;

                float dt = 1f / 60;
                float generator = 0;

                while (running) {
                    ThreadLocalRandom r = ThreadLocalRandom.current();

                    generator += dt;

                    while (generator > 0) {
                        generator -= targetDt;
                        if (r.nextDouble() > 0.01) {
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

                    screen.refresh();

                    long end = System.nanoTime();

                    if (showUpdateTime) {
                        dt = (end - start) / Const.NANOS_IN_SECOND;
                        char[] chars = String.valueOf(dt * 1000).toCharArray();
                        for (int i = 0; i < chars.length; i++) {
                            screen.setCharacter(i, 0, Characters.fromCharacter(chars[i]));
                        }
                        screen.refresh();
                    }

                    long nanosToSleep = (16_666_666L - (end - start));
                    long millisToSleep = nanosToSleep / 1_000_000;
                    nanosToSleep -= millisToSleep * 1_000_000;
                    if (millisToSleep >= 0 && (nanosToSleep >= 0 && nanosToSleep <= 999999)) {
                        Thread.sleep(millisToSleep, (int) nanosToSleep);
                    }

                    end = System.nanoTime();
                    dt = (end - start) / Const.NANOS_IN_SECOND;
                }

                screen.stopScreen();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                exited = true;
                if (textSubscription != null) {
                    textSubscription.unsubscribe();
                }
            }
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
