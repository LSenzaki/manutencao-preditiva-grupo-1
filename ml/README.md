# ML — Manutenção Preditiva (AI4I 2020)

Responsável: Engenharia de Dados / MLOps. Branch: `feature/ml-model`.

## Estrutura
```
ml/
├── data/          # ai4i2020.csv (NÃO vai para o git)
├── models/        # modelo.pkl gerado pelo train.py
├── notebooks/     # exploração
├── download_data.py  # baixa o dataset da UCI
├── train.py       # treina e salva models/modelo.pkl
├── predict.py     # inferência via linha de comando
└── requirements.txt
```

## Setup
```bash
cd ml
python3.12 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
```
Dataset (não versionado): rodar `python download_data.py` (baixa de https://archive.ics.uci.edu/dataset/601 para `ml/data/ai4i2020.csv`; `--force` para baixar de novo).
Treinar com o scikit-learn do `requirements.txt` (1.5.2), senão o `.pkl` pode não carregar em outra máquina.

## Avaliar o modelo salvo sem retreinar

Com o dataset baixado e as dependências instaladas, rode `python evaluate.py`.
O script carrega `models/modelo.pkl`, recria a divisão de teste estratificada
usada em `train.py` (20%, `random_state=42`) e grava
`reports/avaliacao_modelo.md`. Ele não treina nem sobrescreve o modelo.
O relatório inclui métricas por classe, F1 macro, balanced accuracy, matriz
de confusão e métricas binárias de risco no limiar 0.50, incluindo recall,
PR-AUC e falsos negativos.

## Contrato do predict.py (o Java depende disso — não mudar sem avisar o backend)

O Java vai chamar via `ProcessBuilder`:
```bash
python predict.py --tipo-produto M --temperatura-ar-k 298.1 --temperatura-processo-k 308.6 \
                  --rotacao-rpm 1551 --torque-nm 42.8 --desgaste-ferramenta-min 0
```

Saída: **uma única linha** de JSON no stdout, nada mais:
```json
{"probabilidade_falha": 0.05, "tipo_falha_previsto": "NONE", "versao_modelo": "v1"}
```

- `probabilidade_falha`: número entre 0 e 1
- `tipo_falha_previsto`: `TWF`, `HDF`, `PWF`, `OSF`, `RNF` ou `NONE` (falha mais provável quando `probabilidade_falha` >= 0.5; senão `NONE`)
- `versao_modelo`: até 20 caracteres

Regras de higiene:
- Proibido `print` de debug ("Carregando modelo...") — quebra o parser do Java.
- Erros vão para o **stderr** com código de saída diferente de 0 (`sys.exit(1)`).
- Warnings do sklearn/pandas vão para o stderr; mesmo assim, silencie com `warnings.filterwarnings("ignore")`.
