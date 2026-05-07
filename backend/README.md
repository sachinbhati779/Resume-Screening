# AI Resume Screening + AI Interviewer Backend

Spring Boot backend for the Next.js dashboard in the repository root.

## Stack

- Java 17+
- Spring Boot 4.0.6
- Spring JDBC
- MySQL
- ExecutorService for concurrent screening
- JSON REST APIs
- Optional OpenAI evaluation hook through `OPENAI_ENABLED` and `OPENAI_API_KEY`

## Run Locally

```bash
cd backend
docker compose up -d
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` and allows the frontend origin `http://localhost:3000` by default.

Run backend tests without installing Maven globally:

```bash
cd backend
./mvnw test
```

Start the frontend against the backend:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 npm run dev
```

Docker Desktop or another Docker daemon must be running before `docker compose up -d` can start MySQL.

## Environment

```bash
DB_URL=jdbc:mysql://localhost:3306/resume_screening?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=password
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://127.0.0.1:3000
SCREENING_THREAD_POOL_SIZE=8
OPENAI_ENABLED=false
OPENAI_API_KEY=
```

## API Surface

- `GET /api/dashboard/summary`
- `GET /api/dashboard/recent-activity`
- `POST /api/job-roles`
- `GET /api/job-roles`
- `GET /api/job-roles/{id}`
- `POST /api/resumes`
- `POST /api/resumes/upload`
- `GET /api/resumes`
- `GET /api/resumes/{id}`
- `GET /api/resumes/{id}/file`
- `GET /api/resumes/{id}/file?download=true`
- `POST /api/screen`
- `GET /api/screening-reports`
- `GET /api/screening-reports/{id}`
- `GET /api/candidates/ranking`
- `GET /api/shortlisted`
- `GET /api/shortlisted/export`
- `POST /api/interview/start`
- `GET /api/interview/{sessionId}/question`
- `POST /api/interview/{sessionId}/answer`
- `GET /api/interview/{sessionId}/result`
- `GET /api/hiring-decisions`
- `POST /api/hiring-decisions`

## Response Shape

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {}
}
```

Errors:

```json
{
  "success": false,
  "message": "Summary is required",
  "errorCode": "INCOMPLETE_RESUME"
}
```

## Screening Rules

The rule engine implements `Scorable` and scores resumes out of 100:

- Skills: default 40
- Experience: default 25
- Projects: default 15
- Education: default 10
- Keywords: default 10

Status bands:

- `80-100`: `SHORTLISTED`
- `60-79`: `CONSIDER`
- `<60`: `REJECTED`

Multiple resumes are screened with `ExecutorService.invokeAll`, and each resume is persisted independently.

Each screening report also stores matched keywords, missing keywords, weighted score breakdown, ATS validation flags, and a recruiter-readable explanation. Candidate ranking combines ATS and interview results with:

```text
Final Score = (ATS Score * 0.6) + (Interview Score * 0.4)
```

Hiring decisions are normalized to `HIRE`, `HOLD`, or `REJECT`.

## Local API Smoke Test

For a quick HTTP smoke test without MySQL, the backend can run with the test H2 schema:

```bash
cd backend
./mvnw spring-boot:test-run -Dspring-boot.run.profiles=test -Dspring-boot.run.arguments="--spring.sql.init.mode=always --spring.sql.init.schema-locations=classpath:schema-h2.sql --server.port=8080"
```
