package fr.abes.sudoc.iarbatchdump;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(
        exclude = {
                HibernateJpaAutoConfiguration.class
        }
)
public class IarBatchDumpApplication {
    public static void main(String[] args) {
        SpringApplication.run(IarBatchDumpApplication.class, args);
    }
}