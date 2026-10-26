package com.oficina.execution.service.application.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oficina.execution.service.application.ExecutionApplicationService;
import com.oficina.execution.service.application.event.SagaEvent;
import com.oficina.execution.service.domain.ExecucaoOS;
import com.oficina.execution.service.domain.StatusExecucao;
import com.oficina.execution.service.infrastructure.repository.ExecucaoOSRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExecutionApplicationServiceImpl implements ExecutionApplicationService {

    private final ExecucaoOSRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ExecutionApplicationServiceImpl(ExecucaoOSRepository repository,
                                           KafkaTemplate<String, String> kafkaTemplate,
                                           ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ExecucaoOS iniciarDiagnostico(String osNumero) {
        return iniciarPorSaga(null, osNumero);
    }

    @Override
    public ExecucaoOS iniciarPorSaga(String sagaId, String osNumero) {
        ExecucaoOS execucao = repository.findByOsNumero(osNumero);
        if (execucao == null) {
            execucao = new ExecucaoOS();
            execucao.setOsNumero(osNumero);
            // O MongoDB nao gera id Long automaticamente; usamos um id unico
            // baseado em timestamp para manter compatibilidade com o contrato.
            execucao.setId(System.currentTimeMillis());
        }
        if (sagaId != null) {
            execucao.setSagaId(sagaId);
        }
        execucao.setStatusExecucao(StatusExecucao.DIAGNOSTICO.toString());
        if (execucao.getDataInicio() == null) {
            execucao.setDataInicio(LocalDateTime.now().toString());
        }
        ExecucaoOS saved = repository.save(execucao);

        publicar("order-progress", saved, "EXECUTION_STARTED");
        return saved;
    }

    @Override
    public ExecucaoOS atualizarStatus(Long id, String statusExecucao) {
        Optional<ExecucaoOS> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        ExecucaoOS execucao = optional.get();
        execucao.setStatusExecucao(statusExecucao);
        if (StatusExecucao.CONCLUIDA.toString().equals(statusExecucao)
            || StatusExecucao.ENTREGUE.toString().equals(statusExecucao)) {
            execucao.setDataConclusao(LocalDateTime.now().toString());
        }
        ExecucaoOS updated = repository.save(execucao);

        publicar("order-progress", updated, StatusExecucao.CONCLUIDA.toString().equals(statusExecucao)
            ? "EXECUTION_COMPLETED" : "EXECUTION_PROGRESS");

        if (StatusExecucao.CONCLUIDA.toString().equals(statusExecucao)) {
            publicar("order-finalized", updated, "ORDER_FINALIZED");
        }
        return updated;
    }

    @Override
    public ExecucaoOS registrarPecaSubstituida(Long id, String pecas) {
        Optional<ExecucaoOS> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        ExecucaoOS execucao = optional.get();
        execucao.setPecasSubstituidas(pecas);
        ExecucaoOS updated = repository.save(execucao);
        publicar("order-progress", updated, "PARTS_REGISTERED");
        return updated;
    }

    @Override
    public ExecucaoOS finalizarExecucao(Long id) {
        Optional<ExecucaoOS> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        ExecucaoOS execucao = optional.get();
        execucao.setStatusExecucao(StatusExecucao.ENTREGUE.toString());
        execucao.setDataConclusao(LocalDateTime.now().toString());
        ExecucaoOS updated = repository.save(execucao);
        publicar("order-finalized", updated, "ORDER_FINALIZED");
        return updated;
    }

    @Override
    public ExecucaoOS cancelarPorSaga(String sagaId, String osNumero) {
        ExecucaoOS execucao = repository.findByOsNumero(osNumero);
        if (execucao == null) {
            return null;
        }
        execucao.setStatusExecucao(StatusExecucao.CANCELADA.toString());
        if (sagaId != null) {
            execucao.setSagaId(sagaId);
        }
        ExecucaoOS updated = repository.save(execucao);
        publicar("execution-compensated", updated, "EXECUTION_CANCELLED");
        return updated;
    }

    @Override
    public ExecucaoOS consultarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<ExecucaoOS> listarPorStatus(String statusExecucao) {
        return repository.findByStatusExecucao(statusExecucao);
    }

    private void publicar(String topico, ExecucaoOS execucao, String tipo) {
        SagaEvent event = SagaEvent.of(tipo, execucao.getSagaId());
        event.execucaoId = execucao.getId();
        event.osNumero = execucao.getOsNumero();
        event.status = execucao.getStatusExecucao();
        event.message = "OS " + execucao.getOsNumero() + " - " + tipo;
        try {
            kafkaTemplate.send(topico, String.valueOf(execucao.getId()),
                objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar evento " + tipo, e);
        }
    }
}
