package lt.makerspace.jmatrix;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

import java.util.Collection;
import java.util.EnumSet;

public class SingleWidthCharacter extends TextCharacter {

    public static SingleWidthCharacter create(char c) {
        return new SingleWidthCharacter(c);
    }

    public static SingleWidthCharacter create(char c, TextColor foregroundColor, TextColor backgroundColor, SGR... modifiers) {
        return new SingleWidthCharacter(c, foregroundColor, backgroundColor, modifiers);
    }

    public SingleWidthCharacter(char character) {
        super(character);
    }

    public SingleWidthCharacter(TextCharacter character) {
        super(character);
    }

    public SingleWidthCharacter(char character, TextColor foregroundColor, TextColor backgroundColor, SGR... styles) {
        super(character, foregroundColor, backgroundColor, styles);
    }

    public SingleWidthCharacter(char character, TextColor foregroundColor, TextColor backgroundColor, EnumSet<SGR> modifiers) {
        super(character, foregroundColor, backgroundColor, modifiers);
    }

    @Override
    public boolean isDoubleWidth() {
        return false;
    }

    @Override
    public TextCharacter withCharacter(char character) {
        if (getCharacterString().equals(Character.toString(character))) {
            return this;
        }
        return new SingleWidthCharacter(character, getForegroundColor(), getBackgroundColor(), getModifiers());
    }

    @Override
    public TextCharacter withForegroundColor(TextColor foregroundColor) {
        if (this.getForegroundColor() == foregroundColor || this.getForegroundColor().equals(foregroundColor)) {
            return this;
        }
        return new SingleWidthCharacter(getCharacter(), foregroundColor, getBackgroundColor(), getModifiers());
    }

    @Override
    public TextCharacter withBackgroundColor(TextColor backgroundColor) {
        if (this.getBackgroundColor() == backgroundColor || this.getBackgroundColor().equals(backgroundColor)) {
            return this;
        }
        return new SingleWidthCharacter(getCharacter(), getForegroundColor(), backgroundColor, getModifiers());
    }

    @Override
    public TextCharacter withModifiers(Collection<SGR> modifiers) {
        EnumSet<SGR> newSet = EnumSet.copyOf(modifiers);
        if (modifiers.equals(newSet)) {
            return this;
        }
        return new SingleWidthCharacter(getCharacter(), getForegroundColor(), getBackgroundColor(), newSet);
    }

    @Override
    public TextCharacter withModifier(SGR modifier) {
        EnumSet<SGR> mods = getModifiers();
        if (mods.contains(modifier)) {
            return this;
        }
        EnumSet<SGR> newSet = mods;
        newSet.add(modifier);
        return new SingleWidthCharacter(getCharacter(), getForegroundColor(), getBackgroundColor(), newSet);
    }

    @Override
    public TextCharacter withoutModifier(SGR modifier) {
        EnumSet<SGR> mods = getModifiers();
        if (!mods.contains(modifier)) {
            return this;
        }
        EnumSet<SGR> newSet = mods;
        newSet.remove(modifier);
        return new SingleWidthCharacter(getCharacter(), getForegroundColor(), getBackgroundColor(), newSet);
    }

    @Override
    public char getCharacter() {
        return getCharacterString().charAt(0);
    }
}
