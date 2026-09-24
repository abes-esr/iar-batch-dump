package fr.abes.sudoc.iarbatchdump.writer;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;

@Configuration
public class NoticeCsvWriterConfig {

    @Value("${app.batch.action}")
    private String action;
    

    @Bean
    @StepScope
    @Primary
    public FlatFileItemWriter<CsvRecord> noticeCsvWriter(
            @Value("#{jobParameters['outputFilePath']}") String outputFilePath,
            @Value("#{jobParameters['filename']}") String filename
            ) {
        
        String outputFilename = filename.split(".csv")[0] + "_" + action + ".csv";

        FlatFileItemWriter<CsvRecord> writer = new FlatFileItemWriter<>();
        writer.setName("noticeCsvWriter");
        writer.setResource(new FileSystemResource(outputFilePath + "/" + outputFilename));
        writer.setAppendAllowed(false);
        writer.setLineAggregator(item -> item.toCsvLine());
        writer.setHeaderCallback(w -> w.write("ppn\tthese\ttitre\tresume\trameau\tlangue"));
        return writer;
    }


    @Bean
    @StepScope
    public FlatFileItemWriter<CsvRecord> deduplicationCsvWriter(
            @Value("#{jobParameters['outputFilePath']}") String outputFilePath,
            @Value("#{jobParameters['filename']}") String filename) {

        String outputFilename = "deduplicated_" + filename;

        FlatFileItemWriter<CsvRecord> writer = new FlatFileItemWriter<>();

        writer.setName("deduplicationCsvWriter");
        writer.setResource( new FileSystemResource( outputFilePath + "/" + outputFilename ) );
        writer.setAppendAllowed(false);
        writer.setLineAggregator(CsvRecord::toCsvLine);
        writer.setHeaderCallback( w -> w.write( "ppn\tthese\ttitre\tresume\trameau\tlangue" ) );

        return writer;
    }



}


