package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.client.InstrumentQuoteMapper;
import academy.backend.market_pulse.dto.InstrumentQuoteDto;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;

public class QuoteFileStoreSkeleton implements QuoteFileStore {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void save(Path path, List<Quote> quotes) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<InstrumentQuoteDto> dtos = quotes.stream()
                    .map(InstrumentQuoteMapper::toDto)
                    .toList();
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), dtos);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить котировки в " + path, e);
        }
    }

    @Override
    public List<Quote> load(Path path) {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            List<InstrumentQuoteDto> dtos = OBJECT_MAPPER.readValue(
                    path.toFile(),
                    OBJECT_MAPPER.getTypeFactory()
                            .constructCollectionType(List.class, InstrumentQuoteDto.class));
            return new ArrayList<>(dtos.stream().map(InstrumentQuoteMapper::toQuote).toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать котировки из " + path, e);
        }
    }
}