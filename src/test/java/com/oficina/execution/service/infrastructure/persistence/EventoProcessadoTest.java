package com.oficina.execution.service.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventoProcessado - Idempotencia no MongoDB")
class EventoProcessadoTest {

    @Test
    @DisplayName("Deve registrar evento processado")
    void shouldCreateEventoProcessado() {
        EventoProcessado evento = new EventoProcessado("evt-1", "execution-requested");

        assertEquals("evt-1", evento.getEventId());
        assertEquals("execution-requested", evento.getTopic());
        assertNotNull(evento.getProcessedAt());

        LocalDateTime data = LocalDateTime.of(2026, 1, 1, 10, 0);
        evento.setEventId("evt-2");
        evento.setTopic("compensate-execution");
        evento.setProcessedAt(data);

        assertEquals("evt-2", evento.getEventId());
        assertEquals("compensate-execution", evento.getTopic());
        assertEquals(data, evento.getProcessedAt());
    }

    @Test
    @DisplayName("Deve permitir construcao vazia para o driver do Mongo")
    void shouldCreateEmptyEventoProcessado() {
        EventoProcessado evento = new EventoProcessado();

        assertNull(evento.getEventId());
        assertNull(evento.getTopic());
        assertNull(evento.getProcessedAt());
    }
}
