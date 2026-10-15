package com.oficina.execution.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class TcOficinaExecutionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TcOficinaExecutionApplication.class, args);
    }
}
