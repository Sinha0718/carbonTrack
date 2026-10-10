package com.carbontrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarbonTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarbonTrackApplication.class, args);
    }
}
