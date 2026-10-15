package com.oficina.execution.service.infrastructure.repository;

import com.oficina.execution.service.domain.ExecucaoOS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecucaoOSRepository extends MongoRepository<ExecucaoOS, Long> {
    ExecucaoOS findByOsNumero(String osNumero);
    java.util.List<ExecucaoOS> findByStatusExecucao(String statusExecucao);
}
