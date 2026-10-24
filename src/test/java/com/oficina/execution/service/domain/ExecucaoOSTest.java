package com.oficina.execution.service.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExecucaoOS - Testes de Dominio")
class ExecucaoOSTest {

    @Test
    @DisplayName("Deve criar execucao vazia")
    void shouldCreateEmptyExecucao() {
        ExecucaoOS execucao = new ExecucaoOS();

        assertNull(execucao.getId());
        assertNull(execucao.getOsNumero());
        assertNull(execucao.getDescricaoDiagnostico());
        assertNull(execucao.getStatusExecucao());
        assertNull(execucao.getPecasSubstituidas());
        assertNull(execucao.getObservacoes());
        assertNull(execucao.getDataInicio());
        assertNull(execucao.getDataConclusao());
    }

    @Test
    @DisplayName("Deve criar execucao com construtor completo")
    void shouldCreateExecucaoWithFullConstructor() {
        ExecucaoOS execucao = new ExecucaoOS(1L, "OS-1");

        assertEquals(1L, execucao.getId());
        assertEquals("OS-1", execucao.getOsNumero());
        assertNotNull(execucao.getDataInicio());
    }

    @Test
    @DisplayName("Deve atualizar todos os campos via setters")
    void shouldSetAllFields() {
        ExecucaoOS execucao = new ExecucaoOS();

        execucao.setId(2L);
        execucao.setOsNumero("OS-2");
        execucao.setDescricaoDiagnostico("Diagnostico completo");
        execucao.setStatusExecucao("REPARO");
        execucao.setPecasSubstituidas("Pastilha de freio");
        execucao.setObservacoes("Cliente autorizou");
        execucao.setDataInicio("2026-01-01T10:00:00");
        execucao.setDataConclusao("2026-01-02T10:00:00");

        assertEquals(2L, execucao.getId());
        assertEquals("OS-2", execucao.getOsNumero());
        assertEquals("Diagnostico completo", execucao.getDescricaoDiagnostico());
        assertEquals("REPARO", execucao.getStatusExecucao());
        assertEquals("Pastilha de freio", execucao.getPecasSubstituidas());
        assertEquals("Cliente autorizou", execucao.getObservacoes());
        assertEquals("2026-01-01T10:00:00", execucao.getDataInicio());
        assertEquals("2026-01-02T10:00:00", execucao.getDataConclusao());
    }

    @Test
    @DisplayName("Deve expor todos os status de execucao")
    void shouldExposeAllStatus() {
        assertEquals(7, StatusExecucao.values().length);
        assertEquals(StatusExecucao.AGUARDANDO, StatusExecucao.valueOf("AGUARDANDO"));
        assertEquals(StatusExecucao.DIAGNOSTICO, StatusExecucao.valueOf("DIAGNOSTICO"));
        assertEquals(StatusExecucao.REPARO, StatusExecucao.valueOf("REPARO"));
        assertEquals(StatusExecucao.TESTE, StatusExecucao.valueOf("TESTE"));
        assertEquals(StatusExecucao.CONCLUIDA, StatusExecucao.valueOf("CONCLUIDA"));
        assertEquals(StatusExecucao.ENTREGUE, StatusExecucao.valueOf("ENTREGUE"));
        assertEquals(StatusExecucao.CANCELADA, StatusExecucao.valueOf("CANCELADA"));
    }
}
