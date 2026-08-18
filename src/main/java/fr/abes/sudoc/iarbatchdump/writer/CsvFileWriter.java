package fr.abes.sudoc.iarbatchdump.writer;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.NonNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvFileWriter implements ItemWriter<CsvRecord> {

    private FileSystemResource fileResource;
    private BufferedWriter writer;

    public void write(List<? extends CsvRecord> items) throws Exception {
        if (writer == null) {
            writer = new BufferedWriter(new OutputStreamWriter(fileResource.getOutputStream(), StandardCharsets.UTF_8));
            // Écrire l'en-tête si nécessaire
            writer.write("ppn\tthese\ttitre\tresume\trameau\tlangue\n");
        }
        for (CsvRecord record : items) {
            writer.write(record.toCsvLine() + "\n");
        }
        writer.flush();
    }

    public void afterStep(StepExecution stepExecution) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur à la fermeture du fichier CSV", e);
        }
    }

    public void setFileResource(FileSystemResource fileResource) {
        this.fileResource = fileResource;
    }

    @Override
    public void write(@NonNull Chunk<? extends CsvRecord> chunk) throws Exception {

    }
}