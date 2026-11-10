package br.edu.biopark.manutencao.controller;

import br.edu.biopark.manutencao.dto.LeituraRequest;
import br.edu.biopark.manutencao.dto.PredicaoResponse;
import br.edu.biopark.manutencao.service.PredicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Predição criada e salva no histórico",
                    headers = @Header(name = "Location", description = "Caminho da predição criada",
                            schema = @Schema(type = "string", example = "/api/predicoes/1")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PredicaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Leitura inválida ou JSON malformado",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Serviço de predição indisponível",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PredicaoResponse> criar(@Valid @RequestBody LeituraRequest request) {
        PredicaoResponse resposta = service.registrar(request);
        return ResponseEntity.created(URI.create("/api/predicoes/" + resposta.id())).body(resposta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma predição pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Predição encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PredicaoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Predição não encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PredicaoResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Lista as 50 predições mais recentes (opcionalmente de uma máquina)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico de predições, possivelmente vazio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PredicaoResponse.class))))
    })
    public List<PredicaoResponse> listar(@RequestParam(required = false) String maquinaId) {
        return service.listar(maquinaId);
    }
}
