package lt.makerspace.jmatrix;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.screen.Screen;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static java.lang.Math.abs;
import static lt.makerspace.jmatrix.Const.*;
import static lt.makerspace.jmatrix.Const.GREEN_2;
import static lt.makerspace.jmatrix.Const.WHITE;

public class Droplet {

    private static TextCharacter getNextChar(Random r) {
        return Characters.fromCharacter(CHARS[r.nextInt(CHARS.length)], GREEN_1, ANSI.DEFAULT);
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
        characters[0] = characters[0].withForegroundColor(WHITE);
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
        characters[1] = characters[1]
            .withForegroundColor(GREEN_1)
            .withoutModifier(SGR.BOLD);
        characters[0] = getNextChar(ThreadLocalRandom.current())
            .withForegroundColor(WHITE)
            .withModifier(SGR.BOLD);

        characters[length - 3] = characters[length - 3].withForegroundColor(GREEN_2);
        characters[length - 2] = characters[length - 2].withForegroundColor(GREEN_3);
        characters[length - 1] = characters[length - 1].withForegroundColor(GREEN_4);

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
