package fr.abes.sudoc.iarbatchdump.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RameauJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job '{}' starting with parameters: {}", jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job '{}' completed successfully. Items read: {}, written: {}, skipped: {}",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getStepExecutions().stream().mapToLong(s -> s.getReadCount()).sum(),
                    jobExecution.getStepExecutions().stream().mapToLong(s -> s.getWriteCount()).sum(),
                    jobExecution.getStepExecutions().stream().mapToLong(s -> s.getSkipCount()).sum());
        } else {
            log.error("Job '{}' ended with status: {}", jobExecution.getJobInstance().getJobName(),
                    jobExecution.getStatus());
        }
    }
}
