package com.oficina.execution.service.infrastructure.persistence;

import com.oficina.execution.service.domain.ExecucaoOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExecucaoOSEntity - Testes de Persistencia")
class ExecucaoOSEntityTest {

    @Test
    @DisplayName("Deve criar entidade com valores padrao")
    void shouldCreateDefaultEntity() {
        ExecucaoOSEntity entity = new ExecucaoOSEntity();

        assertNull(entity.getId());
        assertNotNull(entity.getDataInicio());
        assertNull(entity.getDataConclusao());
    }

    @Test
    @DisplayName("Deve criar entidade a partir do dominio sem data de conclusao")
    void shouldCreateEntityFromDomainWithoutConclusionDate() {
        ExecucaoOS dominio = new ExecucaoOS(1L, "OS-1");
        dominio.setSagaId("saga-1");
        dominio.setDescricaoDiagnostico("Diagnostico");
        dominio.setStatusExecucao("DIAGNOSTICO");
        dominio.setPecasSubstituidas("Filtro");
        dominio.setObservacoes("Ok");

        ExecucaoOSEntity entity = new ExecucaoOSEntity(dominio);

        assertEquals(1L, entity.getId());
        assertEquals("saga-1", entity.getSagaId());
        assertEquals("OS-1", entity.getOsNumero());
        assertEquals("Diagnostico", entity.getDescricaoDiagnostico());
        assertEquals("DIAGNOSTICO", entity.getStatusExecucao());
        assertEquals("Filtro", entity.getPecasSubstituidas());
        assertEquals("Ok", entity.getObservacoes());
        assertNotNull(entity.getDataInicio());
        assertNull(entity.getDataConclusao());
    }

    @Test
    @DisplayName("Deve criar entidade a partir do dominio com data de conclusao")
    void shouldCreateEntityFromDomainWithConclusionDate() {
        ExecucaoOS dominio = new ExecucaoOS(2L, "OS-2");
        dominio.setDataConclusao("2026-01-02T11:00:00");

        ExecucaoOSEntity entity = new ExecucaoOSEntity(dominio);

        assertNotNull(entity.getDataConclusao());
        assertEquals(LocalDateTime.of(2026, 1, 2, 11, 0), entity.getDataConclusao());
    }

    @Test
    @DisplayName("Deve atualizar todos os campos via setters")
    void shouldSetAllFields() {
        ExecucaoOSEntity entity = new ExecucaoOSEntity();
        LocalDateTime inicio = LocalDateTime.of(2026, 3, 1, 8, 0);
        LocalDateTime conclusao = LocalDateTime.of(2026, 3, 2, 18, 0);

        entity.setId(3L);
        entity.setSagaId("saga-3");
        entity.setOsNumero("OS-3");
        entity.setDescricaoDiagnostico("Diagnostico");
        entity.setStatusExecucao("CONCLUIDA");
        entity.setPecasSubstituidas("Correia");
        entity.setObservacoes("Finalizado");
        entity.setDataInicio(inicio);
        entity.setDataConclusao(conclusao);

        assertEquals(3L, entity.getId());
        assertEquals("saga-3", entity.getSagaId());
        assertEquals("OS-3", entity.getOsNumero());
        assertEquals("Diagnostico", entity.getDescricaoDiagnostico());
        assertEquals("CONCLUIDA", entity.getStatusExecucao());
        assertEquals("Correia", entity.getPecasSubstituidas());
        assertEquals("Finalizado", entity.getObservacoes());
        assertEquals(inicio, entity.getDataInicio());
        assertEquals(conclusao, entity.getDataConclusao());
    }

    @Test
    @DisplayName("Deve converter entidade para dominio sem data de conclusao")
    void shouldConvertToDomainWithoutConclusionDate() {
        ExecucaoOSEntity entity = new ExecucaoOSEntity();
        entity.setId(4L);
        entity.setSagaId("saga-4");
        entity.setOsNumero("OS-4");
        entity.setDescricaoDiagnostico("Diagnostico");
        entity.setStatusExecucao("REPARO");
        entity.setPecasSubstituidas("Bomba");
        entity.setObservacoes("Em andamento");
        entity.setDataInicio(LocalDateTime.of(2026, 4, 1, 9, 0));

        ExecucaoOS dominio = entity.toDomain();

        assertEquals(4L, dominio.getId());
        assertEquals("saga-4", dominio.getSagaId());
        assertEquals("OS-4", dominio.getOsNumero());
        assertEquals("Diagnostico", dominio.getDescricaoDiagnostico());
        assertEquals("REPARO", dominio.getStatusExecucao());
        assertEquals("Bomba", dominio.getPecasSubstituidas());
        assertEquals("Em andamento", dominio.getObservacoes());
        assertEquals("2026-04-01T09:00", dominio.getDataInicio());
        assertNull(dominio.getDataConclusao());
    }

    @Test
    @DisplayName("Deve converter entidade para dominio com data de conclusao")
    void shouldConvertToDomainWithConclusionDate() {
        ExecucaoOSEntity entity = new ExecucaoOSEntity();
        entity.setDataInicio(LocalDateTime.of(2026, 5, 1, 9, 0));
        entity.setDataConclusao(LocalDateTime.of(2026, 5, 2, 17, 30));

        ExecucaoOS dominio = entity.toDomain();

        assertNotNull(dominio.getDataConclusao());
        assertEquals("2026-05-02T17:30", dominio.getDataConclusao());
    }
}
