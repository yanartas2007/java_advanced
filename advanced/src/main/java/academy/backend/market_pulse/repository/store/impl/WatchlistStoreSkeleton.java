package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.repository.store.WatchlistStore;

public class WatchlistStoreSkeleton implements WatchlistStore {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void save(Path path, Set<String> tickers) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(path.toFile(), new ArrayList<>(tickers));
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить watchlist в " + path, e);
        }
    }

    @Override
    public Set<String> load(Path path) {
        if (!Files.exists(path)) {
            return new LinkedHashSet<>();
        }
        try {
            List<String> list = OBJECT_MAPPER.readValue(
                    path.toFile(),
                    OBJECT_MAPPER.getTypeFactory()
                            .constructCollectionType(List.class, String.class));
            return new LinkedHashSet<>(list);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать watchlist из " + path, e);
        }
    }
}