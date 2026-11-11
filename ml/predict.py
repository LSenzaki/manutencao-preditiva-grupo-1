"""Inferência via linha de comando (chamado pelo Java com ProcessBuilder).

Contrato (ver README.md): imprime UMA única linha de JSON no stdout:
{"probabilidade_falha": 0.05, "tipo_falha_previsto": "NONE", "versao_modelo": "v1"}
Erros vão para o stderr com código de saída 1. Nenhum outro print.
"""
import argparse
import json
import sys
import warnings
from pathlib import Path

warnings.filterwarnings("ignore")

MODELO = Path(__file__).resolve().parent / "models" / "modelo.pkl"
VERSAO_MODELO = "v1"  # até 20 caracteres


class Parser(argparse.ArgumentParser):
    def error(self, message):  # argparse sai com 2 por padrão; o contrato pede 1
        print(f"Erro nos argumentos: {message}", file=sys.stderr)
        sys.exit(1)


def ler_args():
    p = Parser(description="Previsão de falha (AI4I 2020)")
    p.add_argument("--tipo-produto", required=True, choices=["L", "M", "H"])
    p.add_argument("--temperatura-ar-k", required=True, type=float)
    p.add_argument("--temperatura-processo-k", required=True, type=float)
    p.add_argument("--rotacao-rpm", required=True, type=float)
    p.add_argument("--torque-nm", required=True, type=float)
    p.add_argument("--desgaste-ferramenta-min", required=True, type=float)
    return p.parse_args()


def main() -> None:
    args = ler_args()
    try:
        import joblib
        import pandas as pd

        if not MODELO.exists():
            raise FileNotFoundError(f"Modelo não encontrado: {MODELO}. Rode: python train.py")
        modelo = joblib.load(MODELO)

        X = pd.DataFrame([{
            "tipo_produto": args.tipo_produto,
            "temperatura_ar_k": args.temperatura_ar_k,
            "temperatura_processo_k": args.temperatura_processo_k,
            "rotacao_rpm": args.rotacao_rpm,
            "torque_nm": args.torque_nm,
            "desgaste_ferramenta_min": args.desgaste_ferramenta_min,
        }])

        probas = dict(zip(modelo.classes_, modelo.predict_proba(X)[0]))
        resultado = {
            "probabilidade_falha": round(float(1.0 - probas.get("NONE", 0.0)), 4),
            "tipo_falha_previsto": str(modelo.predict(X)[0]),
            "versao_modelo": VERSAO_MODELO,
        }
    except Exception as e:
        print(f"Erro na predição: {e}", file=sys.stderr)
        sys.exit(1)

    print(json.dumps(resultado))


if __name__ == "__main__":
    main()
