package com.oficina.execution.service.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oficina.execution.service.domain.ExecucaoOS;
import com.oficina.execution.service.domain.StatusExecucao;
import com.oficina.execution.service.infrastructure.repository.ExecucaoOSRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutionApplicationServiceImpl - Testes de Aplicacao")
class ExecutionApplicationServiceImplTest {

    @Mock
    private ExecucaoOSRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ExecutionApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExecutionApplicationServiceImpl(repository, kafkaTemplate, new ObjectMapper());
    }

    private ExecucaoOS execucaoExistente() {
        ExecucaoOS execucao = new ExecucaoOS(1L, "OS-1");
        execucao.setSagaId("saga-1");
        execucao.setStatusExecucao(StatusExecucao.AGUARDANDO.toString());
        return execucao;
    }

    @Test
    @DisplayName("Deve iniciar diagnostico criando nova execucao quando nao existir")
    void shouldStartDiagnosisCreatingNewExecution() {
        when(repository.findByOsNumero("OS-1")).thenReturn(null);
        when(repository.save(any(ExecucaoOS.class))).thenAnswer(invocation -> {
            ExecucaoOS execucao = invocation.getArgument(0);
            execucao.setId(10L);
            return execucao;
        });

        ExecucaoOS result = service.iniciarDiagnostico("OS-1");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("OS-1", result.getOsNumero());
        assertEquals(StatusExecucao.DIAGNOSTICO.toString(), result.getStatusExecucao());
        assertNotNull(result.getDataInicio());
        verify(kafkaTemplate).send(eq("order-progress"), eq("10"), contains("\"osNumero\":\"OS-1\""));
    }

    @Test
    @DisplayName("Deve iniciar diagnostico reutilizando execucao existente")
    void shouldStartDiagnosisReusingExistingExecution() {
        ExecucaoOS existente = execucaoExistente();
        when(repository.findByOsNumero("OS-1")).thenReturn(existente);
        when(repository.save(any(ExecucaoOS.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoOS result = service.iniciarPorSaga("saga-1", "OS-1");

        assertSame(existente, result);
        assertEquals("saga-1", result.getSagaId());
        assertEquals(StatusExecucao.DIAGNOSTICO.toString(), result.getStatusExecucao());
        verify(repository, times(1)).save(existente);
    }

    @Test
    @DisplayName("Deve iniciar execucao via saga quando ainda nao existir")
    void shouldStartFromSagaWhenNew() {
        when(repository.findByOsNumero("OS-2")).thenReturn(null);
        when(repository.save(any(ExecucaoOS.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoOS result = service.iniciarPorSaga("saga-2", "OS-2");

        assertNotNull(result.getId());
        assertEquals("saga-2", result.getSagaId());
        assertEquals("OS-2", result.getOsNumero());
    }

    @Test
    @DisplayName("Deve atualizar status e publicar evento de progresso")
    void shouldUpdateStatusAndPublishProgress() {
        ExecucaoOS execucao = execucaoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(execucao));
        when(repository.save(any(ExecucaoOS.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoOS result = service.atualizarStatus(1L, "REPARO");

        assertEquals("REPARO", result.getStatusExecucao());
        assertNull(result.getDataConclusao());
        verify(kafkaTemplate).send(eq("order-progress"), eq("1"), contains("REPARO"));
        verify(kafkaTemplate, never()).send(eq("order-finalized"), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve atualizar status para CONCLUIDA, registrar conclusao e publicar eventos")
    void shouldUpdateStatusToConcluida() {
        ExecucaoOS execucao = execucaoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(execucao));
        when(repository.save(any(ExecucaoOS.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoOS result = service.atualizarStatus(1L, StatusExecucao.CONCLUIDA.toString());

        assertEquals(StatusExecucao.CONCLUIDA.toString(), result.getStatusExecucao());
        assertNotNull(result.getDataConclusao());
        verify(kafkaTemplate).send(eq("order-finalized"), eq("1"), contains("OS-1"));
        verify(kafkaTemplate).send(eq("order-progress"), eq("1"), contains("CONCLUIDA"));
    }

    @Test
    @DisplayName("Deve retornar null ao atualizar status de execucao inexistente")
    void shouldReturnNullWhenUpdatingUnknownExecution() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.atualizarStatus(99L, "REPARO"));
        verify(repository, never()).save(any(ExecucaoOS.class));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Deve registrar peca substituida e publicar evento")
    void shouldRegisterReplacedPart() {
        ExecucaoOS execucao = execucaoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(execucao));
        when(repository.save(any(ExecucaoOS.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoOS result = service.registrarPecaSubstituida(1L, "Pastilha de freio");

        assertEquals("Pastilha de freio", result.getPecasSubstituidas());
        verify(kafkaTemplate).send(eq("order-progress"), eq("1"), contains("PARTS_REGISTERED"));
    }

    @Test
    @DisplayName("Deve retornar null ao registrar peca em execucao inexistente")
    void shouldReturnNullWhenRegisteringPartOnUnknownExecution() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.registrarPecaSubstituida(99L, "Peca"));
        verify(repository, never()).save(any(ExecucaoOS.class));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Deve finalizar execucao e publicar evento de finalizacao")
    void shouldFinishExecution() {
        ExecucaoOS execucao = execucaoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(execucao));
        when(repository.save(any(ExecucaoOS.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoOS result = service.finalizarExecucao(1L);

        assertEquals(StatusExecucao.ENTREGUE.toString(), result.getStatusExecucao());
        assertNotNull(result.getDataConclusao());
        verify(kafkaTemplate).send(eq("order-finalized"), eq("1"), contains("OS-1"));
    }

    @Test
    @DisplayName("Deve retornar null ao finalizar execucao inexistente")
    void shouldReturnNullWhenFinishingUnknownExecution() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.finalizarExecucao(99L));
        verify(repository, never()).save(any(ExecucaoOS.class));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Deve cancelar execucao por saga e publicar evento de compensacao")
    void shouldCancelBySaga() {
        ExecucaoOS execucao = execucaoExistente();
        when(repository.findByOsNumero("OS-1")).thenReturn(execucao);
        when(repository.save(any(ExecucaoOS.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoOS result = service.cancelarPorSaga("saga-1", "OS-1");

        assertEquals(StatusExecucao.CANCELADA.toString(), result.getStatusExecucao());
        verify(kafkaTemplate).send(eq("execution-compensated"), eq("1"), contains("EXECUTION_CANCELLED"));
    }

    @Test
    @DisplayName("Deve retornar null ao cancelar execucao inexistente")
    void shouldReturnNullWhenCancellingUnknownExecution() {
        when(repository.findByOsNumero("OS-X")).thenReturn(null);

        assertNull(service.cancelarPorSaga("saga-x", "OS-X"));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("Deve consultar execucao por id")
    void shouldFindExecutionById() {
        ExecucaoOS execucao = execucaoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(execucao));

        assertNotNull(service.consultarPorId(1L));
    }

    @Test
    @DisplayName("Deve retornar null ao consultar execucao inexistente")
    void shouldReturnNullWhenExecutionNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.consultarPorId(99L));
    }

    @Test
    @DisplayName("Deve listar execucoes por status")
    void shouldListExecutionsByStatus() {
        List<ExecucaoOS> execucoes = List.of(execucaoExistente());
        when(repository.findByStatusExecucao("DIAGNOSTICO")).thenReturn(execucoes);

        List<ExecucaoOS> result = service.listarPorStatus("DIAGNOSTICO");

        assertEquals(1, result.size());
        verify(repository).findByStatusExecucao("DIAGNOSTICO");
    }
}
