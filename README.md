
# MemoryMap

[![CI](https://github.com/Caicai-Jin/memorymap/actions/workflows/ci.yml/badge.svg)](https://github.com/Caicai-Jin/memorymap/actions/workflows/ci.yml)

A personal moment/mood journal. Every entry can carry text, photos, videos, a mood, and a location — all optional, in any combination — and every location you've recorded renders on a private map of everywhere you've been.

## Why MemoryMap

MemoryMap is not a food-review app.

It's built on a simple belief: life is made up of moments, and every one of them is worth keeping — not just the polished, exciting ones. A great meal you finally found time to enjoy. An afternoon of cherry picking, or a gym session that left you genuinely happy. A job interview that ended in rejection. An argument that turned into a breakup, right there at your usual bubble tea spot. A walk through a mall with no particular purpose. An anxious, embarrassing moment that turned warm because a stranger, without being asked, quietly helped you out. Rain outside your window at night, and a calm before sleep that you can't quite explain.

None of these are "content." They're just life. MemoryMap exists so you can record them as they are — happy, sad, or the kind of open, hard-to-name feeling that doesn't fit either label — with text, a photo or video, a mood, and optionally, a place.

Location is a supporting feature, not the point. If you attach a place to a moment, it appears on your own private map over time, so you can look back and see where your life has happened. But MemoryMap is deliberately not a travel app, and it never assumes you have the money or ability to go anywhere. A moment recorded at your kitchen table counts exactly as much as one recorded at the top of a mountain — the stressful evenings spent sending out job applications from home, the small joy of pulling a cake you made yourself out of the oven, both belong here just as much as anywhere else.

## Key Features

- **Moments** — create, edit, and delete journal entries with optional text, mood tags, media, and location, each secured to its owner
- **Media** — photo/video uploads via Cloudinary, with enforced limits (9 items per moment, 3 videos max, 60s per video, 50MB per file)
- **Locations** — real-world place search, with a dedicated Home address that is masked server-side on every response — other consumers of the API only ever see `"Home"`, never the real address or coordinates
- **Map view** — every public location plotted on an interactive map; Home locations never appear
- **Stats** — yearly mood breakdown and dominant-mood/location summaries, cached in Redis
- **Auth** — JWT-based registration and login, with every endpoint enforcing per-user ownership

## Architecture

```mermaid
flowchart LR
    Browser -->|HTTPS| Frontend[React + Vite<br/>served by nginx]
    Frontend -->|REST + JWT| Backend[Spring Boot API]
    Backend --> Postgres[(PostgreSQL)]
    Backend --> Redis[(Redis<br/>stats cache)]
    Backend --> Cloudinary[Cloudinary<br/>media storage]
    Backend --> Photon[Photon<br/>place search]
```

The frontend never talks to Postgres, Redis, Cloudinary, or Photon directly — every request goes through the backend, which is the single point enforcing authentication, ownership, and the Home-location masking rule.

## Tech Stack

**Backend** — Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, Spring Data Redis, Bean Validation, springdoc-openapi

**Frontend** — React 19, Vite, React Router, Tailwind CSS, React Leaflet

**Data & external services** — PostgreSQL, Redis, Cloudinary (media storage), Photon (place search, OpenStreetMap-based)

**Infrastructure** — Docker Compose, GitHub Actions CI

## Notable Design Decisions

- **Server-side privacy masking.** A user's Home location is desensitized in the API layer itself, not hidden client-side — `LocationService.maskIfHome` returns a location with only the literal name `"Home"` and no address or coordinates whenever it's attached to a moment, so no API consumer can ever recover it, regardless of how the response is used.
- **Ownership enforced at the service layer.** Every read/update/delete on a moment or location re-fetches the record and checks it against the authenticated user before proceeding, closing a class of IDOR vulnerabilities where a valid token for one account could be used to access another account's data by guessing an id.
- **Upload limits validated before the network call, not after.** File-size and Cloudinary-upload-count checks run before any file is sent to Cloudinary, so a rejected upload never wastes bandwidth or storage on a file that was going to be deleted anyway.
- **Both automated-test styles, deliberately.** The core ownership rule is covered by both a unit test (mocked dependencies, isolated logic) and an end-to-end test (real HTTP layer, real database) — see `MomentServiceTest` and `MomentOwnershipTest`.

## API Documentation

Every endpoint is documented and testable interactively once the backend is running:

```
http://localhost:8080/swagger-ui/index.html
```

Register or log in via the docs page's `/register` or `/login` endpoints, then click **Authorize** and paste the returned token — every subsequent request made through the docs UI will carry it automatically.

## Getting Started

### Option A: Docker Compose (recommended)

This runs the entire stack — PostgreSQL, Redis, backend, and frontend — with a single command.

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

1. Clone the repository and move into it:
   ```
   git clone https://github.com/Caicai-Jin/memorymap.git
   cd memorymap
   ```
2. Copy the environment template and fill in your own values:
   ```
   cp .env.example .env
   ```
   Open `.env` and set:
   - `DB_PASSWORD` — any password you choose; Docker Compose creates the database for you
   - `JWT_SECRET` — a Base64-encoded string, e.g. generate one with `openssl rand -base64 32`
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` — from a free [Cloudinary](https://cloudinary.com/) account
3. Start everything:
   ```
   docker compose up
   ```
4. Once startup finishes, open:
   - Frontend: [http://localhost:5173](http://localhost:5173)
   - API docs: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

To stop everything: `docker compose down` (add `-v` to also delete the database volume).

### Option B: Manual local setup

Use this if you want to run the backend and frontend directly on your machine instead of in containers — for active development, for example.

**Prerequisites:** JDK 21, Node.js 20+, PostgreSQL 17 running locally, Redis running locally.

1. Create a local PostgreSQL database named `memorymap`.
2. Set the following environment variables in your shell (or your IDE's run configuration):
   - `DB_PASSWORD` — your local Postgres password
   - `JWT_SECRET` — a Base64-encoded string, e.g. `openssl rand -base64 32`
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` — from a free Cloudinary account
3. Start the backend from the project root:
   ```
   ./mvnw spring-boot:run
   ```
   It runs on `http://localhost:8080`.
4. In a separate terminal, start the frontend:
   ```
   cd frontend
   npm install
   npm run dev
   ```
   It runs on `http://localhost:5173`.

## Running Tests

```
./mvnw test
```

Covers both unit tests (mocked dependencies) and end-to-end tests (real HTTP layer against a real PostgreSQL database). The same command runs automatically via GitHub Actions on every push and pull request.

## Project Structure

```
memorymap/
├── src/main/java/com/memorymap/memorymap/
│   ├── controller/   REST endpoints
│   ├── service/      business logic
│   ├── model/        JPA entities
│   ├── repository/   Spring Data repositories
│   ├── dto/          API request/response shapes
│   ├── exception/    custom exceptions + centralized error handling
│   ├── config/       security, caching, OpenAPI configuration
│   └── security/     JWT authentication filter
├── src/test/java/    unit and end-to-end tests
├── frontend/         React application
├── docker-compose.yml
└── .github/workflows/  CI pipeline
```

## Live Demo

- Frontend: https://memorymap-frontend-hz0x.onrender.com
- API docs: https://memorymap-backend.onrender.com/swagger-ui/index.html

Hosted on free-tier infrastructure (Render, Neon, Upstash). The backend spins down after 15 minutes of inactivity, so the first request after a while can take 30-60 seconds to wake up — subsequent requests are fast.
