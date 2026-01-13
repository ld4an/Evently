# Evently

Full-stack event management system with a Spring Boot API and a Quasar (Vue 3) client.

## Tech stack

- Backend: Spring Boot 3, Java 21, PostgreSQL, Spring Security, JWT, Mail
- Frontend: Quasar (Vue 3 + Vite), Pinia, Axios

## Project structure

- `backend/` Spring Boot API
- `frontend/event-client/` Quasar SPA

## Prerequisites

- Java 21
- Maven (or use the included `mvnw`/`mvnw.cmd`)
- Node.js (see `frontend/event-client/package.json` engines)
- Docker (optional, for Postgres)

## Configuration

Backend environment variables live in `backend/.env`. Update these values for your local setup:

- `DB_USER`, `DB_PASS`, `DB_NAME`
- `JWT_SECRET`, `JWT_EXPIRATION_MS`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_TOKEN`

## Running locally

### 1) Start PostgreSQL (optional but recommended)

```bash
cd backend
docker compose up -d
```

The container exposes Postgres on `localhost:5431` (mapped to container port 5432).

### 2) Run the backend API

```bash
cd backend
./mvnw spring-boot:run
```

On Windows:

```bat
cd backend
mvnw.cmd spring-boot:run
```

### 3) Run the frontend

```bash
cd frontend/event-client
npm install
npm run dev
```

## Frontend scripts

```bash
cd frontend/event-client
npm run lint
npm run format
npm run build
```

## Backend build

```bash
cd backend
./mvnw clean package
```

## License

See `LICENSE`.
