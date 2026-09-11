# Rental Platform

Property rental system with owner/client multi-auth and Stripe payments.

Stack: **Java 21 / Spring Boot 3** (backend) · **React 18 / Vite** (frontend) · **PostgreSQL** · **JWT auth** · **Stripe**.

## Project layout

```
rental-platform/
├── backend/     Spring Boot API (Maven)
├── frontend/    React SPA (Vite)
└── docker-compose.yml
```

## Prerequisites

- Java 21, Maven
- Node 20+
- Docker (for Postgres, or to run everything via Compose)
- A Stripe account (test mode keys are enough)

## Running locally (without Docker)

### 1. Database
```bash
docker run --name rental-db -e POSTGRES_DB=rentaldb \
  -e POSTGRES_USER=rental_user -e POSTGRES_PASSWORD=rental_pass \
  -p 5432:5432 -d postgres:16-alpine
```

### 2. Backend
```bash
cd backend
cp .env.example .env   # fill in JWT_SECRET and Stripe keys
export $(cat .env | xargs)
mvn spring-boot:run
```
Flyway runs the migrations automatically on startup. API is at `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`.

### 3. Frontend
```bash
cd frontend
cp .env.example .env   # set VITE_STRIPE_PUBLISHABLE_KEY
npm install
npm run dev
```
App is at `http://localhost:5173`.

### 4. Stripe webhook (local dev)
```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```
Copy the printed `whsec_...` into `backend/.env` as `STRIPE_WEBHOOK_SECRET`.

## Running everything via Docker Compose

```bash
JWT_SECRET=$(openssl rand -base64 32) \
STRIPE_SECRET_KEY=sk_test_... \
STRIPE_WEBHOOK_SECRET=whsec_... \
docker compose up --build
```
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`

## Testing

### Backend
```bash
cd backend
mvn test
```
Includes:
- Unit tests (`BookingServiceTest`, `PropertyServiceTest`) — mocked repositories, no DB needed
- A Spring Security slice test (`PropertyControllerSecurityTest`) verifying role-based `403`s, `401`s, and the ownership path, using the app's real `SecurityConfig` filter chain

### Frontend
```bash
cd frontend
npm test
```
Covers form/auth-flow behavior (`LoginPage`) and route guarding (`ProtectedRoute`).

## Key design notes

- **Roles**: `OWNER`, `CLIENT`, `ADMIN` on a single `users` table. Admins are never self-registered (enforced server-side in `AuthService`).
- **Authorization**: `@PreAuthorize("hasRole(...)")` on controllers, plus explicit ownership checks in the service layer (e.g. an owner can only edit/delete *their own* properties — see `PropertyService.getOwnedProperty`).
- **Payments**: a booking is created as `PENDING`; the frontend never marks it paid. Only a verified Stripe webhook event (`payment_intent.succeeded`) flips it to `CONFIRMED` — see `PaymentService.handleWebhookEvent`. Card details are collected client-side via Stripe Elements and never touch the backend.
- **Booking overlap prevention**: `BookingRepository.findOverlapping` blocks double-booking a property for intersecting date ranges — enforced in `BookingService`, covered by `BookingServiceTest`'s mocked-repository unit tests.

## What's not included (next steps)

- Property image upload (S3/blob storage integration) — schema and repository are in place, upload endpoint is not
- Refund flow (Stripe refund API call + `REFUNDED` status transition)
- Email verification before first booking
- Rate limiting on `/api/auth/**`
- CI pipeline (GitHub Actions) running the above test suites
- **Real-database integration tests.** Earlier drafts included `AuthIntegrationTest` (full register → login → protected-endpoint flow) and `BookingRepositoryTest` (overlap query against real Postgres) via Testcontainers. Both were removed because they require a working Docker daemon on the machine running `mvn test`, which isn't guaranteed in every dev environment — a hard requirement failure is worse than a coverage gap here. The logic they covered (overlap detection, auth flow) is still exercised by the mocked-repository unit tests, just not against a real database. If Docker is available in your environment (or in CI), re-adding Testcontainers-based tests is straightforward and worth doing before production use.
