# Manutenção Preditiva de Máquinas

Projeto final de Microsserviços. API em Spring Boot, modelo de ML em Python
(chamado pelo Java via `ProcessBuilder`) e PostgreSQL para o histórico, orquestrados com Docker Compose.
Dataset: AI4I 2020 Predictive Maintenance (UCI).

## Integrantes e papéis
- **Tech Lead / DevOps:** Lucas
- **Engenheiros de Dados / MLOps:** Oliver e Daniel
- **Engenheiros de Backend (Java):** Eugenio, Julio e Oliver

## Subir
```bash
docker compose up
```

- No primeiro uso, o Compose constrói as imagens e baixa as dependências; depois, basta `docker compose up`.
- O Compose usa valores locais de demonstração por padrão; `.env` é opcional.
- A API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Saúde do backend: http://localhost:8080/actuator/health
- Confira `docker compose ps` e aguarde o backend ficar `healthy` antes de enviar a primeira requisição.
- Postgres no host: `localhost:5433` (para não colidir com um Postgres instalado na máquina)

Para testar uma previsão, envie o JSON de exemplo de `backend/requisicoes.http` para `POST /api/predicoes`.
Uma leitura válida retorna `200 OK`, a probabilidade e o tipo de falha previsto; o histórico fica disponível em `GET /api/predicoes`.

## Estrutura
- `backend/`: Spring Boot (validação, persistência, orquestração)
- `ml/`: `train.py` e `predict.py` (linha de comando) — contrato em `ml/README.md`
- `db/init.sql`: schema do histórico
- `docs/arquitetura.mmd`: diagrama (importável no Excalidraw)

## Integração da previsão
- O backend executa `ml/predict.py` localmente via `ProcessBuilder`; o Docker instala Python e as dependências do modelo na imagem do backend.
- O contrato atual usa somente tipo do produto, temperaturas do ar e do processo, rotação, torque e desgaste da ferramenta, conforme o treino AI4I 2020.
- O `POST /api/predicoes` valida os campos, obtém a previsão do modelo e persiste entrada e resultado no PostgreSQL. Não há captura direta de sensores: o cliente envia os valores no JSON.

## Branches
- `feature/api-base`: Backend (Java)
- `feature/ml-model`: Dados / MLOps (Python)
- `main`: só recebe merge via Pull Request revisado pelo Tech Lead

## Problemas comuns
- `Cannot connect to the Docker daemon` → abrir o Docker Desktop.
- Trocou a senha no `.env` e o banco recusa → o volume guarda a senha antiga: `docker compose down -v`.
- Sem Maven instalado → rode pela IDE ou deixe o `docker compose up --build` compilar.
