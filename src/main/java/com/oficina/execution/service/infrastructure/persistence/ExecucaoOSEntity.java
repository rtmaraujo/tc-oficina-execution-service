package com.oficina.execution.service.infrastructure.persistence;

import com.oficina.execution.service.domain.ExecucaoOS;
import com.oficina.execution.service.domain.StatusExecucao;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "execucao_os")
public class ExecucaoOSEntity {

    @Id
    private Long id;

    private String osNumero;

    private String descricaoDiagnostico;

    private String statusExecucao;

    private String pecasSubstituidas;

    private String observacoes;

    private LocalDateTime dataInicio;

    private LocalDateTime dataConclusao;

    // Default constructor
    public ExecucaoOSEntity() {
        this.dataInicio = LocalDateTime.now();
    }

    // Constructor from domain
    public ExecucaoOSEntity(ExecucaoOS dominio) {
        this.id = dominio.getId();
        this.osNumero = dominio.getOsNumero();
        this.descricaoDiagnostico = dominio.getDescricaoDiagnostico();
        this.statusExecucao = dominio.getStatusExecucao();
        this.pecasSubstituidas = dominio.getPecasSubstituidas();
        this.observacoes = dominio.getObservacoes();
        this.dataInicio = LocalDateTime.now();
        this.dataConclusao = dominio.getDataConclusao() != null ? LocalDateTime.parse(dominio.getDataConclusao()) : null;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }

    // Convert to domain
    public ExecucaoOS toDomain() {
        ExecucaoOS dominio = new ExecucaoOS();
        dominio.setId(this.id);
        dominio.setOsNumero(this.osNumero);
        dominio.setDescricaoDiagnostico(this.descricaoDiagnostico);
        dominio.setStatusExecucao(this.statusExecucao);
        dominio.setPecasSubstituidas(this.pecasSubstituidas);
        dominio.setObservacoes(this.observacoes);
        dominio.setDataInicio(this.dataInicio.toString());
        dominio.setDataConclusao(this.dataConclusao != null ? this.dataConclusao.toString() : null);
        return dominio;
    }
}
