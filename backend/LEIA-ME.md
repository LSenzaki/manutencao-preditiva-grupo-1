Backend Spring Boot 3.5 / Java 21.

Configuração equivalente no Spring Initializr (para mostrar ao professor):
Maven | Java 21 | Group br.edu.biopark | Artifact manutencao-api
Dependências: Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Spring Boot Actuator.
O springdoc-openapi (Swagger) foi adicionado manualmente no pom.xml.

Rodar local (fora do Docker): suba `docker compose up postgres` e rode a classe
ManutencaoApiApplication pela IDE. Requisições de teste em requisicoes.http.
