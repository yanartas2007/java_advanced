package academy.backend.market_pulse.history.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;

public class PriceHistoryStoreSkeleton implements PriceHistoryStore {

    private static final String DELIMITER = ";";
    private static final String EXTENSION = ".txt";

    @Override
    public Path append(Path directory, PricePoint point) {
        try {
            Files.createDirectories(directory);
            String content = point.ticker() + DELIMITER + point.timestamp() + DELIMITER + point.price();
            String safeTicker = point.ticker().toUpperCase(Locale.ROOT);
            String baseName = safeTicker + "_"
                    + point.timestamp().getEpochSecond() + "_"
                    + point.timestamp().getNano();

            Path file = directory.resolve(baseName + EXTENSION);
            int suffix = 1;
            while (true) {
                try {
                    Files.writeString(file, content, StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE_NEW);
                    return file;
                } catch (FileAlreadyExistsException e) {
                    file = directory.resolve(baseName + "_" + suffix + EXTENSION);
                    suffix++;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать точку истории в " + directory, e);
        }
    }

    @Override
    public List<PricePoint> load(Path directory, String ticker) {
        if (!Files.exists(directory)) {
            return new ArrayList<>();
        }
        String prefix = ticker.toUpperCase(Locale.ROOT) + "_";
        try (Stream<Path> files = Files.list(directory)) {
            return new ArrayList<>(files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(prefix))
                    .flatMap(p -> {
                        PricePoint point = tryReadPoint(p);
                        return point == null ? Stream.empty() : Stream.of(point);
                    })
                    .filter(p -> p.ticker().equalsIgnoreCase(ticker))
                    .sorted(Comparator.comparing(PricePoint::timestamp))
                    .toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать историю из " + directory, e);
        }
    }

    private PricePoint tryReadPoint(Path file) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String[] parts = content.split(DELIMITER, 3);
            if (parts.length < 3) {
                return null;
            }
            Instant timestamp = Instant.parse(parts[1]);
            BigDecimal price = new BigDecimal(parts[2]);
            return new PricePoint(parts[0], timestamp, price); // ticker — как есть, без апперкейса
        } catch (IOException | DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }
}