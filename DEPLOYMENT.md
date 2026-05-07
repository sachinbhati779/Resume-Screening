# Deployment

This app deploys as two services:

- Spring Boot backend, connected to Aiven MySQL
- Next.js frontend, connected to the backend URL

## Backend on Render

Create a Render web service from this repository using the root `render.yaml`.

Use these environment variables in Render:

```text
DB_URL=jdbc:mysql://mysql-29b88b89-bhatisachin7790-9f62.k.aivencloud.com:27789/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
DB_USERNAME=avnadmin
DB_PASSWORD=<your Aiven password>
SPRING_SQL_INIT_MODE=always
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://<your-vercel-app>.vercel.app
```

Do not commit the Aiven password or CA certificate.

## Frontend on Vercel

Import the same GitHub repository in Vercel.

Use these settings:

```text
Framework Preset: Next.js
Root Directory: .
Build Command: npm run build
Install Command: npm install
```

Set this environment variable:

```text
NEXT_PUBLIC_API_BASE_URL=https://<your-render-backend>.onrender.com
```

After Vercel gives a public URL, update `CORS_ALLOWED_ORIGINS` in Render to include it, then redeploy/restart the backend.
