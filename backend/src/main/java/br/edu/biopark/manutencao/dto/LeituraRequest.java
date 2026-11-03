package br.edu.biopark.manutencao.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Leitura de sensores enviada pelo cliente.
 * Toda validação acontece aqui, no Java, antes de gastar uma chamada ao serviço de ML.
 */
public record LeituraRequest(

        @Schema(example = "MAQ-001")
        @NotBlank(message = "maquinaId não pode ser vazio ou em branco")
        @Size(max = 50)
        String maquinaId,

        @Schema(example = "M", description = "Qualidade do produto: L (baixa), M (média), H (alta)")
        @NotNull
        @Pattern(regexp = "[LMH]", message = "deve ser L, M ou H")
        String tipoProduto,

        @Schema(example = "298.1")
        @NotNull
        @DecimalMin("250.0") @DecimalMax("350.0")
        BigDecimal temperaturaArK,

        @Schema(example = "308.6")
        @NotNull
        @DecimalMin("250.0") @DecimalMax("400.0")
        BigDecimal temperaturaProcessoK,

        @Schema(example = "1551")
        @NotNull
        @Min(1) @Max(5000)
        Integer rotacaoRpm,

        @Schema(example = "42.8")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("200.0")
        BigDecimal torqueNm,

        @Schema(example = "0")
        @NotNull
        @Min(0) @Max(500)
        Integer desgasteFerramentaMin
) {

    /** Regra entre campos: o processo não pode estar mais frio que o ambiente. */
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "temperaturaProcessoK deve ser maior ou igual a temperaturaArK")
    public boolean isTemperaturasCoerentes() {
        if (temperaturaArK == null || temperaturaProcessoK == null) {
            return true; // @NotNull já reporta
        }
        return temperaturaProcessoK.compareTo(temperaturaArK) >= 0;
    }
}
