package br.edu.biopark.manutencao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Contrato do POST /predict do serviço Python (snake_case). */
public record MlPredictRequest(
        @JsonProperty("tipo_produto") String tipoProduto,
        @JsonProperty("temperatura_ar_k") BigDecimal temperaturaArK,
        @JsonProperty("temperatura_processo_k") BigDecimal temperaturaProcessoK,
        @JsonProperty("rotacao_rpm") Integer rotacaoRpm,
        @JsonProperty("torque_nm") BigDecimal torqueNm,
        @JsonProperty("desgaste_ferramenta_min") Integer desgasteFerramentaMin
) {

    public static MlPredictRequest from(LeituraRequest r) {
        return new MlPredictRequest(
                r.tipoProduto(), r.temperaturaArK(), r.temperaturaProcessoK(),
                r.rotacaoRpm(), r.torqueNm(), r.desgasteFerramentaMin()
        );
    }
}
