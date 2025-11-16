package com.eventeanagementsystem.event_management_system.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {


    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .directory("./backend") // Look for .env in the backend folder
                .ignoreIfMalformed()    // Optional: useful for CI/CD
                .ignoreIfMissing()      // Optional: useful for CI/CD
                .load();
    }
}
