# PharmaPredict AI

A portfolio-grade pharmacy inventory and medicine demand forecasting demo. It combines a React dashboard, a Java 21 / Spring Boot REST API, PostgreSQL persistence and a Python / FastAPI machine-learning service.

> **Important scope note:** The ML service is a working Random Forest forecasting example trained on a reproducible **synthetic demo dataset**. The generated sample sales in PostgreSQL are also illustrative. The validation metrics shown by the ML service are calculated on synthetic hold-out data; they are not evidence of accuracy on real pharmacy sales. Do not use this demo for actual medicine purchasing or clinical decisions without real data validation, monitoring and domain review.

## Architecture

```text
React + Vite dashboard (Netlify)
             |
             | HTTPS / JSON
             v
Java 21 + Spring Boot API (Render)
       |                  |
       | JPA              | HTTP POST /forecast
       v                  v
PostgreSQL (Neon)      Python FastAPI ML service (Render)
                            |
                            v
                    scikit-learn RandomForestRegressor
```

The Java service owns medicines, stock, sales and database access. It aggregates the last 30 days of sales into daily values and calls the Python service. The Python service forecasts 7–30 days recursively using lag, rolling-average, weekday and month features.

## Features

- Dashboard KPIs: active medicines, total units, low-stock count and 30-day sales.
- 14-day sales activity chart.
- Medicine inventory search, stock status and restock action.
- Record-sale API that decreases stock transactionally.
- Demand forecast with a selectable 7- or 14-day horizon.
- Synthetic demo data seeded once on an empty database.
- PostgreSQL schema managed by Flyway.
- Health endpoints and Micrometer / Prometheus metrics exposure.
- Netlify-ready React/Vite frontend with a labelled demo-preview fallback.
- Dockerfiles for the Java and Python services; Docker Compose for local database + services.

## Repository layout

```text
pharmapredict-ai/
├── frontend/       # React + Vite dashboard (deploy this folder to Netlify)
├── backend/        # Java 21 / Spring Boot REST API
├── ml-service/     # FastAPI + scikit-learn forecasting model
├── docker-compose.yml
└── README.md
```

## Run locally

### Option 1 — Docker Compose (recommended for API + database)

Requirements: Docker Desktop with Compose enabled, Node.js 20+ (22 recommended), and Java is not needed on the host for this option.

From the repository root:

```bash
docker compose up --build
```

The first ML startup trains the model and can take a little time. Services:

- Frontend is started separately below at `http://localhost:5173`.
- Spring Boot API: `http://localhost:8080`
- API health: `http://localhost:8080/actuator/health`
- Swagger-style API exploration is available at the ML service docs: `http://localhost:8000/docs`
- ML health: `http://localhost:8000/health`

In a second terminal:

```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

Open `http://localhost:5173`.

### Stop services

```bash
docker compose down
```

To also delete the local PostgreSQL data volume (destructive):

```bash
docker compose down -v
```

### Option 2 — Run services without Docker

1. Install Java 21, Maven 3.9+, PostgreSQL 16, Python 3.12 and Node.js 20+.
2. Create a database named `pharmapredict` and user/password `pharmapredict` (or update environment variables).
3. Start the ML API:

```bash
cd ml-service
python -m venv .venv
# Windows PowerShell: .venv\\Scripts\\Activate.ps1
# macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
uvicorn app:app --reload --port 8000
```

4. Start the Java API in another terminal:

```bash
cd backend
mvn spring-boot:run
```

5. Start the frontend:

```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

## API reference

Base URL: `http://localhost:8080`

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/actuator/health` | Backend health |
| GET | `/api/dashboard/summary` | Dashboard KPIs |
| GET | `/api/dashboard/sales-trend` | Last 14 days of sales |
| GET | `/api/medicines` | Active medicine catalogue |
| GET | `/api/medicines/low-stock` | Medicines at/below reorder level |
| POST | `/api/medicines` | Add medicine |
| POST | `/api/medicines/{id}/restock` | Add stock units |
| POST | `/api/sales` | Record a sale and reduce stock |
| GET | `/api/forecasts/{medicineId}?days=7` | ML forecast; days 1–30 |

Example restock:

```bash
curl -X POST http://localhost:8080/api/medicines/1/restock \\
  -H 'Content-Type: application/json' -d '{"quantity":50}'
```

Example sale:

```bash
curl -X POST http://localhost:8080/api/sales \\
  -H 'Content-Type: application/json' \\
  -d '{"medicineId":1,"quantity":2,"soldAt":"2026-11-03"}'
