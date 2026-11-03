-- Histórico de leituras de sensores e predições (campos do AI4I 2020)
CREATE TABLE IF NOT EXISTS predicao (
    id                      BIGSERIAL PRIMARY KEY,
    maquina_id              VARCHAR(50)  NOT NULL,
    tipo_produto            VARCHAR(1)   NOT NULL CHECK (tipo_produto IN ('L','M','H')),
    temperatura_ar_k        NUMERIC(6,2) NOT NULL,
    temperatura_processo_k  NUMERIC(6,2) NOT NULL,
    rotacao_rpm             INTEGER      NOT NULL,
    torque_nm               NUMERIC(6,2) NOT NULL,
    desgaste_ferramenta_min INTEGER      NOT NULL,
    probabilidade_falha     NUMERIC(5,4),
    tipo_falha_previsto     VARCHAR(10),          -- TWF, HDF, PWF, OSF, RNF ou NONE
    versao_modelo           VARCHAR(20),
    criado_em               TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_predicao_maquina ON predicao (maquina_id, criado_em DESC);
