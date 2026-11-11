
## Medicaps Launchpad API

A Spring Boot API for the coding question bank.

## API

All endpoints are under `/api/v1/questions`:

- `GET /api/v1/questions` lists published questions. It returns a page of summaries with `id`, `title`, `slug`, and `difficulty`.
- `GET /api/v1/questions/{id}` returns a question by its numeric ID.
- `GET /api/v1/questions/slug/{slug}` returns a question by its slug.

The list endpoint accepts `page` (default `0`), `size` (default `20`, capped at `100`), `difficulty` (`EASY`, `MEDIUM`, or `HARD`), `topic`, and `search` (matches question titles). Detail responses include the description, constraints, topic names, and visible sample test cases. Hidden test cases stay out of API responses.

Admin routes under `/api/v1/admin/**` require `X-Admin-Key`. Internal execution routes under `/api/v1/internal/**` require `X-Internal-Key`; set `INTERNAL_API_KEY` to enable them. They return execution configuration and hidden test cases, so keep the key private.

Example:

```text
GET /api/v1/questions?difficulty=EASY&topic=Arrays&page=0&size=20
```

## Database

Flyway migrations live in `src/main/resources/db/migration`:

- `problems` stores each question's title, unique slug, description, difficulty, draft/published status, constraints, and timestamps.
- `topics` stores unique topic names.
- `problem_topics` links questions and topics (many-to-many).
- `test_cases` stores ordered inputs and expected outputs for a question, with a `hidden` flag. The API only returns non-hidden cases.

`V2__seed_sample_questions.sql` adds a starter set of published questions, topics, and test cases. The database connection is configurable with `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. The default `dev` profile points to a local PostgreSQL database named `launchpad`.

## Run it

You need JDK 21 and PostgreSQL. Start the app with the Maven wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on `http://localhost:8080`. CI runs `./mvnw -B clean verify`; tests use an in-memory H2 database.
