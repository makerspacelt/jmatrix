package lt.makerspace.jmatrix.textupdater;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileText implements TextUpdater {

    private static final char TOP = '▀';
    private static final char BOTTOM = '▄';
    private static final char BLACK = '█';
    private static final char WHITE = ' ';

    private final Path file;
    private final ImageSize size;
    private final ScheduledExecutorService ses;


    private final List<Consumer<String>> subscriptions = new ArrayList<>();
    private String currentValue = "Loading...";

    private ScheduledFuture<?> future;

    public FileText(Path file, ImageSize size, ScheduledExecutorService ses) throws IOException {
        this.file = file;
        this.size = size;
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
            switch (size) {
                case FULL -> drawFullSize(w, h, pixels, sb);
                case HALF -> drawHalfSize(w, h, pixels, sb);
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

    private static void drawFullSize(int w, int h, int[] pixels, StringBuilder sb) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean isBlack = isPixelBlack(x, y, w, pixels);
                if (isBlack) {
                    sb.append(BLACK).append(BLACK);
                } else {
                    sb.append(WHITE).append(WHITE);
                }
            }
            sb.append('\n');
        }
    }

    private static void drawHalfSize(int w, int h, int[] pixels, StringBuilder sb) {
        int hh = h / 2;
        for (int y = 0; y < hh; y++) {
            for (int x = 0; x < w; x++) {
                int y1 = y * 2;
                int y2 = y * 2 + 1;
                boolean topBlack = isPixelBlack(x, y1, w, pixels);
                boolean bottomBlack = isPixelBlack(x, y2, w, pixels);
                char pixel = '?';
                if (topBlack && bottomBlack) {
                    sb.append(BLACK);
                } else if (!topBlack && bottomBlack) {
                    sb.append(BOTTOM);
                } else if (topBlack && !bottomBlack) {
                    sb.append(TOP);
                } else {
                    sb.append(WHITE);
                }
            }
            sb.append('\n');
        }
        if (h % 2 != 0) {

        }
    }

    private static int getPixel(int x, int y, int w, int[] pixels) {
        return pixels[y * w + x];
    }


    private static boolean isPixelBlack(int x, int y, int w, int[] pixels) {
        return isPixelBlack(getPixel(x, y, w, pixels));
    }

    private static boolean isPixelBlack(int argb) {
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = (argb) & 0xFF;
        return r < 50 && g < 50 && b < 50;
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

    public enum ImageSize {

        FULL,
        HALF

    }
}
