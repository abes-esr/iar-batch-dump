package fr.abes.sudoc.iarbatchdump.takslet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component 
public class CallVectorizationApiTasklet implements Tasklet{

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.batch.vectorization-url}")
    protected String URL_API;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        

        restTemplate.postForEntity(
            URL_API, 
            null, 
            Void.class);
    
    
        return RepeatStatus.FINISHED;
    }
}
