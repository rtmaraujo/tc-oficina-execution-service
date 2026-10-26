package com.oficina.execution.service.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Registro de idempotencia: cada eventId consumido e gravado uma unica vez.
 */
@Document(collection = "evento_processado")
public class EventoProcessado {

    @Id
    private String eventId;

    private String topic;

    private LocalDateTime processedAt;

    public EventoProcessado() {
    }

    public EventoProcessado(String eventId, String topic) {
        this.eventId = eventId;
        this.topic = topic;
        this.processedAt = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
