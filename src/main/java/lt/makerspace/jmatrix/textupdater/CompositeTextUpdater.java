package lt.makerspace.jmatrix.textupdater;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CompositeTextUpdater implements TextUpdater {

    private final List<TextUpdater> delegates;

    private final String[] strings;
    private final List<Subscription> subscriptions;

    private String fullString;


    @Getter
    @Setter
    private int spacing = 1;


    private final List<Consumer<String>> subscribers = new ArrayList<>();

    public CompositeTextUpdater(List<TextUpdater> delegates) {
        this.delegates = new ArrayList<>(delegates);
        this.strings = new String[delegates.size()];
        this.subscriptions = new ArrayList<>(delegates.size());

        for (int i = 0; i < delegates.size(); i++) {
            int fi = i;
            TextUpdater delegate = delegates.get(i);
            delegate.subscribe(str -> {
                strings[fi] = str;
                updateFullString();
            });
        }
    }

    private void updateFullString() {
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str);
            for (int i = 0; i < spacing + 1; i++) {
                sb.append('\n');
            }
        }
        sb.setLength(sb.length() - spacing - 1);
        fullString = sb.toString();

        subscribers.forEach(s -> s.accept(fullString));
    }

    @Override
    public String get() {
        return fullString;
    }

    @Override
    public Subscription subscribe(Consumer<String> listener) {
        listener.accept(get());
        subscribers.add(listener);
        return () -> subscribers.remove(listener);
    }

    @Override
    public void close() throws Exception {
        subscriptions.forEach(Subscription::unsubscribe);
        subscriptions.clear();
        subscribers.clear();
    }
}
