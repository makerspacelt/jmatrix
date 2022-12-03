package lt.makerspace.jmatrix;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.screen.Screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.max;

public class TextDisplay {

    private boolean textDirty = true;

    private String text;
    private List<String> lines = new ArrayList<>();

    private int borderWidth = 2;
    private int textWidth = 0;
    private int textHeight = 0;

    private int maxLineLength = 64;

    private TerminalSize terminalSize;

    public TextDisplay(String text) {
        setText(Objects.requireNonNullElse(text, ""));
    }


    public void setText(String text) {
        this.text = text;
        this.textDirty = true;
    }

    private void updateText() {
        lines.clear();

        StringBuilder currentLineString = new StringBuilder(64);

        String[][] strings = Arrays.stream(text.split("\n"))
            .map(s -> s.split(" "))
            .toArray(String[][]::new);

        for (int i = 0; i < strings.length; i++) {
            String[] line = strings[i];
            for (int j = 0; j < line.length; j++) {
                String word = line[j];
                if (currentLineString.length() > 0) {
                    currentLineString.append(' ');
                }
                currentLineString.append(word);
                if (currentLineString.length() > maxLineLength) {
                    currentLineString.setLength(currentLineString.lastIndexOf(" "));
                    lines.add(currentLineString.toString());
                    currentLineString.setLength(0);
                    currentLineString.append(word);
                }
            }
            lines.add(currentLineString.toString());
            currentLineString.setLength(0);
        }

        this.textHeight = lines.size();
        this.textWidth = lines.stream().mapToInt(String::length).max().orElse(0);

        textDirty = false;
    }

    public void update(float dt, TerminalSize size) {
        this.terminalSize = size;
        if (textDirty) {
            updateText();
        }
    }

    public void render(Screen screen) {

        int textX = findTextX();
        int textY = findTextY();

        int boxX = textX - borderWidth;
        int boxY = textY - borderWidth;
        int boxWidth = textWidth + borderWidth * 2;
        int boxHeight = textHeight + borderWidth * 2;

        for (int x = 0; x < boxWidth; x++) {
            for (int y = 0; y < boxHeight; y++) {
                screen.setCharacter(boxX + x, boxY + y, Main.EMPTY_CHAR);
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (int j = 0; j < line.length(); j++) {
                screen.setCharacter(textX + j, textY + i, TextCharacter.fromCharacter(line.charAt(j))[0]);
            }
        }

    }

    private int findTextX() {
        return (terminalSize.getColumns() - textWidth) / 2;
    }

    private int findTextY() {
        return (terminalSize.getRows() - textHeight) / 2;
    }
}
