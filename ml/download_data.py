"""Baixa o dataset AI4I 2020 (UCI #601) para ml/data/ai4i2020.csv.

Uso: python download_data.py [--force]
Usa apenas a biblioteca padrão. O CSV não é versionado (ver .gitignore).
"""
import argparse
import io
import sys
import urllib.request
import zipfile
from pathlib import Path

URL = "https://archive.ics.uci.edu/static/public/601/ai4i+2020+predictive+maintenance+dataset.zip"
DESTINO = Path(__file__).resolve().parent / "data" / "ai4i2020.csv"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--force", action="store_true", help="baixa novamente mesmo se o arquivo existir")
    args = parser.parse_args()

    if DESTINO.exists() and not args.force:
        print(f"{DESTINO} já existe (use --force para sobrescrever).")
        return 0

    try:
        req = urllib.request.Request(URL, headers={"User-Agent": "python-urllib"})
        with urllib.request.urlopen(req, timeout=60) as resp:
            conteudo = resp.read()
        with zipfile.ZipFile(io.BytesIO(conteudo)) as zf:
            csvs = [n for n in zf.namelist() if n.lower().endswith(".csv")]
            if not csvs:
                print("Nenhum CSV encontrado no zip baixado.", file=sys.stderr)
                return 1
            DESTINO.parent.mkdir(parents=True, exist_ok=True)
            DESTINO.write_bytes(zf.read(csvs[0]))
    except Exception as e:
        print(f"Falha ao baixar o dataset: {e}\nBaixe manualmente em https://archive.ics.uci.edu/dataset/601 "
              f"e salve em {DESTINO}", file=sys.stderr)
        return 1

    print(f"Dataset salvo em {DESTINO}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
