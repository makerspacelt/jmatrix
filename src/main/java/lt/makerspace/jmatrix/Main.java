package lt.makerspace.jmatrix;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345789/*-+`~!@#$%^&()_=[]{}\\|'\"<,>./?".toCharArray();

    public static char[] RANDOM_ORDER_CHARTS = Arrays.copyOf(CHARS, CHARS.length);

    static {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        char[] a = RANDOM_ORDER_CHARTS;
        for (int i = 0; i < a.length; i++) {
            int randomIndexToSwap = r.nextInt(a.length);
            char temp = a[randomIndexToSwap];
            a[randomIndexToSwap] = a[i];
            a[i] = temp;
        }
    }

    public static final TextCharacter EMPTY_CHAR = TextCharacter.fromCharacter(' ')[0];

    private static final float NANOS_IN_SECOND = (float) 1e9;
    static TerminalSize terminalSize;

    public static void main(String[] args) throws Exception {

        List<Droplet> droplets = new ArrayList<>(1000);
        Set<Droplet> absolete = Collections.newSetFromMap(new IdentityHashMap<>());

        TextDisplay textDisplay = new TextDisplay("Hello Matrix!");
        TextFetcher textFetcher = new TextFetcher(Executors.newSingleThreadScheduledExecutor(), textDisplay::setText);

        try (var t = new DefaultTerminalFactory().createTerminal()) {
            terminalSize = t.getTerminalSize();

            Screen screen = new TerminalScreen(t);
            screen.startScreen();

            float targetDt = 1f / 60;

            float dt = 1f / 60;
            while (true) {
                ThreadLocalRandom r = ThreadLocalRandom.current();
//                if (r.nextDouble() > 0.983333333) {

                float generator = dt;
                while (generator > targetDt) {
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
//                screen.clear();

                for (var droplet : droplets) {
                    droplet.update(dt, terminalSize);
                    droplet.render(screen);
                }

                droplets.removeIf(absolete::contains);
                absolete.clear();

                textDisplay.update(dt, terminalSize);
                textDisplay.render(screen);

                KeyStroke keyStroke = screen.pollInput();
                if (keyStroke != null) {
                    if (keyStroke.getCharacter() != null && (keyStroke.getCharacter() == 'c' || keyStroke.getCharacter() == 'C') && keyStroke.isCtrlDown()) {
                        break;
                    }
                }

                screen.refresh();

                long end = System.nanoTime();

                dt = (end - start) / NANOS_IN_SECOND;
                char[] chars = String.valueOf(dt * 1000).toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    screen.setCharacter(i, 0, TextCharacter.fromCharacter(chars[i])[0]);
                }
                screen.refresh();

                long nanosToSleep = (16_666_666L - (end - start));
                long millisToSleep = nanosToSleep / 1_000_000;
                nanosToSleep -= millisToSleep * 1_000_000;
                if (millisToSleep >= 0 && (nanosToSleep >= 0 && nanosToSleep <= 999999)) {
                    Thread.sleep(millisToSleep, (int) nanosToSleep);
                }

                end = System.nanoTime();
                dt = (end - start) / NANOS_IN_SECOND;
            }

            screen.stopScreen();
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
