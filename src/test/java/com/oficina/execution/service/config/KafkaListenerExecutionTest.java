package com.oficina.execution.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oficina.execution.service.application.ExecutionApplicationService;
import com.oficina.execution.service.application.event.SagaEvent;
import com.oficina.execution.service.infrastructure.persistence.EventoProcessado;
import com.oficina.execution.service.infrastructure.repository.EventoProcessadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("KafkaListenerExecution - Consumo dos comandos da Saga")
class KafkaListenerExecutionTest {

    @Mock
    private ExecutionApplicationService executionService;
    @Mock
    private EventoProcessadoRepository eventoRepository;

    private KafkaListenerExecution listener;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new KafkaListenerExecution(executionService, eventoRepository, objectMapper);
        when(eventoRepository.existsById(anyString())).thenReturn(false);
        when(eventoRepository.save(any(EventoProcessado.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private String payload(String tipo) {
        SagaEvent event = SagaEvent.of(tipo, "SAGA-OS-1");
        event.osNumero = "OS-1";
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("Deve iniciar execucao ao consumir execution-requested")
    void shouldStartExecution() {
        listener.handleExecutionRequested(payload("EXECUTION_REQUESTED"));

        verify(executionService).iniciarPorSaga("SAGA-OS-1", "OS-1");
        verify(eventoRepository).save(any(EventoProcessado.class));
    }

    @Test
    @DisplayName("Deve cancelar execucao ao consumir compensate-execution")
    void shouldCompensateExecution() {
        listener.handleCompensateExecution(payload("COMPENSATE_EXECUTION"));

        verify(executionService).cancelarPorSaga("SAGA-OS-1", "OS-1");
        verify(eventoRepository).save(any(EventoProcessado.class));
    }

    @Test
    @DisplayName("Deve ignorar evento ja processado")
    void shouldIgnoreDuplicatedEvent() {
        when(eventoRepository.existsById(anyString())).thenReturn(true);

        listener.handleExecutionRequested(payload("EXECUTION_REQUESTED"));

        verifyNoInteractions(executionService);
        verify(eventoRepository, never()).save(any(EventoProcessado.class));
    }

    @Test
    @DisplayName("Deve falhar com payload invalido")
    void shouldFailOnInvalidPayload() {
        assertThrows(IllegalArgumentException.class,
            () -> listener.handleExecutionRequested("{invalido}"));
        verifyNoInteractions(executionService);
    }

    @Test
    @DisplayName("Nao deve registrar idempotencia quando o evento nao tiver eventId")
    void shouldNotRegisterWithoutEventId() {
        listener.handleCompensateExecution(
            "{\"type\":\"COMPENSATE_EXECUTION\",\"sagaId\":\"SAGA-1\",\"osNumero\":\"OS-1\"}");

        verify(executionService).cancelarPorSaga("SAGA-1", "OS-1");
        verify(eventoRepository, never()).save(any(EventoProcessado.class));
    }
}
