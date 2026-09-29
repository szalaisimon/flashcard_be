# Flashcard — Backend

REST API for a flashcard study app with user accounts, deck and card editing, resumable practice sessions, and saved practice history.

The app consists of two repositories:

| Repository | Purpose |
| --- | --- |
| [flashcard_be](https://github.com/szalaisimon/flashcard_be) | Spring Boot API, persistence, and authentication |
| [flashcard_fe](https://github.com/szalaisimon/flashcard_fe) | Angular user interface |

## Stack

Java 25, Spring Boot 4.0.0, Spring Security, Spring Data JPA, PostgreSQL 16, and Redis 7. Authentication uses access and refresh cookies. Practice sessions snapshot card content so edits and deletions preserve saved history.

## Run locally

Requires JDK 25 and Docker with Compose. Use the included Maven wrapper.

```bash
git clone https://github.com/szalaisimon/flashcard_be.git
cd flashcard_be
cp .env-example .env
docker compose up -d postgres redis

# Export configuration for Spring Boot in this shell.
set -a
. ./.env
set +a
./mvnw spring-boot:run
```

Copy `.env-example` only during initial setup; keep an existing `.env`. The sample values are for local development, and `.env` is ignored by Git. Spring Boot needs the variables exported in its shell; Compose reads `.env` automatically.

The API runs at `http://localhost:8080`. Start the [frontend](https://github.com/szalaisimon/flashcard_fe#run-locally) at `http://localhost:4200`, which is the allowed CORS origin. PostgreSQL and Redis expose their ports only on `127.0.0.1`.

The environment file defines the database connection, Redis connection, JWT signing key, and cookie security setting. Hibernate updates the database schema on startup with `ddl-auto=update`.

## Build and test

```bash
./mvnw -DskipTests package
./mvnw test
```

The JAR is written to `target/flashcard-api.jar`. The current test is a Spring context-loading test and requires the exported environment and a reachable PostgreSQL database. Redis is required for rate-limited authentication requests.

## API areas

- `/api/v1/user`: register, login, refresh, logout, and current user.
- `/api/v1/deck`: decks and nested flashcards.
- `/api/v1/deckattempt`: practice sessions, ratings, scores, and history.
- `/api/v1/health`: health endpoint.

See [CLAUDE.md](CLAUDE.md) for architecture, API contracts, and coding context.
