# Manutenção Preditiva de Máquinas

Projeto final de Microsserviços. API em Spring Boot, modelo de ML em Python
(chamado pelo Java via `ProcessBuilder`) e PostgreSQL para o histórico, orquestrados com Docker Compose.
Dataset: AI4I 2020 Predictive Maintenance (UCI).

## Subir
```bash
cp .env.example .env
docker compose up --build
```

- Swagger: http://localhost:8080/swagger-ui.html
- Postgres no host: `localhost:5433` (para não colidir com um Postgres instalado na máquina)

## Estrutura
- `backend/`: Spring Boot (validação, persistência, orquestração)
- `ml/`: `train.py` e `predict.py` (linha de comando) — contrato em `ml/README.md`
- `db/init.sql`: schema do histórico
- `docs/arquitetura.mmd`: diagrama (importável no Excalidraw)

## Branches
- `feature/api-base`: Backend (Java)
- `feature/ml-model`: Dados / MLOps (Python)
- `main`: só recebe merge via Pull Request revisado pelo Tech Lead

## Problemas comuns
- `Cannot connect to the Docker daemon` → abrir o Docker Desktop.
- Trocou a senha no `.env` e o banco recusa → o volume guarda a senha antiga: `docker compose down -v`.
- Sem Maven instalado → rode pela IDE ou deixe o `docker compose up --build` compilar.
