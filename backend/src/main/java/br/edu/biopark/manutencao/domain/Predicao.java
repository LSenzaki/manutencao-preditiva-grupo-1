package br.edu.biopark.manutencao.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "predicao")
public class Predicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "maquina_id", nullable = false, length = 50)
    private String maquinaId;

    @Column(name = "tipo_produto", nullable = false, length = 1)
    private String tipoProduto;

    @Column(name = "temperatura_ar_k", nullable = false, precision = 6, scale = 2)
    private BigDecimal temperaturaArK;

    @Column(name = "temperatura_processo_k", nullable = false, precision = 6, scale = 2)
    private BigDecimal temperaturaProcessoK;

    @Column(name = "rotacao_rpm", nullable = false)
    private Integer rotacaoRpm;

    @Column(name = "torque_nm", nullable = false, precision = 6, scale = 2)
    private BigDecimal torqueNm;

    @Column(name = "desgaste_ferramenta_min", nullable = false)
    private Integer desgasteFerramentaMin;

    @Column(name = "probabilidade_falha", precision = 5, scale = 4)
    private BigDecimal probabilidadeFalha;

    @Column(name = "tipo_falha_previsto", length = 10)
    private String tipoFalhaPrevisto;

    @Column(name = "versao_modelo", length = 20)
    private String versaoModelo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Predicao() {
        // exigido pelo JPA
    }

    public Predicao(String maquinaId, String tipoProduto, BigDecimal temperaturaArK,
                    BigDecimal temperaturaProcessoK, Integer rotacaoRpm, BigDecimal torqueNm,
                    Integer desgasteFerramentaMin) {
        this.maquinaId = maquinaId;
        this.tipoProduto = tipoProduto;
        this.temperaturaArK = temperaturaArK;
        this.temperaturaProcessoK = temperaturaProcessoK;
        this.rotacaoRpm = rotacaoRpm;
        this.torqueNm = torqueNm;
        this.desgasteFerramentaMin = desgasteFerramentaMin;
    }

    public void registrarResultado(BigDecimal probabilidadeFalha, String tipoFalhaPrevisto, String versaoModelo) {
        this.probabilidadeFalha = probabilidadeFalha;
        this.tipoFalhaPrevisto = tipoFalhaPrevisto;
        this.versaoModelo = versaoModelo;
    }

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public String getMaquinaId() { return maquinaId; }
    public String getTipoProduto() { return tipoProduto; }
    public BigDecimal getTemperaturaArK() { return temperaturaArK; }
    public BigDecimal getTemperaturaProcessoK() { return temperaturaProcessoK; }
    public Integer getRotacaoRpm() { return rotacaoRpm; }
    public BigDecimal getTorqueNm() { return torqueNm; }
    public Integer getDesgasteFerramentaMin() { return desgasteFerramentaMin; }
    public BigDecimal getProbabilidadeFalha() { return probabilidadeFalha; }
    public String getTipoFalhaPrevisto() { return tipoFalhaPrevisto; }
    public String getVersaoModelo() { return versaoModelo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
