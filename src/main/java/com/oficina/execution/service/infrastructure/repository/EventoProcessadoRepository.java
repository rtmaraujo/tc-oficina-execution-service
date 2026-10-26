package com.oficina.execution.service.infrastructure.repository;

import com.oficina.execution.service.infrastructure.persistence.EventoProcessado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoProcessadoRepository extends MongoRepository<EventoProcessado, String> {
}
