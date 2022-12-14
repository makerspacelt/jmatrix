package lt.makerspace.jmatrix.textupdater;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LocalDateTimeText implements TextUpdater {


    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private final ScheduledExecutorService ses;

    private final List<Consumer<String>> subscribers = new ArrayList<>();

    private String text = LocalDateTime.now().format(FORMATTER);

    private ScheduledFuture<?> future;

    public LocalDateTimeText(ScheduledExecutorService ses) {
        this.ses = ses;

        future = ses.scheduleAtFixedRate(() -> {
            text = LocalDateTime.now().format(FORMATTER);
            subscribers.forEach(s -> s.accept(text));
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public String get() {
        return text;
    }

    @Override
    public Subscription subscribe(Consumer<String> listener) {
        listener.accept(get());
        subscribers.add(listener);
        return () -> subscribers.remove(listener);
    }

    @Override
    public void close() throws Exception {
        if (future != null) {
            future.cancel(true);
        }
    }
}
