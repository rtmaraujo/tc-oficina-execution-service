package com.oficina.execution.service.domain;

public class ExecucaoOS {
    private Long id;
    private String sagaId;
    private String osNumero;
    private String descricaoDiagnostico;
    private String statusExecucao;
    private String pecasSubstituidas;
    private String observacoes;
    private String dataInicio;
    private String dataConclusao;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
    public String getOsNumero() { return osNumero; }
    public void setOsNumero(String osNumero) { this.osNumero = osNumero; }
    public String getDescricaoDiagnostico() { return descricaoDiagnostico; }
    public void setDescricaoDiagnostico(String descricaoDiagnostico) { this.descricaoDiagnostico = descricaoDiagnostico; }
    public String getStatusExecucao() { return statusExecucao; }
    public void setStatusExecucao(String statusExecucao) { this.statusExecucao = statusExecucao; }
    public String getPecasSubstituidas() { return pecasSubstituidas; }
    public void setPecasSubstituidas(String pecasSubstituidas) { this.pecasSubstituidas = pecasSubstituidas; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }
    public String getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(String dataConclusao) { this.dataConclusao = dataConclusao; }

    // Constructors
    public ExecucaoOS() {}

    public ExecucaoOS(Long id, String osNumero) {
        this.id = id;
        this.osNumero = osNumero;
        this.dataInicio = java.time.LocalDateTime.now().toString();
    }
}
