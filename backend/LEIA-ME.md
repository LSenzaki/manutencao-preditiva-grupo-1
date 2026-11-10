# API Java de manutenção preditiva

A API usa Spring Boot 3.5, Java 21 e Maven. O projeto tem grupo
`br.edu.biopark` e artefato `manutencao-api`. O Spring Web recebe as requisições,
o Bean Validation valida as leituras, o Spring Data JPA grava o histórico no
PostgreSQL e o Spring Boot Actuator expõe a saúde da aplicação. O Springdoc
gera o contrato OpenAPI e a interface Swagger.

## Estrutura

| Pacote | Responsabilidade |
| --- | --- |
| `controller` | Expõe as rotas e documenta as operações e respostas HTTP. |
| `service` | Processa as leituras e coordena a gravação e a consulta do histórico. |
| `repository` | Acessa o histórico de predições no PostgreSQL. |
| `dto` | Define os dados recebidos e devolvidos pela API. |

## Executar com Docker

Na raiz do repositório, com o Docker Engine em execução, use as configurações
de exemplo para a demonstração local.

```bash
docker compose --env-file .env.example up --build -d
docker compose --env-file .env.example ps
```

O Compose aguarda o PostgreSQL ficar saudável e publica a API na porta
`8080`. O Dockerfile compila o JAR com Maven e roda a aplicação em uma imagem
Java 21 JRE Alpine.

- [Saúde da API](http://localhost:8080/actuator/health)
- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [Contrato OpenAPI em JSON](http://localhost:8080/v3/api-docs)

Para executar o Java pela IDE, suba só o banco com
`docker compose --env-file .env.example up -d postgres` e execute a classe
`ManutencaoApiApplication` com Java 21. A configuração local padrão do backend
usa a porta `5433` do PostgreSQL.

## Contrato HTTP

| Método e rota | Respostas |
| --- | --- |
| `POST /api/predicoes` | `201` com a predição salva e seu endereço no cabeçalho `Location`, `400` para entrada inválida e `503` quando a predição está indisponível. |
| `GET /api/predicoes/{id}` | `200` com a predição ou `404` se o ID não existir. |
| `GET /api/predicoes` | `200` com até as 50 predições mais recentes. Use `?maquinaId=MAQ-001` para filtrar por máquina. |

O DTO `LeituraRequest` recebe o identificador da máquina, o tipo do produto,
as temperaturas do ar e do processo em kelvin, a rotação em RPM, o torque em
N·m e o desgaste da ferramenta em minutos. `PredicaoResponse` devolve a
leitura com o ID, a probabilidade, o tipo de falha, o nível de risco, a versão
do modelo e a data de criação.

## Demonstrar a base Java

1. Abra a rota de saúde e confira o status `UP`.
2. No Swagger, execute a listagem e confira o `200`. Em um banco sem
   predições, a resposta é `[]`.
3. Consulte um ID que não exista e confira o `404`.
4. Envie uma leitura com `maquinaId` em branco e confira o `400`.

O `201` depende da integração de ML. Enquanto a predição estiver
indisponível, uma leitura válida retorna `503`. Há exemplos prontos em
[requisicoes.http](requisicoes.http).
