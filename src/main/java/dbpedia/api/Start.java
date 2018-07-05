package dbpedia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@EnableScheduling
public class Start {
  public static void main(String[] args) {
    SpringApplication.run(Start.class, args);
  }
}



