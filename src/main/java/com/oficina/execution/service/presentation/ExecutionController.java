package com.oficina.execution.service.presentation;

import com.oficina.execution.service.domain.ExecucaoOS;
import com.oficina.execution.service.application.ExecutionApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/execution")
public class ExecutionController {

    @Autowired
    private ExecutionApplicationService executionService;

    @PostMapping("/iniciar/{osNumero}")
    public ResponseEntity<ExecucaoOS> iniciar(@PathVariable String osNumero) {
        ExecucaoOS execucao = executionService.iniciarDiagnostico(osNumero);
        if (execucao == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(execucao);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ExecucaoOS> atualizarStatus(@PathVariable Long id, @RequestParam String statusExecucao) {
        ExecucaoOS execucao = executionService.atualizarStatus(id, statusExecucao);
        if (execucao == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(execucao);
    }

    @PostMapping("/{id}/pecas")
    public ResponseEntity<ExecucaoOS> registrarPecas(@PathVariable Long id, @RequestParam String pecas) {
        ExecucaoOS execucao = executionService.registrarPecaSubstituida(id, pecas);
        if (execucao == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(execucao);
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ExecucaoOS> finalizar(@PathVariable Long id) {
        ExecucaoOS execucao = executionService.finalizarExecucao(id);
        if (execucao == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(execucao);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecucaoOS> consultar(@PathVariable Long id) {
        ExecucaoOS execucao = executionService.consultarPorId(id);
        if (execucao == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(execucao);
    }

    @GetMapping("/status/{statusExecucao}")
    public ResponseEntity<List<ExecucaoOS>> listarPorStatus(@PathVariable String statusExecucao) {
        List<ExecucaoOS> lista = executionService.listarPorStatus(statusExecucao);
        return ResponseEntity.ok(lista);
    }
}
