"""Treina o modelo de manutenção preditiva (AI4I 2020) e salva em models/modelo.pkl.

Uso: python train.py
Requer scikit-learn==1.5.2 (ver requirements.txt) para o .pkl carregar em outras máquinas.

Alvo: tipo de falha (multiclasse) -> NONE, TWF, HDF, PWF, OSF, RNF.
probabilidade_falha = 1 - P(NONE), calculado no predict.py.
"""
from datetime import datetime
from pathlib import Path

import joblib
import pandas as pd
import sklearn
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder

BASE = Path(__file__).resolve().parent
CSV = BASE / "data" / "ai4i2020.csv"
SAIDA = BASE / "models" / "modelo.pkl"
RELATORIOS = BASE / "reports"

COLUNAS = {
    "Type": "tipo_produto",
    "Air temperature [K]": "temperatura_ar_k",
    "Process temperature [K]": "temperatura_processo_k",
    "Rotational speed [rpm]": "rotacao_rpm",
    "Torque [Nm]": "torque_nm",
    "Tool wear [min]": "desgaste_ferramenta_min",
}
CATEGORICAS = ["tipo_produto"]
NUMERICAS = [
    "temperatura_ar_k",
    "temperatura_processo_k",
    "rotacao_rpm",
    "torque_nm",
    "desgaste_ferramenta_min",
]
FEATURES = CATEGORICAS + NUMERICAS
TIPOS_FALHA = ["TWF", "HDF", "PWF", "OSF", "RNF"]


def tipo_falha(linha: pd.Series) -> str:
    """Primeiro tipo de falha marcado na linha; NONE se nenhum."""
    for t in TIPOS_FALHA:
        if linha[t] == 1:
            return t
    return "NONE"


def gerar_resultados(pipeline, y, y_te, pred, metricas) -> str:
    classes = list(pipeline.classes_)
    cm = confusion_matrix(y_te, pred, labels=classes)

    L = [
        "# Resultados do treino",
        "",
        f"- Data: {datetime.now():%Y-%m-%d %H:%M}",
        f"- scikit-learn: {sklearn.__version__}",
        f"- Modelo: RandomForestClassifier (n_estimators=200, class_weight=balanced)",
        f"- Linhas: {len(y)} (treino {len(y) - len(y_te)} / teste {len(y_te)})",
        f"- Acurácia no teste: {metricas['accuracy']:.4f}",
        f"- F1 macro: {metricas['macro avg']['f1-score']:.4f}",
        "",
        "> A acurácia é inflada pelo desbalanceamento (a classe NONE domina). "
        "Use recall e F1 por classe.",
        "",
        "## Distribuição das classes (dataset inteiro)",
        "",
        "| Classe | Linhas |",
        "|---|---|",
    ]
    L += [f"| {c} | {n} |" for c, n in y.value_counts().items()]

    L += ["", "## Métricas por classe (teste)", "",
          "| Classe | Precisão | Recall | F1 | Suporte |", "|---|---|---|---|---|"]
    for c in classes:
        m = metricas[c]
        L.append(f"| {c} | {m['precision']:.2f} | {m['recall']:.2f} | "
                 f"{m['f1-score']:.2f} | {int(m['support'])} |")

    L += ["", "## Matriz de confusão (linhas = real, colunas = previsto)", "",
          "| real \\ previsto | " + " | ".join(classes) + " |",
          "|---|" + "---|" * len(classes)]
    for c, linha in zip(classes, cm):
        L.append(f"| {c} | " + " | ".join(str(int(v)) for v in linha) + " |")

    nomes = [n.split("__", 1)[-1] for n in pipeline["prep"].get_feature_names_out()]
    imp = sorted(zip(nomes, pipeline["clf"].feature_importances_), key=lambda x: -x[1])
    L += ["", "## Importância das features", "", "| Feature | Importância |", "|---|---|"]
    L += [f"| {n} | {v:.3f} |" for n, v in imp]
    return "\n".join(L) + "\n"


