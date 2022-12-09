package lt.makerspace.jmatrix;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

public class Characters {

    public static TextCharacter fromCharacter(char c) {
//        return TextCharacter.fromString(Character.toString(c))[0];
        return new SingleWidthCharacter(c);
    }

    public static TextCharacter fromCharacter(char c, TextColor foregroundColor, TextColor backgroundColor, SGR... modifiers) {
//        return TextCharacter.fromString(Character.toString(c), foregroundColor, backgroundColor, modifiers)[0];
        return new SingleWidthCharacter(c, foregroundColor, backgroundColor, modifiers);
    }

}
