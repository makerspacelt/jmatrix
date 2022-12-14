package lt.makerspace.jmatrix.textupdater;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileText implements TextUpdater {

    private static final char BLACK = '@';
    private static final char WHITE = ' ';

    private final Path file;
    private final ScheduledExecutorService ses;


    private final List<Consumer<String>> subscriptions = new ArrayList<>();
    private String currentValue = "Loading...";

    private ScheduledFuture<?> future;

    public FileText(Path file, ScheduledExecutorService ses) throws IOException {
        this.file = file;
        this.ses = ses;
        updateText();

        try {

            WatchService ws = file.getFileSystem().newWatchService();
            WatchKey watchKey = file.toAbsolutePath().getParent().register(ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            future = ses.scheduleWithFixedDelay(() -> {
                try {
                    for (var event : watchKey.pollEvents()) {
                        if (event.context() instanceof Path p && Files.exists(p) && Files.isRegularFile(p)) {
                            updateText();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1, 1, TimeUnit.SECONDS);

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void updateText() throws IOException {
        byte[] fileBytes = readFile();
        String newString = stringifyFile(fileBytes);
        if (!this.currentValue.equals(this.currentValue = newString)) {
            subscriptions.forEach(s -> s.accept(newString));
        }
    }

    private byte[] readFile() throws IOException {
        return Files.readAllBytes(file);
    }

    private String stringifyFile(byte[] file) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file));
            int w = image.getWidth();
            int h = image.getHeight();

            int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);

            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = pixels[y * w + x];
                    int r = (argb >>> 16) & 0xFF;
                    int g = (argb >>> 8) & 0xFF;
                    int b = (argb >>> 0) & 0xFF;
                    if (r < 50 && g < 50 && b < 50) {
                        sb.append(BLACK);
                    } else {
                        sb.append(WHITE);
                    }
                }
                sb.append('\n');
            }
            return sb.toString();

        } catch (IOException | NullPointerException e) {
            try {
                return new String(file);
            } catch (Exception e2) {
                e.addSuppressed(e2);
                e.printStackTrace();
                return e2.getMessage() != null ? e2.getMessage() : e2.getClass().getSimpleName();
            }
        }
    }

    @Override
    public String get() {
        return currentValue;
    }

    @Override
    public Subscription subscribe(Consumer<String> listener) {
        listener.accept(get());
        subscriptions.add(listener);
        return () -> subscriptions.remove(listener);
    }

    @Override
    public void close() throws Exception {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }
}
