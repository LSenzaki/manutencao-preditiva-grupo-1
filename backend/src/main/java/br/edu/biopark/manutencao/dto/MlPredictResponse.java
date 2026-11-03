package br.edu.biopark.manutencao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record MlPredictResponse(
        @JsonProperty("probabilidade_falha") BigDecimal probabilidadeFalha,
        @JsonProperty("tipo_falha_previsto") String tipoFalhaPrevisto,
        @JsonProperty("versao_modelo") String versaoModelo
) {
}