```

ML service endpoints:

- `GET /health`
- `GET /model-info`
- `POST /forecast`
- `GET /docs` for interactive API documentation

## Deploy the frontend to Netlify

Netlify hosts the React static frontend. The Java API and Python ML service must be deployed separately; a Netlify static site cannot run these long-lived services.

1. Create a GitHub repository and push this project to it.
2. In Netlify, choose **Add new site → Import an existing project**, connect GitHub and select the repository.
3. The included root `netlify.toml` sets the base directory to `frontend`, the build command to `npm run build`, and the publish directory to `dist`. Leave the repository root as the config location.
4. Deploy once. The site can display its labelled sample preview before the API is connected.
5. After deploying the backend (below), open **Site configuration → Environment variables** and add:
   - Key: `VITE_API_URL`
   - Value: `https://YOUR-SPRING-BOOT-SERVICE.onrender.com`
6. Trigger a new deploy so the Vite build receives the new value.

The root `netlify.toml` configures the `frontend` base directory, `dist` output and SPA fallback. Avoid overriding those paths in the Netlify UI unless you are deliberately changing the configuration.

## Deploy the ML service to Render

1. Push the repository to GitHub.
2. In Render, create **New → Web Service** and connect the repository.
3. Set **Root Directory** to `ml-service`.
4. Choose **Docker** as the runtime. Render will build `ml-service/Dockerfile`.
5. Set the health check path to `/health` and deploy.
6. Copy the service URL, for example `https://pharmapredict-ml.onrender.com`.

The ML service trains its model during startup. On a small/free instance the first boot can be slower; wait until `/health` returns `{"status":"ok","modelReady":true}`.

## Deploy PostgreSQL

Create a PostgreSQL database with a provider such as Neon. Copy the connection information from the provider console. Keep the database username/password private; never put them in frontend variables or commit them to GitHub.

For Neon, construct the JDBC URL using the host and database name from the connection details:

```text
jdbc:postgresql://YOUR-NEON-HOST/YOUR-DB?sslmode=require
```

Set the username and password as separate environment variables on the Java service. Use the provider's direct connection endpoint if connection pooling creates migration/session issues.

## Deploy the Spring Boot API to Render

1. In Render, create another **New → Web Service** using the same GitHub repository.
2. Set **Root Directory** to `backend` and runtime to **Docker**.
3. Set health check path to `/actuator/health`.
4. Add these environment variables in the Render service settings:

| Key | Value |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://YOUR-NEON-HOST/YOUR-DB?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Your database username |
| `SPRING_DATASOURCE_PASSWORD` | Your database password |
| `ML_SERVICE_URL` | The deployed ML service URL, e.g. `https://pharmapredict-ml.onrender.com` |
| `APP_CORS_ORIGINS` | Your exact Netlify origin, e.g. `https://YOUR-SITE.netlify.app` |

5. Deploy. Flyway creates the schema; the application seeds sample rows only when the medicines table is empty.
6. Test `https://YOUR-SPRING-BOOT-SERVICE.onrender.com/actuator/health`. It should return `{"status":"UP"}`.
7. Test `/api/medicines` and `/api/forecasts/1?days=7`.
8. Set `VITE_API_URL` in Netlify to the API URL and redeploy the frontend.

## Deployment order checklist

- [ ] Create Neon PostgreSQL database.
- [ ] Deploy `ml-service` on Render and confirm `/health`.
- [ ] Deploy `backend` on Render with DB + ML + CORS environment variables.
- [ ] Confirm backend health and `/api/medicines`.
- [ ] Deploy `frontend` on Netlify with `VITE_API_URL` set to the backend URL.
- [ ] Open the Netlify URL, refresh, and confirm the badge changes from Demo preview to Live data connected.
- [ ] Test a restock action and a forecast.

## Important production limitations

This is an educational portfolio application, not a production pharmacy platform. It currently has no user login/RBAC, tenant isolation, prescription workflows, drug-interaction checks, audit trail, rate limiting, or formal inventory concurrency controls. Add authentication/authorization, tenant-level access controls, audit logging, backups, secrets management and real-data model validation before any real business use. Forecasts are estimates and must not be used as medical advice.

## Suggested interview explanation

"PharmaPredict AI is a three-part system. The Spring Boot service owns inventory and sales data in PostgreSQL. It aggregates the recent daily sales and calls a separate Python FastAPI service. The ML service uses a Random Forest regressor with lag-based and calendar features to generate a short-term demand estimate. I separated the model runtime from the transactional backend so model dependencies and inference can evolve independently. The current training data is synthetic, so I treat this as a system-design and implementation demo rather than claiming real-world model accuracy."
