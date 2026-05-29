# Production Deployment Guide

## Changes Made to All Services

The same set of changes was applied to: **iwallet, iaccount, iuser, imerchant, ibalance**

### 1. `pom.xml`
- Added `<skip>true</skip>` to `mybatis-generator-maven-plugin` inside the `production` profile — prevents it trying to connect to `localhost:5432` during Railway builds
- ibalance: added the entire `production` profile (it had none)

### 2. `app/web/src/main/resources/application.properties`
All hardcoded `localhost` values replaced with `${ENV_VAR:fallback}` syntax — local dev still works with the fallback, Railway injects the real values.

### 3. `Dockerfile` (moved/created at project root)
- Multi-stage build: Maven build stage → JRE runtime stage
- `ARG GITHUB_USERNAME` / `ARG GITHUB_TOKEN` injected from Railway env vars for GitHub Packages auth
- Old `app/web/Dockerfile` removed (iuser)

### 4. `.mvn/settings.xml` + `.mvn/maven.config` (new)
- `settings.xml`: maps GitHub server IDs to `${env.GITHUB_USERNAME}` / `${env.GITHUB_TOKEN}`
- `maven.config`: tells Maven to use `.mvn/settings.xml` automatically — no build command changes needed

### 5. `.dockerignore` (new)
Excludes `target/` directories from Docker build context.

---

## Kafka Topics (Confluent Cloud)

Create these **7 topics** (1 partition each):

| Topic | Publisher | Consumer |
|---|---|---|
| `EC_TRANSACTION` | iwallet | iaccount |
| `EC_TRANSACTION_RESULT` | iaccount | iwallet |
| `EC_AUTO_RELOAD` | iwallet + iaccount | iwallet |
| `TOP_UP_SUCCESS` | iwallet | iwallet |
| `EC_DEAD_LETTER_QUEUE` | iaccount | *(DLQ — no consumer)* |
| `EC_SYNC_DOCUMENTS_JOB` | ibalance | ibalance |
| `EC_SOURCE_DOCUMENT_READY` | ibalance | ibalance |

---

## Railway Environment Variables

### Shared — set in EVERY service that uses it

| Variable | Value | Used by |
|---|---|---|
| `GITHUB_USERNAME` | `Jung028` | all (Maven build auth) |
| `GITHUB_TOKEN` | GitHub PAT with `read:packages` | all (Maven build auth) |
| `REDIS_HOST` | Railway Redis hostname | all |
| `REDIS_PORT` | Railway Redis port | all |
| `NACOS_ADDRESS` | Nacos URL or leave unset if not used | all |
| `KAFKA_BOOTSTRAP_SERVERS` | Confluent bootstrap URL | iwallet, iaccount, imerchant, ibalance |
| `KAFKA_SECURITY_PROTOCOL` | `SASL_SSL` | iwallet, iaccount, imerchant, ibalance |
| `KAFKA_SASL_MECHANISM` | `PLAIN` | iwallet, iaccount, imerchant, ibalance |
| `KAFKA_SASL_JAAS_CONFIG` | `org.apache.kafka.common.security.plain.PlainLoginModule required username="KEY" password="SECRET";` | iwallet, iaccount, imerchant, ibalance |

### Per-service

| Variable | Service | Value |
|---|---|---|
| `DATABASE_URL` | all | Railway Postgres URL (each service gets its own DB) |
| `DATABASE_USERNAME` | all | Railway Postgres username |
| `DATABASE_PASSWORD` | all | Railway Postgres password |
| `STRIPE_API_KEY` | iuser, ibalance | Stripe secret key |
| `PLAID_CLIENT_ID` | ibalance | Plaid client ID |
| `PLAID_SECRET` | ibalance | Plaid secret |
| `PLAID_BASE_URL` | ibalance | `https://sandbox.plaid.com` (sandbox) or production URL |
| `GMAIL_CLIENT_ID` | ibalance | Google Cloud OAuth2 client ID |
| `GMAIL_CLIENT_SECRET` | ibalance | Google Cloud OAuth2 client secret |
| `GMAIL_REDIRECT_URI` | ibalance | Production callback URL |
| `IBALANCE_AGENT_BASE_URL` | ibalance | URL of the AI agent service |
| `IBALANCE_AGENT_MOCK` | ibalance | `false` in production |
| `IBALANCE_STORAGE_BASE_URL` | ibalance | Public base URL of ibalance service |
| `WHATSAPP_ACCESS_TOKEN` | ibalance | Meta WhatsApp token |
| `WHATSAPP_PHONE_NUMBER_ID` | ibalance | Meta phone number ID |

---

## Git Commands (per service)

```bash
# iwallet
cd /Users/adam/IdeaProjects/iwallet
git add Dockerfile .dockerignore .mvn/ pom.xml app/web/src/main/resources/application.properties
git commit -m "fix: production build — multi-stage Dockerfile, env vars, skip MyBatis generator"
git push

# iaccount
cd /Users/adam/IdeaProjects/iaccount
git add Dockerfile .dockerignore .mvn/ pom.xml app/web/src/main/resources/application.properties
git commit -m "fix: production build — multi-stage Dockerfile, env vars, skip MyBatis generator"
git push

# iuser
cd /Users/adam/IdeaProjects/iuser
git add Dockerfile .dockerignore .mvn/ pom.xml app/web/src/main/resources/application.properties
git rm app/web/Dockerfile
git commit -m "fix: production build — multi-stage Dockerfile, env vars, skip MyBatis generator"
git push

# imerchant
cd /Users/adam/IdeaProjects/imerchant
git add Dockerfile .dockerignore .mvn/ pom.xml app/web/src/main/resources/application.properties
git commit -m "fix: production build — multi-stage Dockerfile, env vars, skip MyBatis generator"
git push

# ibalance
cd /Users/adam/IdeaProjects/ibalance
git add Dockerfile .mvn/ pom.xml app/web/src/main/resources/application.properties
git commit -m "fix: production build — multi-stage Dockerfile, env vars, skip MyBatis generator"
git push
```
