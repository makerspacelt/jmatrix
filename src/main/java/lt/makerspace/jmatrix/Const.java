package lt.makerspace.jmatrix;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import org.eclipse.collections.api.factory.primitive.CharIntMaps;
import org.eclipse.collections.api.map.primitive.CharIntMap;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class Const {

    public static final int REFERENCE_TERMINAL_WIDTH = 200;

    public static char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123457890ąčęėįšųūžĄČĘĖĮŠŲŪŽ/*-+`~!@#$%^&()_=[]{}\\|'\"<,>./?:".toCharArray();
    public static char[] RANDOM_ORDER_CHARS = Arrays.copyOf(CHARS, CHARS.length);
    public static final CharIntMap CHAR_INDEX;

    static {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        char[] a = RANDOM_ORDER_CHARS;
        for (int i = 0; i < a.length; i++) {
            int randomIndexToSwap = r.nextInt(a.length);
            char temp = a[randomIndexToSwap];
            a[randomIndexToSwap] = a[i];
            a[i] = temp;
        }

        List<Integer> ints = IntStream
            .range(0, RANDOM_ORDER_CHARS.length)
            .boxed()
            .toList();


        CHAR_INDEX = CharIntMaps.immutable.from(ints, i -> RANDOM_ORDER_CHARS[i], i -> i);
    }

    public static final TextCharacter EMPTY_CHAR = Characters.fromCharacter(' ');

    static final float NANOS_IN_SECOND = (float) 1e9;


    public static SGR[] SGR_BOLD = {SGR.BOLD};
    public static SGR[] SGR_NONE = {

    };

    public static final TextColor WHITE = TextColor.ANSI.WHITE;

    public static TextColor GREEN_1 = new TextColor.RGB(0, 255, 0);
    public static TextColor GREEN_2 = new TextColor.RGB(0, 150, 0);
    public static TextColor GREEN_3 = new TextColor.RGB(0, 100, 0);
    public static TextColor GREEN_4 = new TextColor.RGB(0, 50, 0);
}
