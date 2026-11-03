"""Serviço de inferência — Manutenção Preditiva (AI4I 2020).

Enquanto o modelo não estiver treinado, /predict retorna uma regra
heurística para que o backend Java já consiga integrar.
"""
from pathlib import Path
from typing import Literal

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="ML Service - Manutenção Preditiva", version="0.1.0")

MODEL_PATH = Path(__file__).parent.parent / "models" / "modelo.joblib"
model = None
if MODEL_PATH.exists():
    import joblib
    model = joblib.load(MODEL_PATH)


class Leitura(BaseModel):
    tipo_produto: Literal["L", "M", "H"]
    temperatura_ar_k: float
    temperatura_processo_k: float
    rotacao_rpm: int
    torque_nm: float
    desgaste_ferramenta_min: int


class Predicao(BaseModel):
    probabilidade_falha: float
    tipo_falha_previsto: str
    versao_modelo: str


@app.get("/health")
def health():
    return {"status": "ok", "modelo_carregado": model is not None}


@app.post("/predict", response_model=Predicao)
def predict(leitura: Leitura):
    if model is None:
        # Stub: regras inspiradas nas falhas do AI4I
        potencia_w = leitura.torque_nm * leitura.rotacao_rpm * 2 * 3.14159 / 60
        delta_t = leitura.temperatura_processo_k - leitura.temperatura_ar_k
        if leitura.desgaste_ferramenta_min >= 200:
            return Predicao(probabilidade_falha=0.8, tipo_falha_previsto="TWF", versao_modelo="stub")
        if delta_t < 8.6 and leitura.rotacao_rpm < 1380:
            return Predicao(probabilidade_falha=0.9, tipo_falha_previsto="HDF", versao_modelo="stub")
        if potencia_w < 3500 or potencia_w > 9000:
            return Predicao(probabilidade_falha=0.85, tipo_falha_previsto="PWF", versao_modelo="stub")
        return Predicao(probabilidade_falha=0.05, tipo_falha_previsto="NONE", versao_modelo="stub")

    # TODO (MLOps): montar DataFrame com as mesmas features do treino
    raise NotImplementedError
