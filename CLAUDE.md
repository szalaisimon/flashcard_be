# CLAUDE.md

This file provides guidance to AI coding assistants working in this repository.

## Project

Spring Boot 4.0.0 / Java 25 REST API for a flashcard study app. PostgreSQL stores users, decks, cards, and practice sessions; Redis enforces rate limits. The Angular frontend lives in the sibling `flashcard_fe` repository and uses cookie-based authentication. See [flashcard_fe](https://github.com/szalaisimon/flashcard_fe).

## Local development

Use JDK 25 and the Maven wrapper. `docker-compose.yml` provides PostgreSQL 16 and Redis 7 for local development, with ports bound to `127.0.0.1`.

```bash
# First-time setup: copy only if .env does not already exist.
cp .env-example .env
docker compose up -d postgres redis

# Export the local configuration in the shell that runs Maven.
set -a
. ./.env
set +a
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`; CORS permits `http://localhost:4200` with credentials. Spring Boot does not automatically load `.env`; Compose reads it for variable interpolation.

Required environment variables are `DB_HOST`, `DB_PORT`, `DB_NAME`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, and `JWT_SECRET` (a base64-encoded HMAC key). `.env-example` supplies local sample values. `APP_COOKIE_SECURE` defaults to `false`. The properties `app.jwt.access-expiration-minutes` and `app.jwt.refresh-expiration-days` default to 30 minutes and 30 days respectively.

```bash
./mvnw clean package                  # compile, test, and package
./mvnw -DskipTests package            # package without running tests
./mvnw test                           # run tests with the environment above
./mvnw test -Dtest=ClassName
./mvnw test -Dtest=ClassName#method
```

The test suite currently contains one `@SpringBootTest` context-loading test. It uses the application configuration and needs a reachable PostgreSQL database; there is no separate test profile or embedded database. Redis must be reachable for rate-limited API requests.

Schema management uses `spring.jpa.hibernate.ddl-auto=update`; there are no migration scripts. JPA auditing and JDBC timestamp handling use UTC.

## Architecture

Code is under `com.example.flashcard_api`, organized as `controller` → `service` → `repository`. Hand-written classes in `mapping` translate JPA entities from `model/entity` into DTOs from `model/dto`; controllers return DTOs rather than entities. Lombok supplies constructors and accessors.

- `controller/AuthController`: registration, login, refresh, logout, and current user at `/api/v1/user`.
- `controller/DeckController`: deck CRUD at `/api/v1/deck` and nested flashcard CRUD at `/{deckId}/flashcard`.
- `controller/DeckAttemptController`: practice creation/resumption, details, assessments, abort, scores, and history at `/api/v1/deckattempt`.
- `controller/HealthController`: public `GET /api/v1/health`.

Controllers use `/api/{version}/...` with Spring MVC's `version = "1"`; `spring.mvc.apiversion.*` configures the version path segment. Security matchers, JWT-filter exclusions, and the refresh-cookie path use literal v1 paths and must stay aligned with the controllers.

## Authentication and errors

- `AuthController` sets `HttpOnly`, `SameSite=Strict` cookies: `accessToken` at `/` and `refreshToken` at `/api/v1/user`. Login always creates a new database session and revokes the refresh token supplied by the same browser. A user can have multiple sessions through `RefreshToken`'s many-to-one user relation.
- Access JWTs include a `sid` claim with the session row ID. `JwtFilter` validates both the JWT and the session's existence, username, and expiry, then loads the user into the security context. Public auth endpoints and health bypass this filter. Authentication is stateless at the HTTP-session level, but access depends on the database session.
- Refresh validates the existing refresh token and issues a new access cookie. Logout revokes the session identified by either cookie and clears both cookies.
- Registration strips usernames, strips and lowercases email addresses, enforces database uniqueness for both, and hashes passwords with BCrypt strength 12. Login uses `LoginDto`; registration uses `UserDto`. Passwords are limited to 72 UTF-8 bytes for BCrypt.
- `CurrentUserService` exposes the principal's ID and username without a database lookup; `getUser()` loads the entity. `DeckAccessService` resolves owned, non-deleted decks, with a pessimistic-lock variant for writes. Attempt repositories restrict queries to the current owner, including history for deleted decks.
- Services throw `FlashCardApiException(HttpStatus, message)`. `GlobalExceptionHandler` returns JSON `{"error": ...}`; validation responses also include a `details` map of field errors. Security entry points and the JWT filter write their own JSON errors outside controller advice.
- `@RateLimit` currently covers register, login, refresh, and logout. `RateLimitAspect` keys buckets by authenticated username or remote IP plus controller method name. `RedisRateLimitService` executes an atomic Lua script for the counter and TTL. Rejections return 429 with `Retry-After`, which CORS exposes to the frontend.
- `LoggingAspect` logs controller arguments at DEBUG and excludes `AuthController`.

## Decks and practice sessions

- Decks and flashcards use explicit `deleted` flags. Active CRUD queries exclude deleted rows; there is no global Hibernate filter. Deleting a deck or card preserves practice history.
- Starting practice locks the owned active deck and returns its existing active attempt, if any. Empty decks cannot start practice. Each new attempt snapshots the deck name and the active cards' questions, answers, and positions, ordered by creation time and ID.
- `DeckAttemptStatus` is `IN_PROGRESS`, `COMPLETED`, or `ABORTED`. A unique nullable `activeDeckId` permits one active attempt per deck; completion or abort clears it and records `endedAt`.
- Every snapshotted card has a `CardAttempt`; `correct == null` means unanswered. Assessment locks the owned attempt and must target the next unanswered card. Repeating the same saved rating is idempotent unless the attempt is aborted; conflicting ratings return 409. The final assessment completes the attempt.
- Full attempt responses and history use snapshots, so later edits or soft deletions do not change saved questions, answers, deck names, or maximum scores. Scores expose correct, incorrect, and unanswered counts separately.
- Lists use `PaginatedResponse` with `data`, zero-based `currentPage`, `totalPages`, `totalItems`, `pageSize`, `hasNext`, and `hasPrevious`. Decks sort by `createdAt`/`id` descending, cards by `createdAt`/`id` ascending, and attempts by `attemptedAt`/`id` descending.
- Deck summaries and attempt scores use constructor projections in `model/projection` with count subqueries. Preserve these queries instead of loading entity collections to count items. Hibernate's default batch fetch size is 50.
- `AuditedBaseEntity` supplies actor IDs and timestamps through `ApplicationAuditAware`. `User` extends `BaseEntity` and has timestamp auditing without actor fields.

Keep API DTOs, status values, pagination fields, and cookie behavior consistent with the frontend models and services.
