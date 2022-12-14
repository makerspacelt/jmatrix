package lt.makerspace.jmatrix.textupdater;

import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ChuckNorrisText implements TextUpdater {

    private String currentText = "Chuck Norris is behind you.";

    private final List<Consumer<String>> subscriptions = new ArrayList<>();

    private final ScheduledExecutorService scheduler;

    private ScheduledFuture<?> future;

    public ChuckNorrisText(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;

        HttpClient httpClient = HttpClient.newHttpClient();

        future = scheduler.scheduleAtFixedRate(() -> {
            try {
                HttpRequest rq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.chucknorris.io/jokes/random"))
                    .GET()
                    .build();

                String text = JsonParser.parseString(httpClient.send(rq, HttpResponse.BodyHandlers.ofString())
                        .body())
                    .getAsJsonObject()
                    .get("value")
                    .getAsString();

                subscriptions.forEach(s -> s.accept(text));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 5, 60, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws Exception {
        future.cancel(true);
    }

    @Override
    public String get() {
        return Objects.requireNonNullElse(currentText, "");
    }

    @Override
    public Subscription subscribe(Consumer<String> listener) {
        listener.accept(get());
        subscriptions.add(listener);
        return () -> subscriptions.remove(listener);
    }
}
