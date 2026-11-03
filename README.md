# Manutenção Preditiva de Máquinas

Projeto final de Microsserviços. API em Spring Boot, modelo de ML em Python (FastAPI)
e PostgreSQL para o histórico, orquestrados com Docker Compose.
Dataset: AI4I 2020 Predictive Maintenance (UCI).

## Subir
```bash
cp .env.example .env
docker compose up --build
```
Hoje, antes do backend existir: `docker compose up postgres ml-service`

- Swagger: http://localhost:8080/swagger-ui.html
- ML Service: http://localhost:8000/docs

## Estrutura
- `backend/`: Spring Boot (validação, persistência, orquestração)
- `ml-service/`: FastAPI e o modelo
- `db/init.sql`: schema do histórico
- `docs/arquitetura.mmd`: diagrama (importável no Excalidraw)
