package com.oficina.execution.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oficina.execution.service.application.ExecutionApplicationService;
import com.oficina.execution.service.application.event.SagaEvent;
import com.oficina.execution.service.infrastructure.persistence.EventoProcessado;
import com.oficina.execution.service.infrastructure.repository.EventoProcessadoRepository;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerExecution {

    private final ExecutionApplicationService executionService;
    private final EventoProcessadoRepository eventoRepository;
    private final ObjectMapper objectMapper;

    public KafkaListenerExecution(ExecutionApplicationService executionService,
                                  EventoProcessadoRepository eventoRepository,
                                  ObjectMapper objectMapper) {
        this.executionService = executionService;
        this.eventoRepository = eventoRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "execution-requested", groupId = "execution-service-group")
    public void handleExecutionRequested(String message) {
        SagaEvent event = ler(message, "execution-requested");
        if (event == null || jaProcessado(event.eventId)) {
            return;
        }
        try {
            MDC.put("sagaId", event.sagaId);
            executionService.iniciarPorSaga(event.sagaId, event.osNumero);
            registrar(event.eventId, "execution-requested");
        } finally {
            MDC.remove("sagaId");
        }
    }

    @KafkaListener(topics = "compensate-execution", groupId = "execution-service-group")
    public void handleCompensateExecution(String message) {
        SagaEvent event = ler(message, "compensate-execution");
        if (event == null || jaProcessado(event.eventId)) {
            return;
        }
        try {
            MDC.put("sagaId", event.sagaId);
            executionService.cancelarPorSaga(event.sagaId, event.osNumero);
            registrar(event.eventId, "compensate-execution");
        } finally {
            MDC.remove("sagaId");
        }
    }

    private SagaEvent ler(String message, String topico) {
        try {
            return objectMapper.readValue(message, SagaEvent.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Payload invalido em " + topico, e);
        }
    }

    private boolean jaProcessado(String eventId) {
        return eventId != null && eventoRepository.existsById(eventId);
    }

    private void registrar(String eventId, String topico) {
        if (eventId != null) {
            eventoRepository.save(new EventoProcessado(eventId, topico));
        }
    }
}
