package br.edu.biopark.manutencao.controller;

import br.edu.biopark.manutencao.dto.LeituraRequest;
import br.edu.biopark.manutencao.dto.PredicaoResponse;
import br.edu.biopark.manutencao.service.PredicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/predicoes")
@Tag(name = "Predições", description = "Predição de falhas a partir de leituras de sensores")
public class PredicaoController {

    private final PredicaoService service;

    public PredicaoController(PredicaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Envia uma leitura de sensores e recebe a predição de falha")
    public ResponseEntity<PredicaoResponse> criar(@Valid @RequestBody LeituraRequest request) {
        PredicaoResponse resposta = service.registrar(request);
        return ResponseEntity.created(URI.create("/api/predicoes/" + resposta.id())).body(resposta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma predição pelo id")
    public PredicaoResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Lista as 50 predições mais recentes (opcionalmente de uma máquina)")
    public List<PredicaoResponse> listar(@RequestParam(required = false) String maquinaId) {
        return service.listar(maquinaId);
    }
}
