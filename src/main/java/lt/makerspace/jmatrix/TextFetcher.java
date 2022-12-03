package lt.makerspace.jmatrix;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TextFetcher implements AutoCloseable {

    private ScheduledExecutorService scheduler;
    private Consumer<String> text;

    private ScheduledFuture<?> future;

    public TextFetcher(ScheduledExecutorService scheduler, Consumer<String> textConsumer) {
        this.scheduler = scheduler;
        this.text = textConsumer;

        HttpClient httpClient = HttpClient.newHttpClient();

        scheduler.scheduleAtFixedRate(() -> {
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
                textConsumer.accept(text);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws Exception {
        future.cancel(true);
    }
}