def gerar_sugestoes(metricas) -> str:
    fracas = [c for c in TIPOS_FALHA + ["NONE"]
              if c in metricas and metricas[c]["recall"] < 0.6]
    L = ["# Sugestões para mudar os resultados", ""]

    L += ["## Pontos fracos deste treino", ""]
    if fracas:
        for c in fracas:
            m = metricas[c]
            nota = " (falha aleatória no dataset: praticamente imprevisível)" if c == "RNF" else ""
            L.append(f"- **{c}**: recall {m['recall']:.2f}, precisão {m['precision']:.2f}, "
                     f"suporte {int(m['support'])}{nota}")
    else:
        L.append("- Nenhuma classe com recall abaixo de 0.60.")

    L += [
        "",
        "## Sugestões (da que costuma ajudar mais para a que ajuda menos)",
        "",
        "1. **Features derivadas** (calculadas dentro do pipeline, sem mudar o contrato do predict.py): "
        "`temperatura_processo_k - temperatura_ar_k` (HDF), `torque_nm * rotacao_rpm` "
        "(potência, PWF) e `desgaste_ferramenta_min * torque_nm` (OSF). "
        "Cada tipo de falha do AI4I é definido por esses cálculos; árvores têm dificuldade de aprendê-los sozinhas.",
        "2. **Validação cruzada estratificada** (`StratifiedKFold`, 5 folds): com 4 a 20 exemplos por classe rara "
        "no teste, uma única divisão dá métricas instáveis.",
        "3. **Ajustar o limiar de decisão**: em vez de `predict`, usar `predict_proba` e dar um corte menor "
        "para falhas (ex.: prever falha se `1 - P(NONE) > 0.3`). Sobe o recall à custa de precisão.",
        "4. **Hiperparâmetros** do RandomForest: `max_depth`, `min_samples_leaf`, `n_estimators`, "
        "`class_weight='balanced_subsample'` (usar `GridSearchCV` com `scoring='f1_macro'`).",
        "5. **Outro algoritmo**: `HistGradientBoostingClassifier` (já vem no scikit-learn 1.5.2) "
        "ou `GradientBoostingClassifier`.",
        "6. **Reamostragem** (SMOTE, `imbalanced-learn`): exige nova dependência no requirements.txt, "
        "então só vale se as anteriores não bastarem.",
        "7. **Simplificar o alvo**: se TWF e RNF seguirem em 0, considerar agrupá-las ou tratar o modelo "
        "como binário (falha / sem falha), que é o que o campo `probabilidade_falha` realmente usa.",
        "",
        "> Ao mudar o modelo, mantenha scikit-learn 1.5.2 e refaça o treino para gerar um novo `modelo.pkl`. "
        "O contrato do `predict.py` (README) não muda.",
    ]
    return "\n".join(L) + "\n"


def main() -> None:
    if not CSV.exists():
        raise SystemExit(f"Dataset não encontrado: {CSV}. Rode: python download_data.py")

    df = pd.read_csv(CSV, encoding="utf-8-sig")
    y = df.apply(tipo_falha, axis=1)
    X = df.rename(columns=COLUNAS)[FEATURES]

    X_tr, X_te, y_tr, y_te = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    pipeline = Pipeline([
        ("prep", ColumnTransformer(
            [("tipo", OneHotEncoder(handle_unknown="ignore"), CATEGORICAS)],
            remainder="passthrough",
        )),
        ("clf", RandomForestClassifier(
            n_estimators=200, class_weight="balanced", random_state=42, n_jobs=-1
        )),
    ])
    pipeline.fit(X_tr, y_tr)

    pred = pipeline.predict(X_te)
    print(f"scikit-learn {sklearn.__version__}")
    print(classification_report(y_te, pred, zero_division=0))

    SAIDA.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(pipeline, SAIDA)
    print(f"Modelo salvo em {SAIDA}")

    RELATORIOS.mkdir(parents=True, exist_ok=True)
    metricas = classification_report(y_te, pred, zero_division=0, output_dict=True)
    (RELATORIOS / "resultados_treino.md").write_text(
        gerar_resultados(pipeline, y, y_te, pred, metricas), encoding="utf-8")
    (RELATORIOS / "sugestoes_melhoria.md").write_text(
        gerar_sugestoes(metricas), encoding="utf-8")
    print(f"Relatórios salvos em {RELATORIOS}")


if __name__ == "__main__":
    main()
