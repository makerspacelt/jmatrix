package lt.makerspace.jmatrix;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.screen.Screen;
import lt.makerspace.jmatrix.SingleWidthCharacter.CharColor;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static java.lang.Math.abs;
import static lt.makerspace.jmatrix.Const.CHARS;
import static lt.makerspace.jmatrix.Const.EMPTY_CHAR;
import static lt.makerspace.jmatrix.SingleWidthCharacter.CharColor.WHITE;
import static lt.makerspace.jmatrix.SingleWidthCharacter.withAttributes;
import static lt.makerspace.jmatrix.SingleWidthCharacter.withColor;

public class Droplet {

    private static TextCharacter getNextChar(Random r) {
        return SingleWidthCharacter.getChar(CharColor.GREEN_1, false, CHARS[r.nextInt(CHARS.length)]);
    }

    private int x;
    private float y;
    private float velocity;
    private float yStep;

    private int length;

    private TextCharacter[] characters;

    private Consumer<Droplet> onExit;

    public Droplet(int x, int length, float velocity) {
        this.x = x;
        this.length = length;
        this.velocity = abs(velocity);
        this.yStep = 10 * this.velocity;

        this.characters = new TextCharacter[length];

        Random r = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            this.characters[i] = getNextChar(r);
        }
        characters[0] = withColor(characters[0], WHITE);
    }

    public void update(float dt, TerminalSize size) {
        int yOld = (int) y;
        y += yStep * dt;
        if (!((int) y > yOld)) {
            return;
        }

        for (int i = length - 1; i >= 1; i--) {
            characters[i] = characters[i - 1];
        }
        characters[1] = withAttributes(characters[1], CharColor.GREEN_1, false);
        characters[0] = withAttributes(getNextChar(ThreadLocalRandom.current()), WHITE, true);

        characters[length - 3] = withColor(characters[length - 3], CharColor.GREEN_2);
        characters[length - 2] = withColor(characters[length - 2], CharColor.GREEN_3);
        characters[length - 1] = withColor(characters[length - 1], CharColor.GREEN_4);

        if (y - length - 1 > size.getRows()) {
            onExit.accept(this);
        }
    }

    public void render(Screen screen) {
        int y = (int) this.y;
        for (int i = 0; i < length; i++) {
            screen.setCharacter(x, y - i, characters[i]);
        }
        screen.setCharacter(x, y - length, EMPTY_CHAR);
    }

    public Droplet onExit(Consumer<Droplet> onExit) {
        this.onExit = onExit;
        return this;
    }


}
