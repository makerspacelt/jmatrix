package lt.makerspace.jmatrix.util;

import java.io.IOException;
import java.io.OutputStream;

public class SwitchedOutStream extends OutputStream {

    private final OutputStream first;
    private final OutputStream second;

    private OutputStream current;

    public SwitchedOutStream(OutputStream first, OutputStream second) {
        this.first = first;
        this.second = second;

        this.current = first;
    }

    public void first() {
        current = first;
    }

    public void second() {
        current = second;
    }

    @Override
    public void write(int b) throws IOException {
        current.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        current.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        current.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        current.flush();
    }

    @Override
    public void close() throws IOException {
        first.close();
        second.close();
    }
}
