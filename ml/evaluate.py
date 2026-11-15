"""Avalia o modelo salvo sem treinar nem sobrescrever o artefato.

Uso: python evaluate.py [--dataset data/ai4i2020.csv]
O conjunto de teste replica a divisao estratificada usada por train.py.
"""
import argparse
import warnings
from pathlib import Path

import joblib
import pandas as pd
import sklearn
from sklearn.metrics import (
    average_precision_score,
    balanced_accuracy_score,
    brier_score_loss,
    classification_report,
    confusion_matrix,
    roc_auc_score,
)
from sklearn.model_selection import train_test_split

from train import BASE, COLUNAS, FEATURES, tipo_falha

VERSAO_ESPERADA = "1.5.2"
MODELO = BASE / "models" / "modelo.pkl"
RELATORIO = BASE / "reports" / "avaliacao_modelo.md"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dataset", type=Path, default=BASE / "data" / "ai4i2020.csv")
    args = parser.parse_args()

    if sklearn.__version__ != VERSAO_ESPERADA:
        raise SystemExit(
            f"Use scikit-learn=={VERSAO_ESPERADA} para carregar o modelo; "
            f"ambiente atual: {sklearn.__version__}."
        )
    if not args.dataset.exists():
        raise SystemExit(f"Dataset nao encontrado: {args.dataset}. Rode python download_data.py")
    if not MODELO.exists():
        raise SystemExit(f"Modelo nao encontrado: {MODELO}")

    dados = pd.read_csv(args.dataset, encoding="utf-8-sig")
    alvo = dados.apply(tipo_falha, axis=1)
    entradas = dados.rename(columns=COLUNAS)[FEATURES]
    _, teste, _, y_teste = train_test_split(
        entradas, alvo, test_size=0.2, random_state=42, stratify=alvo
    )

    # O artefato e carregado e avaliado como esta; nao ha fit nem dump neste script.
    with warnings.catch_warnings():
        warnings.simplefilter("error")
        modelo = joblib.load(MODELO)

    classes = list(modelo.classes_)
    previsto = modelo.predict(teste)
    probabilidades = modelo.predict_proba(teste)
    probabilidades_por_classe = dict(zip(classes, probabilidades.T))
    risco = 1.0 - probabilidades_por_classe.get("NONE", 0.0)
    falha_real = (y_teste != "NONE").astype(int)
    falha_prevista = (risco >= 0.5).astype(int)

    relatorio = classification_report(
        y_teste, previsto, labels=classes, output_dict=True, zero_division=0
    )
    matriz_multiclasse = confusion_matrix(y_teste, previsto, labels=classes)
    matriz_binaria = confusion_matrix(falha_real, falha_prevista, labels=[0, 1])
    tn, fp, fn, tp = matriz_binaria.ravel()
    especificidade = tn / (tn + fp) if (tn + fp) else 0.0
    metricas_binarias = {
        "precision": relatorio_binario(falha_real, falha_prevista, "precision"),
        "recall": relatorio_binario(falha_real, falha_prevista, "recall"),
        "f1": relatorio_binario(falha_real, falha_prevista, "f1"),
        "specificity": especificidade,
        "average_precision": average_precision_score(falha_real, risco),
        "roc_auc": roc_auc_score(falha_real, risco),
        "brier": brier_score_loss(falha_real, risco),
    }

    linhas = [
        "# Avaliacao do modelo de manutencao preditiva",
        "",
        f"- Data: {pd.Timestamp.now():%Y-%m-%d %H:%M}",
        f"- Dataset: {args.dataset}",
        f"- scikit-learn: {sklearn.__version__}",
        f"- Amostras no teste: {len(y_teste)}",
        f"- Positivas (falha): {int(falha_real.sum())}; negativas: {int((falha_real == 0).sum())}",
        "- Limiar binario: risco >= 0.50 (igual ao contrato de predict.py)",
        "- Avaliacao do artefato existente; nenhum treino ou salvamento do modelo foi feito.",
        "",
        "## Resultado multiclasse",
        "",
        f"- Acuracia: {relatorio['accuracy']:.4f}",
        f"- Balanced accuracy: {balanced_accuracy_score(y_teste, previsto):.4f}",
        f"- Precisao macro: {relatorio['macro avg']['precision']:.4f}",
        f"- Recall macro: {relatorio['macro avg']['recall']:.4f}",
        f"- F1 macro: {relatorio['macro avg']['f1-score']:.4f}",
        "",
        "| Classe | Precisao | Recall | F1 | Suporte |",
        "|---|---:|---:|---:|---:|",
    ]
    for classe in classes:
        m = relatorio[classe]
        linhas.append(
            f"| {classe} | {m['precision']:.4f} | {m['recall']:.4f} | "
            f"{m['f1-score']:.4f} | {int(m['support'])} |"
        )

    linhas += [
        "",
        "## Falha versus sem falha",
        "",
        f"- Precisao: {metricas_binarias['precision']:.4f}",
        f"- Recall (sensibilidade): {metricas_binarias['recall']:.4f}",
        f"- F1: {metricas_binarias['f1']:.4f}",
        f"- Especificidade: {metricas_binarias['specificity']:.4f}",
        f"- Average precision (PR-AUC): {metricas_binarias['average_precision']:.4f}",
        f"- ROC-AUC: {metricas_binarias['roc_auc']:.4f}",
        f"- Brier score (menor e melhor): {metricas_binarias['brier']:.4f}",
        f"- Verdadeiros positivos: {tp}; falsos negativos: {fn}; falsos positivos: {fp}; verdadeiros negativos: {tn}",
        "",
        "## Matrizes de confusao",
        "",
        "Multiclasse (linhas = real; colunas = previsto; ordem: " + ", ".join(classes) + "):",
        "",
        "```text",
    ]
    linhas.extend(" ".join(str(int(v)) for v in linha) for linha in matriz_multiclasse)
    linhas += ["```", "", "Binaria (linhas = real; colunas = previsto; ordem: sem falha, falha):", "", "```text"]
    linhas.extend(" ".join(str(int(v)) for v in linha) for linha in matriz_binaria)
    linhas += ["```", ""]

    RELATORIO.parent.mkdir(parents=True, exist_ok=True)
    RELATORIO.write_text("\n".join(linhas), encoding="utf-8")
    print("\n".join(linhas))
    print(f"Relatorio salvo em {RELATORIO}")


def relatorio_binario(real, previsto, metrica: str) -> float:
    from sklearn.metrics import precision_score, recall_score, f1_score

    funcoes = {"precision": precision_score, "recall": recall_score, "f1": f1_score}
    return float(funcoes[metrica](real, previsto, zero_division=0))


if __name__ == "__main__":
    main()
