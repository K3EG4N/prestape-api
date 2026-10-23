package com.prestape;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PrestapeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrestapeApplication.class, args);
    }
}
