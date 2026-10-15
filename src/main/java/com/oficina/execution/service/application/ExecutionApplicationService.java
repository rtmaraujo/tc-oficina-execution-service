package com.oficina.execution.service.application;

import com.oficina.execution.service.domain.ExecucaoOS;
import java.util.List;

public interface ExecutionApplicationService {
    ExecucaoOS iniciarDiagnostico(String osNumero);
    ExecucaoOS atualizarStatus(Long id, String statusExecucao);
    ExecucaoOS registrarPecaSubstituida(Long id, String pecas);
    ExecucaoOS finalizarExecucao(Long id);
    ExecucaoOS consultarPorId(Long id);
    List<ExecucaoOS> listarPorStatus(String statusExecucao);
}
