package com.oficina.execution.service.application.event;

import java.time.OffsetDateTime;

/**
 * Contrato de evento trocado entre os microsservicos da Saga.
 *
 * <p>O payload e serializado como JSON no Kafka. Campos nulos sao omitidos
 * para manter os eventos enxutos.</p>
 */
public class SagaEvent {

    public String eventId;
    public String sagaId;
    public String type;
    public Long osId;
    public String osNumero;
    public Long execucaoId;
    public Long orcamentoId;
    public String codigoOrcamento;
    public Double valor;
    public String status;
    public String paymentId;
    public String message;
    public String occurredAt;

    public SagaEvent() {
        this.occurredAt = OffsetDateTime.now().toString();
    }

    public static SagaEvent of(String type, String sagaId) {
        SagaEvent event = new SagaEvent();
        event.type = type;
        event.sagaId = sagaId;
        event.eventId = java.util.UUID.randomUUID().toString();
        return event;
    }
}
