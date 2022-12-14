package lt.makerspace.jmatrix.textupdater;

import java.util.function.Consumer;

public class ConstantText implements TextUpdater {

    private final String text;

    public ConstantText(String text) {
        this.text = text;
    }

    @Override
    public String get() {
        return text;
    }

    @Override
    public Subscription subscribe(Consumer<String> listener) {
        listener.accept(text);
        return () -> {
        };
    }

    @Override
    public void close() {

    }
}
