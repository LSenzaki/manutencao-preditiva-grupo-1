package br.edu.biopark.manutencao.dto;

import br.edu.biopark.manutencao.domain.Predicao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PredicaoResponse(
        Long id,
        String maquinaId,
        String tipoProduto,
        BigDecimal temperaturaArK,
        BigDecimal temperaturaProcessoK,
        Integer rotacaoRpm,
        BigDecimal torqueNm,
        Integer desgasteFerramentaMin,
        BigDecimal probabilidadeFalha,
        String tipoFalhaPrevisto,
        String nivelRisco,
        String versaoModelo,
        LocalDateTime criadoEm
) {

    public static PredicaoResponse from(Predicao p) {
        return new PredicaoResponse(
                p.getId(), p.getMaquinaId(), p.getTipoProduto(),
                p.getTemperaturaArK(), p.getTemperaturaProcessoK(),
                p.getRotacaoRpm(), p.getTorqueNm(), p.getDesgasteFerramentaMin(),
                p.getProbabilidadeFalha(), p.getTipoFalhaPrevisto(),
                nivelRisco(p.getProbabilidadeFalha()),
                p.getVersaoModelo(), p.getCriadoEm()
        );
    }

    private static String nivelRisco(BigDecimal prob) {
        if (prob == null) return "DESCONHECIDO";
        if (prob.compareTo(new BigDecimal("0.5")) >= 0) return "ALTO";
        if (prob.compareTo(new BigDecimal("0.2")) >= 0) return "MEDIO";
        return "BAIXO";
    }
}
