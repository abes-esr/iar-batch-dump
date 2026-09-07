package fr.abes.sudoc.iarbatchdump.processor;

import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

import org.springframework.stereotype.Component;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;

@Component
@StepScope
public class DeduplicateNoticesProcessor
        implements ItemProcessor<CsvRecord, CsvRecord> {

    private final Set<String> ppns = new HashSet<>();

    @Override
    public CsvRecord process(CsvRecord item) {

        String ppn = item.getPpn();

        if (ppn == null || ppn.isBlank()) {
            return item;
        }

        if (ppns.add(ppn)) {
            return item;
        }

        return null;
    }
}