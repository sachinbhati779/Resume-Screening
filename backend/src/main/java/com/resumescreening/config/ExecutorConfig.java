package com.resumescreening.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService screeningExecutor(
            @Value("${app.screening.thread-pool-size:8}") int threadPoolSize) {
        return Executors.newFixedThreadPool(Math.max(2, threadPoolSize));
    }
}
