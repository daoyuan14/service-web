package sg.ncl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WebApplication {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ConnectionProperties connectionProperties() {
        return new ConnectionProperties();
    }

    @Bean
    WebProperties webProperties() {
        return new WebProperties();
    }

    public static void main(String[] args) {
        final SpringApplication application = new SpringApplication(WebApplication.class);
        application.addListeners(new ApplicationPidFileWriter());
        application.run(args);
    }

}
