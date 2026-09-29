# SavePoint

Aplicação para montar e acompanhar sua biblioteca de jogos. O usuário busca jogos (dados da [RAWG](https://rawg.io/apidocs)), adiciona à biblioteca, define status (`PLAYING`, `COMPLETED`, `DROPPED`, `WISHLIST`) e nota, e cada alteração fica registrada em um histórico.

O backend é composto por microsserviços Spring Boot que se comunicam de forma síncrona (Feign + Eureka) e assíncrona (eventos no RabbitMQ com *transactional outbox*), com traces e logs centralizados no Grafana.

## Arquitetura

| Módulo | Responsabilidade |
|---|---|
| `api` (gamelog) | Autenticação (JWT + refresh token), biblioteca de jogos do usuário, histórico de alterações e publicação de eventos via outbox |
| `catalogservice` | Busca e cache dos jogos da RAWG; resolve os dados de um jogo quando ele é adicionado |
| `discoveryserver` | Service discovery (Eureka) |
| `web` | Frontend React (login, cadastro, busca e biblioteca) |

### Fluxo de eventos

Exchanges do tipo *topic* no RabbitMQ, com retry e *dead letter queue* em cada fila:

1. A `api` grava a alteração do jogo e o evento na tabela `outbox_events` **na mesma transação**.
2. O `OutboxPublisher` publica os eventos pendentes a cada 2s na exchange `gamelog.game-events` (`game.added`, `game.<campo>.changed`, `game.deleted`).
3. `gamelog.game-history` consome as alterações e grava o histórico.
4. `catalogservice.game-added` recebe `game.added`, busca o jogo na RAWG e publica `catalog.game.resolved` em `catalogservice.catalog-events`.
5. `gamelog.catalog-game-resolved` atualiza nome e imagem do jogo na biblioteca.

Os consumidores são idempotentes, então reprocessar um evento não duplica dados. A leitura do outbox usa `SELECT ... FOR UPDATE SKIP LOCKED`, o que permite rodar várias réplicas da `api` sem publicar o mesmo evento duas vezes.

## Tecnologias

- **Backend:** Java 21, Spring Boot 4, Spring Security (OAuth2 Resource Server), Spring Data JPA, Spring Cloud (Eureka, OpenFeign), Spring AMQP
- **Banco:** PostgreSQL (Docker/Kubernetes) e H2 (execução local e testes)
- **Mensageria:** RabbitMQ
- **Frontend:** React 19, React Router, Vite
- **Observabilidade:** OpenTelemetry, Grafana Tempo (traces), Grafana Loki (logs), Grafana
- **Testes:** JUnit 5, Mockito, Spring Boot Test, Testcontainers, JaCoCo
- **Infra:** Docker, Docker Compose, Kubernetes (Kustomize, HPA), GitHub Actions, GHCR

## Pré-requisitos

- Docker Desktop
- Chave da API da RAWG ([rawg.io/apidocs](https://rawg.io/apidocs))
- Node.js 24 (frontend)
- Java 21 (só para rodar os serviços fora do Docker)

## Variáveis de ambiente

Copie o `.env.example` da raiz para `.env` e preencha:

| Variável | Descrição |
|---|---|
| `JWT_SECRET` | Chave de assinatura dos JWTs (HS256), com pelo menos 32 caracteres |
| `RAWG_API_KEY` | Chave da API da RAWG |

No frontend, copie `web/.env.example` para `web/.env`:

```env
VITE_API_URL=http://localhost:8080/api/games
VITE_AUTH_URL=http://localhost:8080/api/auth
```

## Como executar

### Docker Compose

```bash
docker compose up -d --build
```

Depois, suba o frontend:

```bash
cd web
npm install
npm run dev
```

| Serviço | URL |
|---|---|
| Frontend | http://localhost:5173 |
| API | http://localhost:8080 |
| Catalog service | http://localhost:8081 |
| Eureka | http://localhost:8761 |
| RabbitMQ (painel) | http://localhost:15672 (`guest` / `guest`) |
| Grafana | http://localhost:3000 |
| PostgreSQL | `localhost:5432` (`savegame` / `savegame`) |

Os dados ficam em volumes nomeados (`postgres-data`, `rabbitmq-data`, `tempo-data`, `loki-data`, `grafana-data`). Para apagar tudo: `docker compose down -v`.


## Endpoints

Todos os endpoints de `/api/games` exigem o header `Authorization: Bearer <access token>`.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Cadastro |
| `POST` | `/api/auth/login` | Login |
| `POST` | `/api/auth/refresh` | Novo access token (usa o cookie `refresh_token`) |
| `POST` | `/api/auth/logout` | Revoga o refresh token |
| `GET` | `/api/games` | Lista a biblioteca do usuário |
| `POST` | `/api/games` | Adiciona um jogo |
| `GET` | `/api/games/{id}` | Detalhes de um jogo |
| `PUT` | `/api/games/{id}` | Atualiza status e nota |
| `DELETE` | `/api/games/{id}` | Remove da biblioteca |
| `GET` | `/api/games/search?query=` | Busca jogos no catálogo |
| `GET` | `/api/games/{id}/history` | Histórico de alterações |

O access token dura 15 minutos e fica só em memória no frontend. O refresh token dura 7 dias, fica em cookie `HttpOnly` e é rotacionado a cada uso, com detecção de reuso.

Health checks: `GET /actuator/health` em cada serviço.

## Observabilidade

`api` e `catalogservice` enviam traces e logs direto por OTLP:

- **Traces → Tempo:** requisições HTTP, chamadas Feign e publicação/consumo de mensagens no RabbitMQ, propagando o contexto entre os serviços.
- **Logs → Loki:** cada log leva `service_name` e `trace_id`.

No Grafana (http://localhost:3000, acesso anônimo como admin), os datasources já vêm configurados: de um trace dá para abrir os logs correspondentes e vice-versa.

## Testes

```bash
cd api            # ou catalogservice / discoveryserver
./mvnw verify
```

- Testes unitários (services, listeners, outbox) e de controller (`@WebMvcTest`)
- Teste de integração do fluxo de eventos com RabbitMQ real via Testcontainers (**precisa do Docker rodando**)
- Relatório de cobertura do JaCoCo em `target/site/jacoco/index.html`

Frontend: `npm run lint` e `npm run build` dentro de `web/`.

## CI/CD

- **CI** (`.github/workflows/ci.yml`): em push e pull request para `main`, roda `./mvnw verify` em cada serviço (publicando o relatório do JaCoCo como artefato) e `lint` + `build` do frontend.
- **CD** (`.github/workflows/cd.yml`): quando o CI passa em um push na `main`, gera as imagens Docker e publica no GitHub Container Registry, com as tags `latest` e o SHA do commit.

## Observações relevantes

- **Credenciais de desenvolvimento:** Postgres, RabbitMQ e Grafana usam credenciais fixas ou acesso anônimo, pensados apenas para ambiente local.
