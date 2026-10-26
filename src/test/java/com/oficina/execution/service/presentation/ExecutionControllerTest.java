package com.oficina.execution.service.presentation;

import com.oficina.execution.service.application.ExecutionApplicationService;
import com.oficina.execution.service.domain.ExecucaoOS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutionController - Endpoints REST")
class ExecutionControllerTest {

    @Mock
    private ExecutionApplicationService executionService;

    private ExecutionController controller;

    @BeforeEach
    void setUp() {
        controller = new ExecutionController(executionService);
    }

    private ExecucaoOS execucao() {
        ExecucaoOS execucao = new ExecucaoOS(1L, "OS-1");
        execucao.setStatusExecucao("DIAGNOSTICO");
        return execucao;
    }

    @Test
    @DisplayName("Deve iniciar diagnostico")
    void shouldStartDiagnosis() {
        when(executionService.iniciarDiagnostico("OS-1")).thenReturn(execucao());

        ResponseEntity<ExecucaoOS> response = controller.iniciar("OS-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OS-1", response.getBody().getOsNumero());
    }

    @Test
    @DisplayName("Deve retornar 404 ao iniciar OS inexistente")
    void shouldReturnNotFoundOnStart() {
        when(executionService.iniciarDiagnostico("OS-X")).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND, controller.iniciar("OS-X").getStatusCode());
    }

    @Test
    @DisplayName("Deve atualizar status da execucao")
    void shouldUpdateStatus() {
        when(executionService.atualizarStatus(1L, "EXECUCAO")).thenReturn(execucao());

        assertEquals(HttpStatus.OK,
            controller.atualizarStatus(1L, "EXECUCAO").getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 404 ao atualizar execucao inexistente")
    void shouldReturnNotFoundOnStatusUpdate() {
        when(executionService.atualizarStatus(99L, "EXECUCAO")).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND,
            controller.atualizarStatus(99L, "EXECUCAO").getStatusCode());
    }

    @Test
    @DisplayName("Deve registrar pecas substituidas")
    void shouldRegisterParts() {
        when(executionService.registrarPecaSubstituida(1L, "filtro")).thenReturn(execucao());

        assertEquals(HttpStatus.OK, controller.registrarPecas(1L, "filtro").getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 404 ao registrar pecas em execucao inexistente")
    void shouldReturnNotFoundOnParts() {
        when(executionService.registrarPecaSubstituida(99L, "filtro")).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND,
            controller.registrarPecas(99L, "filtro").getStatusCode());
    }

    @Test
    @DisplayName("Deve finalizar execucao")
    void shouldFinishExecution() {
        when(executionService.finalizarExecucao(1L)).thenReturn(execucao());

        assertEquals(HttpStatus.OK, controller.finalizar(1L).getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 404 ao finalizar execucao inexistente")
    void shouldReturnNotFoundOnFinish() {
        when(executionService.finalizarExecucao(99L)).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND, controller.finalizar(99L).getStatusCode());
    }

    @Test
    @DisplayName("Deve consultar execucao existente")
    void shouldFindExecution() {
        when(executionService.consultarPorId(1L)).thenReturn(execucao());

        assertEquals(HttpStatus.OK, controller.consultar(1L).getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 404 ao consultar execucao inexistente")
    void shouldReturnNotFoundOnConsult() {
        when(executionService.consultarPorId(99L)).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND, controller.consultar(99L).getStatusCode());
    }

    @Test
    @DisplayName("Deve listar execucoes por status")
    void shouldListByStatus() {
        when(executionService.listarPorStatus("DIAGNOSTICO")).thenReturn(List.of(execucao()));

        ResponseEntity<List<ExecucaoOS>> response = controller.listarPorStatus("DIAGNOSTICO");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(executionService).listarPorStatus("DIAGNOSTICO");
        verify(executionService, never()).consultarPorId(anyLong());
        verify(executionService, never()).iniciarDiagnostico(anyString());
    }
}
