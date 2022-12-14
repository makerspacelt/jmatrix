package lt.makerspace.jmatrix;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import org.eclipse.collections.api.map.primitive.CharIntMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.*;
import static java.util.Objects.requireNonNullElse;
import static lt.makerspace.jmatrix.Const.*;

public class TextDisplay {

    private static final float UPDATE_DT = 16f / 1000f;

    private boolean textDirty = true;
    private float currentUpdate = 0;

    private String text = "";
    private List<int[]> lines = new ArrayList<>();
    private List<int[]> drawnLines = new ArrayList<>();


    private int textChangeRateMultiplier = 1;


    private int borderWidth = 2;
    private int textWidth = 0;
    private int textHeight = 0;

    private int maxLineLength = 64;

    private TerminalSize terminalSize;

    public TextDisplay(String text) {
        setText(text);
    }


    public void setText(String text) {
        if (!this.text.equals(this.text = requireNonNullElse(text, ""))) {
            this.textDirty = true;
        }
    }

    public int getTextChangeRateMultiplier() {
        return textChangeRateMultiplier;
    }

    public void setTextChangeRateMultiplier(int textChangeRateMultiplier) {
        if (textChangeRateMultiplier < 1) {
            throw new IllegalArgumentException("Minimum allowed textChangeRateMultiplier: 1");
        }
        this.textChangeRateMultiplier = textChangeRateMultiplier;
    }

    private void updateText() {
        lines.clear();

        StringBuilder currentLineString = new StringBuilder(64);

        String[][] strings = Arrays.stream(text.split("\n"))
            .map(s -> s.split(" "))
            .toArray(String[][]::new);

        CharIntMap index = CHAR_INDEX;

        int maxLines = min(strings.length, terminalSize.getRows() - 10);

        for (int i = 0; i < maxLines; i++) {
            String[] line = strings[i];
            for (int j = 0; j < line.length; j++) {
                String word = line[j];
                if (currentLineString.length() > 0) {
                    currentLineString.append(' ');
                }
                currentLineString.append(word);
                if (currentLineString.length() > maxLineLength) {
                    int spaceIndex = currentLineString.lastIndexOf(" ");
                    if(spaceIndex > 0) {
                        currentLineString.setLength(spaceIndex);
                    }

                    lines.add(mapStringToChars(currentLineString, index));
                    currentLineString.setLength(0);
                    currentLineString.append(word);
                }
            }
            lines.add(mapStringToChars(currentLineString, index));
            currentLineString.setLength(0);
        }

        this.textHeight = lines.size();
        this.textWidth = lines.stream().mapToInt(a -> a.length).max().orElse(0);

        drawnLines.clear();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int maxIndex = RANDOM_ORDER_CHARS.length;
        for (var line : lines) {
            int[] randomized = new int[line.length];
            for (int i = 0; i < randomized.length; i++) {
                randomized[i] = r.nextInt(maxIndex);
            }
            drawnLines.add(randomized);
        }

        textDirty = false;
    }

    private static int[] mapStringToChars(StringBuilder currentLineString, CharIntMap index) {
        char[] currentLine = currentLineString.toString().toCharArray();
        int[] charIndexes = new int[currentLine.length];
        for (int c = 0; c < currentLine.length; c++) {
            char currentChar = currentLine[c];
            if (currentChar == ' ') {
                charIndexes[c] = -1;
            } else {
                charIndexes[c] = index.get(currentChar);
            }
        }
        return charIndexes;
    }

    public void update(float dt, TerminalSize size) {
        this.terminalSize = size;
        if (textDirty) {
            updateText();
        }

        if ((currentUpdate -= dt) < 0) {
            do {
                List<int[]> targetLines = lines;
                List<int[]> currentLines = drawnLines;

                int tc = textChangeRateMultiplier;
                for (int i = 0; i < targetLines.size(); i++) {
                    int[] target = targetLines.get(i);
                    int[] current = currentLines.get(i);
                    for (int j = 0; j < target.length; j++) {
                        int t = target[j];
                        int c = current[j];
                        if (t > c) {
                            current[j] = min(t, c + tc);
                        } else if (t < c) {
                            current[j] = max(t, c - tc);
                        }

                    }
                }

                ThreadLocalRandom r = ThreadLocalRandom.current();
                int chances = min(text.length() / 10, 1);
                for (int i = 0; i < chances; i++) {
                    if (r.nextDouble() > 0.99) {
                        int[] line = drawnLines.get(r.nextInt(drawnLines.size()));
                        int index = r.nextInt(line.length);
                        line[index] = r.nextInt(RANDOM_ORDER_CHARS.length);
                    }
                }

            } while ((currentUpdate += UPDATE_DT) < UPDATE_DT);
        }
    }

    public void render(Screen screen) {

        int textX = findTextX();
        int textY = findTextY();

        int boxX = textX - borderWidth;
        int boxY = textY - borderWidth / 2;
        int boxWidth = textWidth + borderWidth * 2;
        int boxHeight = textHeight + borderWidth;

        for (int x = 0; x < boxWidth; x++) {
            for (int y = 0; y < boxHeight; y++) {
                screen.setCharacter(boxX + x, boxY + y, EMPTY_CHAR);
            }
        }

        List<int[]> lines = drawnLines;
        List<int[]> targetLines = this.lines;
        char[] chars = RANDOM_ORDER_CHARS;
        for (int i = 0; i < lines.size(); i++) {
            int[] line = lines.get(i);
            for (int j = 0; j < line.length; j++) {
                int lineCharIndex = line[j];
                char c;
                if (lineCharIndex < 0) {
                    c = ' ';
                } else {
                    c = chars[lineCharIndex];
                }
                int distance = abs(lineCharIndex - targetLines.get(i)[j]);
                screen.setCharacter(textX + j, textY + i, getColorForDistance(c, distance));
            }
        }

    }

    private TextCharacter getColorForDistance(char c, int distanceFromTarget) {
        if (distanceFromTarget == 0) {
            return Characters.fromCharacter(c);
        }

        TextColor color;
        if (distanceFromTarget < 5) {
            color = WHITE;
        } else if (distanceFromTarget < 15) {
            color = GREEN_1;
        } else if (distanceFromTarget < 35) {
            color = GREEN_2;
        } else if (distanceFromTarget < 60) {
            color = GREEN_3;
        } else {
            color = GREEN_4;
        }
        return Characters.fromCharacter(c, color, null);
    }

    private int findTextX() {
        return (terminalSize.getColumns() - textWidth) / 2;
    }

    private int findTextY() {
        return (terminalSize.getRows() - textHeight) / 2;
    }
}
