# CarbonTrack

Web platform for environmental impact tracking and sustainability analytics.
Spring Boot 3 (Java 17) REST API + PostgreSQL. React dashboard comes in Milestone 3.

## Milestone 1 — User Profiles, Activity Logging & Emission Calculation Engine

What's included (`backend/`):

| Area | Implementation |
|------|----------------|
| Schema | PostgreSQL + Flyway: `V1__create_core_schema.sql` (users, organisations, activity_logs, emission_factors, goals, badges, user_badges), `V2__seed_emission_factors.sql` (34 factors), `V3__seed_badges.sql` |
| Entities | JPA entities for every table (`ddl-auto=validate`, so Flyway owns the schema) |
| Auth | Spring Security 6, stateless JWT (`/api/auth/register`, `/api/auth/login`), optional Google OAuth2 login |
| Profiles | `GET/PUT /api/users/me`, `PUT /api/users/me/password`, with preferences (units, goal visibility, leaderboard opt-in, weekly insights) |
| Activity logging | `POST /api/activities/{transport,electricity,food,shopping}`, `GET /api/activities` (category/date filters, paging), `GET/DELETE /api/activities/{id}` |
| Validation | Jakarta Bean Validation on all payloads (positive and range-limited quantities, no future dates, enum activity types) |
| Emission engine | `EmissionCalculationService` looks up the factor in force on the log date (`emission_factors`, versioned by `effective_date`), then `EmissionCalculator` computes quantity × factor. The resulting `co2e_kg` and `emission_factor_id` are stored on each log entry |
| Factor admin | `GET /api/emission-factors` (current factors, any user), `GET/POST/PUT /api/admin/emission-factors` (ADMIN only) |
| Tests | JUnit 5 + Mockito unit tests, `@ParameterizedTest`/`@CsvSource` emission tables, boundary cases (zero, max, negative, unknown types), Testcontainers integration tests against real PostgreSQL |

### Emission factor model

Factors live in the database, not in code. To revise a factor (e.g. when new IPCC/EPA/DEFRA tables
come out), an admin `POST`s a new row with a later `effectiveDate`. New logs pick up the new value.
Older logs keep the `co2e_kg` they were stored with, and back-dated logs use the factor that applied
on their date. The seeded values are teaching approximations of the cited sources. Check them
against the current official tables before relying on them in production.

| Category | Endpoint | Activity type field | Quantity field | Unit |
|----------|----------|---------------------|----------------|------|
| Transport | `/api/activities/transport` | `mode` (CAR_PETROL, CAR_DIESEL, CAR_HYBRID, CAR_ELECTRIC, MOTORBIKE, BUS, TRAIN, METRO, FLIGHT_DOMESTIC, FLIGHT_SHORT_HAUL, FLIGHT_LONG_HAUL, BICYCLE, WALK) | `distanceKm` (≤ 50 000) | km |
| Electricity | `/api/activities/electricity` | `energySource` (GRID_AVERAGE, COAL, NATURAL_GAS, SOLAR, WIND, HYDRO, NUCLEAR) | `kwh` (≤ 100 000) | kWh |
| Food | `/api/activities/food` | `mealType` (BEEF, LAMB, PORK, CHICKEN, FISH, VEGETARIAN, VEGAN) | `servings` (≤ 50) | serving |
| Shopping | `/api/activities/shopping` | `productCategory` (CLOTHING, ELECTRONICS, FURNITURE, HOUSEHOLD_GOODS, BOOKS_AND_MEDIA, PERSONAL_CARE, TOYS_AND_SPORTS) | `spendAmount` (≤ 1 000 000) | USD |

All four also accept optional `logDate` (ISO date, defaults to today, must not be in the future) and `notes` (≤ 500 chars).

## Running locally

Prerequisites: Java 17 and Docker. Maven comes through the wrapper.

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Run the API (Flyway migrates + seeds automatically) on http://localhost:8080
cd backend
ADMIN_EMAIL=admin@carbontrack.local ADMIN_PASSWORD='ChangeMe123!' ./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- Configuration: environment variables are listed in `.env.example`. Set `JWT_SECRET` (≥ 32 chars) anywhere outside local dev.

### Google OAuth2 (optional)

Create OAuth credentials in Google Cloud Console with redirect URI
`http://localhost:8080/login/oauth2/code/google`, then:

```bash
SPRING_PROFILES_ACTIVE=oauth GOOGLE_CLIENT_ID=... GOOGLE_CLIENT_SECRET=... ./mvnw spring-boot:run
```

Open `http://localhost:8080/oauth2/authorization/google`. After login, the user is created or linked
by email and redirected to `OAUTH2_REDIRECT_URI#token=<jwt>`. Without the `oauth` profile, Google login
is disabled and the API runs with JWT login only.

### Example requests

```bash
# Register (returns accessToken)
curl -s -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com","password":"Password123!"}'

TOKEN=...   # accessToken from the response

# Log 100 km in a petrol car -> co2eKg 17.0000
curl -s -X POST localhost:8080/api/activities/transport -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"mode":"CAR_PETROL","distanceKm":100}'

# List food entries for March 2025
curl -s "localhost:8080/api/activities?category=FOOD&from=2025-03-01&to=2025-03-31" -H "Authorization: Bearer $TOKEN"
```

## Tests

```bash
cd backend
./mvnw test
```

Integration tests (`*IntegrationTest`) start PostgreSQL through Testcontainers and are skipped automatically if Docker is not available.

## Project structure

```
backend/src/main/java/com/carbontrack
├── activity      # activity log entity, category enums, DTOs, logging API
├── auth          # register / login
├── badge         # badge + user_badge entities (engine in Milestone 2)
├── common        # error handling, paging
├── config        # security, CORS, OpenAPI, admin bootstrap
├── emission      # emission factors, calculator, calculation service, admin API
├── goal          # goal entity (goal tracking in Milestone 2)
├── organisation  # organisation entity (org dashboard in Milestone 3)
├── security      # JWT service/filter, OAuth2 success handler
└── user          # user entity, preferences, profile API
```
