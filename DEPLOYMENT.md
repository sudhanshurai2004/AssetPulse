# Free Vercel portfolio demo

This deployment uses a Vercel project for the Vue frontend and Spring Boot API, plus Neon Free for PostgreSQL. Set `APP_SENSOR_INGESTION_MODE=direct` so sensor readings create work orders synchronously without a hosted Kafka broker. Local development keeps Kafka as the default.

## 1. Create the database

1. Sign up at [Neon](https://neon.com/) and create a PostgreSQL project on the Free plan.
2. In the Neon dashboard, open **Connect** and copy the pooled connection host, database name, user, and password. Use the pooled host for a serverless API.
3. Keep the password private. Do not commit it or send it in chat.

## 2. Import the GitHub project into Vercel

1. Open [Vercel New Project](https://vercel.com/new) and connect GitHub if prompted.
2. Import `sudhanshurai2004/AssetPulse`.
3. Keep the repository root as the project root. The root `vercel.json` configures the `frontend` and `backend` services and their routes.
4. Before the first production deployment, add the following **Production** environment variables in **Project Settings → Environment Variables**:

| Variable | Value |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<pooled-host>/<database>?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Neon database user |
| `SPRING_DATASOURCE_PASSWORD` | Neon database password |
| `APP_JWT_SECRET` | Private random value, at least 32 characters |
| `APP_BOOTSTRAP_USERNAME` | Your chosen admin username |
| `APP_BOOTSTRAP_PASSWORD` | A strong, unique admin password |
| `APP_SENSOR_INGESTION_MODE` | `direct` |

Use the pooled host and database name shown by Neon. If Neon gives a URL with query parameters, retain them and ensure `sslmode=require` is present. Do not set `VITE_API_URL`: the dashboard calls `/api` on the same Vercel domain.

## 3. Deploy and verify

1. Click **Deploy** in Vercel. On later pushes to `main`, Vercel deploys automatically.
2. Open `https://<your-project>.vercel.app/actuator/health`; the API should report `UP`.
3. Open `https://<your-project>.vercel.app/`, sign in with the bootstrap username/password, then send a sensor reading above an asset's threshold to create a work order.
4. Use this deployed URL and `https://github.com/sudhanshurai2004/AssetPulse` on your resume.

Vercel Hobby and Neon Free have usage limits. Neon sleeps after inactivity, so the first request can be slower. This is a portfolio/demo deployment, not an always-on production SLA. The hosted demo uses direct sensor processing; Kafka remains enabled by default for local development and tests.
