# Lokendra Store — Production Deployment Guide 🚀

This document covers how to run and deploy the full stack:
- **Frontend:** React 18 + Vite (served via Nginx Alpine reverse proxy)
- **Backend:** Spring Boot 4.x / Java 17 with Agentic RAG, JWT Security, & MySQL
- **Database:** MySQL 8.0 with auto-seeding of 50 Indian market products
- **AI Engine:** Dual-mode (Local Ollama for dev/GPU machines, Google Gemini Cloud for production)

---

## 1. Quick Start: Local Docker Deployment

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop) installed and running
- (Optional for Local AI) [Ollama](https://ollama.com) running on the host machine:
  ```bash
  ollama serve
  ollama pull mistral
  ollama pull nomic-embed-text
  ```

### Step 1: Configure Environment
Copy `.env.example` to `.env` (already created at project root):
```bash
cp .env.example .env
```
Key variables in `.env`:
| Variable | Description | Default |
|---|---|---|
| `DB_PASSWORD` | MySQL root password | `Lokendra@5001#` |
| `JWT_SECRET` | 256-bit Base64 signing key | pre-configured |
| `OLLAMA_BASE_URL` | Ollama host address | `http://host.docker.internal:11434` |
| `GEMINI_API_KEY` | Google Gemini API key (cloud AI) | pre-configured |
| `FRONTEND_PORT` | Port exposed for Web UI | `3000` |
| `BACKEND_PORT` | Port exposed for Spring Boot API | `8080` |
| `MYSQL_PORT` | Port exposed for MySQL | `3306` |

### Step 2: Build and Run
```bash
# Build all images
docker compose build

# Start all containers in background
docker compose up -d
```

### Step 3: Verify Running Services
```bash
docker compose ps
```
- **Web Storefront:** [http://localhost:3000](http://localhost:3000)
- **Backend API:** [http://localhost:8080/api/products](http://localhost:8080/api/products)
- **MySQL Database:** `localhost:3306` (database: `springecom`, user: `root`)

To view live logs:
```bash
docker compose logs -f backend
```

To stop:
```bash
docker compose down
```

---

## 2. Cloud Deployment Strategies

### Option A: Railway / Render (Easiest Full Stack)

#### 1. Database (Railway MySQL or Aiven Free MySQL)
1. In Railway or Aiven, create a MySQL 8.0 database.
2. Note the connection details: `host`, `port`, `database`, `user`, `password`.
3. Import the initial seed data:
   ```bash
   mysql -h <remote-host> -u <remote-user> -p<remote-password> <database-name> < insert_50_products.sql
   ```

#### 2. Backend (Render / Railway Web Service)
1. Point Railway / Render to your GitHub repository, root directory: `/Search`.
2. Select Dockerfile deployment.
3. Configure Environment Variables:
   ```env
   SERVER_PORT=8080
   DB_URL=jdbc:mysql://<db-host>:<db-port>/<database-name>?allowPublicKeyRetrieval=true&useSSL=true&serverTimezone=UTC
   DB_USERNAME=<db-user>
   DB_PASSWORD=<db-password>
   ALLOWED_ORIGINS=https://your-frontend-app.vercel.app
   GEMINI_API_KEY=<your-gemini-key>
   JWT_SECRET=<your-256-bit-jwt-secret>
   ```
4. Deploy service. It will output a public URL (e.g. `https://lokendra-store-api.up.railway.app`).

#### 3. Frontend (Vercel / Netlify)
1. Connect your GitHub repository to Vercel or Netlify.
2. Set Root Directory to: `t-ecom`.
3. Framework Preset: **Vite**.
4. Build Command: `npm run build`.
5. Output Directory: `dist`.
6. Environment Variables:
   ```env
   VITE_BASE_URL=https://lokendra-store-api.up.railway.app
   ```
7. Add a `vercel.json` or Netlify `_redirects` file for SPA routing:
   ```json
   {
     "rewrites": [
       { "source": "/(.*)", "destination": "/index.html" }
     ]
   }
   ```
8. Click **Deploy**.

---

### Option B: Single VPS (Ubuntu / AWS EC2 / DigitalOcean)

A single \$6-\$12/month VPS can run the entire stack with `docker compose`:

1. SSH into your VPS:
   ```bash
   ssh root@your-server-ip
   ```
2. Install Docker & Docker Compose:
   ```bash
   curl -fsSL https://get.docker.com | sh
   ```
3. Clone your repository:
   ```bash
   git clone https://github.com/<your-username>/SpringBoot-Proj.git
   cd SpringBoot-Proj
   ```
4. Create `.env` file with production secrets:
   ```bash
   cp .env.example .env
   nano .env
   ```
   *Make sure `ALLOWED_ORIGINS` includes your domain (e.g., `https://mystore.com`).*
5. Run the stack:
   ```bash
   docker compose up -d --build
   ```
6. Set up SSL with Caddy or Nginx reverse proxy + Certbot:
   ```bash
   # Caddyfile example
   mystore.com {
       reverse_proxy localhost:3000
   }
   ```

---

## 3. Production AI Configuration (Switching from Ollama to Gemini)

In local development, the app connects to `Ollama` running on your GPU.
When deploying to cloud environments (where GPU hosting is expensive), you can use **Google Gemini Cloud AI**:

1. In `Search/src/main/resources/application.properties` or environment variables:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
2. The `ProductController` already includes Gemini fallbacks for:
   - Product description generation (`/api/product/generate-description`)
   - Product feature extraction
   - Autonomous agent reasoning can target Gemini models directly via Spring AI Google GenAI starter.

---

## 4. Production Checklist Before Going Live

- [ ] Change `DB_PASSWORD` to a strong random string.
- [ ] Change `JWT_SECRET` to a unique 256-bit Base64 key:
  ```bash
  openssl rand -base64 32
  ```
- [ ] Set `ALLOWED_ORIGINS` in backend to your exact frontend domain (disable wildcard CORS).
- [ ] Verify SSL/TLS certificates (HTTPS) on frontend and backend.
- [ ] Run health check:
  ```bash
  curl https://your-backend-domain/api/products?page=0&size=1
  ```
