package com.oficina.execution.service.application.impl;

import com.oficina.execution.service.domain.ExecucaoOS;
import com.oficina.execution.service.domain.StatusExecucao;
import com.oficina.execution.service.application.ExecutionApplicationService;
import com.oficina.execution.service.infrastructure.repository.ExecucaoOSRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ExecutionApplicationServiceImpl implements ExecutionApplicationService {

    private final ExecucaoOSRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ExecutionApplicationServiceImpl(ExecucaoOSRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public ExecucaoOS iniciarDiagnostico(String osNumero) {
        // Buscar OS existente ou criar nova execução
        ExecucaoOS execucao = repository.findByOsNumero(osNumero);
        if (execucao == null) {
            execucao = new ExecucaoOS(null, osNumero);
        }
        execucao.setStatusExecucao(StatusExecucao.DIAGNOSTICO.toString());
        execucao.setDataInicio(java.time.LocalDateTime.now().toString());
        ExecucaoOS saved = repository.save(execucao);
        
        // Publicar evento Kafka
        kafkaTemplate.send("order-progress", saved.getId().toString(),
            "ExecucaoOS{" +
            "id=" + saved.getId() +
            ", osNumero='" + saved.getOsNumero() + '\'' +
            ", status='" + saved.getStatusExecucao() + '\'' +
            '}');
        
        return saved;
    }

    @Override
    public ExecucaoOS atualizarStatus(Long id, String statusExecucao) {
        Optional<ExecucaoOS> optional = repository.findById(id);
        if (optional.isPresent()) {
            ExecucaoOS execucao = optional.get();
            execucao.setStatusExecucao(statusExecucao);
            if (StatusExecucao.CONCLUIDA.toString().equals(statusExecucao)) {
                execucao.setDataConclusao(java.time.LocalDateTime.now().toString());
                // Publicar evento de finalização
                kafkaTemplate.send("order-finalized", execucao.getId().toString(),
                    "OS " + execucao.getOsNumero() + " finalizada e entregue");
            }
            ExecucaoOS updated = repository.save(execucao);
            
            kafkaTemplate.send("order-progress", updated.getId().toString(),
                "Execucao status updated: " + updated.getStatusExecucao());
            
            return updated;
        }
        return null;
    }

    @Override
    public ExecucaoOS registrarPecaSubstituida(Long id, String pecas) {
        Optional<ExecucaoOS> optional = repository.findById(id);
        if (optional.isPresent()) {
            ExecucaoOS execucao = optional.get();
            execucao.setPecasSubstituidas(pecas);
            ExecucaoOS updated = repository.save(execucao);
            kafkaTemplate.send("order-progress", updated.getId().toString(),
                "Peças substituídas: " + updated.getPecasSubstituidas() + " OS " + updated.getOsNumero());
            return updated;
        }
        return null;
    }

    @Override
    public ExecucaoOS finalizarExecucao(Long id) {
        Optional<ExecucaoOS> optional = repository.findById(id);
        if (optional.isPresent()) {
            ExecucaoOS execucao = optional.get();
            execucao.setStatusExecucao(StatusExecucao.ENTREGUE.toString());
            execucao.setDataConclusao(java.time.LocalDateTime.now().toString());
            ExecucaoOS updated = repository.save(execucao);
            kafkaTemplate.send("order-finalized", updated.getId().toString(),
                "OS " + updated.getOsNumero() + " entregue com sucesso");
            return updated;
        }
        return null;
    }

    @Override
    public ExecucaoOS consultarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public java.util.List<ExecucaoOS> listarPorStatus(String statusExecucao) {
        return repository.findByStatusExecucao(statusExecucao);
    }
}
