package lt.makerspace.jmatrix.util;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MatrixTerminal implements Terminal {

    private final Terminal boxed;

    @Getter
    @Setter
    private TerminalSize sizeOverride;

    public MatrixTerminal(Terminal boxed) {
        this.boxed = boxed;
    }

    @Override
    public void enterPrivateMode() throws IOException {
        boxed.enterPrivateMode();
    }

    @Override
    public void exitPrivateMode() throws IOException {
        boxed.exitPrivateMode();
    }

    @Override
    public void clearScreen() throws IOException {
        boxed.clearScreen();
    }

    @Override
    public void setCursorPosition(int x, int y) throws IOException {
        boxed.setCursorPosition(x, y);
    }

    @Override
    public void setCursorPosition(TerminalPosition position) throws IOException {
        boxed.setCursorPosition(position);
    }

    @Override
    public TerminalPosition getCursorPosition() throws IOException {
        return boxed.getCursorPosition();
    }

    @Override
    public void setCursorVisible(boolean visible) throws IOException {
        boxed.setCursorVisible(visible);
    }

    @Override
    public void putCharacter(char c) throws IOException {
        boxed.putCharacter(c);
    }

    @Override
    public void putString(String string) throws IOException {
        boxed.putString(string);
    }

    @Override
    public TextGraphics newTextGraphics() throws IOException {
        return boxed.newTextGraphics();
    }

    @Override
    public void enableSGR(SGR sgr) throws IOException {
        boxed.enableSGR(sgr);
    }

    @Override
    public void disableSGR(SGR sgr) throws IOException {
        boxed.disableSGR(sgr);
    }

    @Override
    public void resetColorAndSGR() throws IOException {
        boxed.resetColorAndSGR();
    }

    @Override
    public void setForegroundColor(TextColor color) throws IOException {
        boxed.setForegroundColor(color);
    }

    @Override
    public void setBackgroundColor(TextColor color) throws IOException {
        boxed.setBackgroundColor(color);
    }

    @Override
    public void addResizeListener(TerminalResizeListener listener) {
        boxed.addResizeListener(listener);
    }

    @Override
    public void removeResizeListener(TerminalResizeListener listener) {
        boxed.removeResizeListener(listener);
    }

    @Override
    public TerminalSize getTerminalSize() throws IOException {
        if (sizeOverride != null) {
            return sizeOverride;
        } else {
            return boxed.getTerminalSize();
        }
    }

    @Override
    public byte[] enquireTerminal(int timeout, TimeUnit timeoutUnit) throws IOException {
        return boxed.enquireTerminal(timeout, timeoutUnit);
    }

    @Override
    public void bell() throws IOException {
        boxed.bell();
    }

    @Override
    public void flush() throws IOException {
        boxed.flush();
    }

    @Override
    public void close() throws IOException {
        boxed.close();
    }

    @Override
    public KeyStroke pollInput() throws IOException {
        return boxed.pollInput();
    }

    @Override
    public KeyStroke readInput() throws IOException {
        return boxed.readInput();
    }
}
