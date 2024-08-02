package com.company.testtask.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class RandomGeneratorConfig {

    @Bean
    public Random random() {
        return new Random();
    }
}
