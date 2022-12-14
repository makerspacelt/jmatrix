package lt.makerspace.jmatrix.textupdater;

import java.util.function.Consumer;

public interface TextUpdater extends AutoCloseable {

    String get();

    Subscription subscribe(Consumer<String> listener);

}
