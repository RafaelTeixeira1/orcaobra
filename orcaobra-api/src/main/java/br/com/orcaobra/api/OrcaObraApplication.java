package br.com.orcaobra.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrcaObraApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrcaObraApplication.class, args);
    }
}
