package br.edu.biopark.manutencao.service;

import br.edu.biopark.manutencao.client.MlClient;
import br.edu.biopark.manutencao.domain.Predicao;
import br.edu.biopark.manutencao.dto.LeituraRequest;
import br.edu.biopark.manutencao.dto.MlPredictRequest;
import br.edu.biopark.manutencao.dto.MlPredictResponse;
import br.edu.biopark.manutencao.dto.PredicaoResponse;
import br.edu.biopark.manutencao.exception.RecursoNaoEncontradoException;
import br.edu.biopark.manutencao.repository.PredicaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PredicaoService {

    private final PredicaoRepository repository;
    private final MlClient mlClient;

    public PredicaoService(PredicaoRepository repository, MlClient mlClient) {
        this.repository = repository;
        this.mlClient = mlClient;
    }

    /**
     * Sem @Transactional de propósito: a chamada HTTP ao ML fica fora da transação,
     * para não segurar conexão do banco enquanto espera o Python.
     */
    public PredicaoResponse registrar(LeituraRequest r) {
        MlPredictResponse ml = mlClient.prever(MlPredictRequest.from(r));

        Predicao predicao = new Predicao(
                r.maquinaId().trim(), r.tipoProduto(),
                r.temperaturaArK(), r.temperaturaProcessoK(),
                r.rotacaoRpm(), r.torqueNm(), r.desgasteFerramentaMin()
        );
        predicao.registrarResultado(ml.probabilidadeFalha(), ml.tipoFalhaPrevisto(), ml.versaoModelo());

        return PredicaoResponse.from(repository.save(predicao));
    }

    @Transactional(readOnly = true)
    public PredicaoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(PredicaoResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Predição " + id + " não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<PredicaoResponse> listar(String maquinaId) {
        List<Predicao> resultado = (maquinaId == null || maquinaId.isBlank())
                ? repository.findTop50ByOrderByCriadoEmDesc()
                : repository.findTop50ByMaquinaIdOrderByCriadoEmDesc(maquinaId.trim());
        return resultado.stream().map(PredicaoResponse::from).toList();
    }
}
