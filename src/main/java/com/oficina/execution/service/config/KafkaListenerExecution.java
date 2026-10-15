package com.oficina.execution.service.config;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerExecution {

    // Consumir order-finalized (coreografia via Kafka)
    @KafkaListener(topics = "order-finalized", groupId = "execution-service-group")
    public void handleOrderFinalized(String message) {
        System.out.println("Execution Service received order-finalized: " + message);
        // Lógica para marcar execução como concluída
    }
}
