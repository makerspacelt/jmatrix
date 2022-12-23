package lt.makerspace.jmatrix;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import org.eclipse.collections.api.map.primitive.ByteObjectMap;
import org.eclipse.collections.api.map.primitive.CharObjectMap;
import org.eclipse.collections.api.map.primitive.MutableByteObjectMap;
import org.eclipse.collections.api.map.primitive.MutableCharObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.ByteObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.CharObjectHashMap;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Objects;

public class SingleWidthCharacter extends TextCharacter {

    public enum CharColor {

        GREEN_1(Const.GREEN_1),
        GREEN_2(Const.GREEN_2),
        GREEN_3(Const.GREEN_3),
        GREEN_4(Const.GREEN_4),
        WHITE(Const.WHITE);
        public final TextColor color;

        CharColor(TextColor color) {
            this.color = color;
        }
    }

    private static final SGR[] sgrBold = {SGR.BOLD};
    private static final SGR[] sgrDefault = new SGR[0];

    private static final EnumMap<CharColor, ByteObjectMap<CharObjectMap<SingleWidthCharacter>>> CHAR_CACHE = new EnumMap<>(CharColor.class);

    static {
        for (CharColor color : CharColor.values()) {
            MutableByteObjectMap<CharObjectMap<SingleWidthCharacter>> byBoldMap = new ByteObjectHashMap<>();
            CHAR_CACHE.put(color, byBoldMap);
            for (byte b : new byte[]{0, 1}) {
                MutableCharObjectMap<SingleWidthCharacter> byCharMap = new CharObjectHashMap<>();
                byBoldMap.put(b, byCharMap);
                for (char c : Const.CHARS) {
                    byCharMap.put(c, create(c, color.color, DEFAULT_CHARACTER.getBackgroundColor(), b == 1 ? sgrBold : sgrDefault));
                }
            }
        }
    }

    public static SingleWidthCharacter getChar(char character) {
        return getChar(CharColor.WHITE, false, character);
    }

    public static SingleWidthCharacter getChar(CharColor color, boolean bold, char character) {
        SingleWidthCharacter c = CHAR_CACHE.get(color).get((byte) (bold ? 1 : 0)).get(character);
        return c != null ? c : create(character, color.color, null, bold ? sgrBold : sgrDefault);
    }

    public static SingleWidthCharacter withColor(TextCharacter c, CharColor color) {
        return getChar(color, c.isBold(), c.getCharacter());
    }

    public static SingleWidthCharacter withAttributes(TextCharacter c, CharColor color, boolean boldness) {
        return getChar(color, boldness, c.getCharacter());
    }

    private static SingleWidthCharacter create(char c, TextColor foregroundColor, TextColor backgroundColor, SGR... modifiers) {
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
