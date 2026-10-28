# Ledgr Backend Web Application 🛡️💸
### *RBI Account Aggregator (AA) & Student FinTech OS REST API Engine*

![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Ready-informational.svg)
![H2 Database](https://img.shields.io/badge/H2-In--Memory%20Fallback-blue.svg)
![RBI AA Framework](https://img.shields.io/badge/RBI%20AA-ReBIT%20Standard-purple.svg)

**Ledgr** is a high-performance backend REST web application engineered for student personal financial planning operating under the **Reserve Bank of India (RBI) Account Aggregator (AA)** framework. It features stepped cryptographic AA handshake emulation, ReBIT financial data ingestion, real-time live transaction streaming, scholarship runway forecasting, and graduate salary tax simulation.

The desktop frontend has been entirely removed, transforming Ledgr into a headless, cloud-ready REST API service.

---

## 🚀 Key Features & Capabilities

- **🏛️ RBI Account Aggregator (AA) Gateway Sandbox (`/api/v1/aa`)**:
  - Full ReBIT-standard lifecycle: Consent negotiation $\rightarrow$ OTP approval $\rightarrow$ Key exchange session $\rightarrow$ Decrypted payload delivery.
  - Multi-account support: Savings Accounts, Term Deposits (FD), and Recurring Deposits (RD).
- **📥 FIU Ingestion & Data Management (`/api/v1/fiu`)**:
  - One-click end-to-end sync (`POST /api/v1/fiu/sync`).
  - Searchable, filterable transactions by merchant, category, type, and pagination.
  - Comprehensive financial overview: net worth, monthly burn rate, scholarship runway, and category expense breakdown.
- **⚡ Real-Time Live Transaction Pump (`/api/v1/simulator`)**:
  - Instant simulated transaction injection (`POST /api/v1/simulator/transactions/generate`).
  - Continuous background streaming (`POST /api/v1/simulator/stream/start?intervalSeconds=3`).
  - Server-Sent Events (SSE) live stream feed (`GET /api/v1/simulator/stream/events`).
- **🎓 Student FinTech Calculators (`/api/v1/tools`)**:
  - **Scholarship Runway Calculator**: Projects safe burn runway (in months) with customizable burn rates and FD buffer inclusions.
  - **Post-Graduation Salary Sandbox**: In-hand salary simulator factoring New Tax Regime slabs, 12% EPF, and city cost-of-living indices (Bangalore, Mumbai, Delhi, Hyderabad, Pune).
  - **Roommate Escrow & Expense Splitter**: Identifies shared dining, rent, groceries, and Wi-Fi IOUs and computes equal shares.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java 21+ / OpenJDK |
| **Framework** | Spring Boot 3.3.4 (REST Web Application & Embedded Tomcat on Port 8085) |
| **Data Persistence** | Spring Data JPA + Hibernate 6 + PostgreSQL (with automatic zero-config H2 fallback) |
| **JSON Serialization** | Jackson Data Processor (JSR310 Datatype) with lenient ReBIT aliasing |
| **Monitoring** | Spring Boot Actuator (`/actuator/health`) |
| **Build Tool** | Apache Maven Wrapper (`mvnw.cmd` / `mvn`) |

---

## 🏁 Getting Started & Running the Backend

### 1. Prerequisites
- **Java 21 LTS** or higher (verify with `java -version`)
- **Maven 3.9+** (optional; the repository includes `mvnw.cmd` / `mvnw`)
- **PostgreSQL** (optional; the backend automatically falls back to an in-memory H2 database with PostgreSQL compatibility mode if Postgres is not reachable)

---

### 2. Environment Configuration (`.env`)
Create a `.env` file in the root directory (or copy from [`.env.example`](file:///d:/college%20related/SEM5/Laboratory%20courses/Java%20lab/.env.example)):

```env
# Server
SERVER_PORT=8085

# Database Configuration (Defaults to local PostgreSQL with H2 fallback)
DB_URL=jdbc:postgresql://localhost:5432/ledgr_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Banking Sandbox API (Plaid) Credentials
BANKING_API_CLIENT_ID=your_plaid_client_id
BANKING_API_SECRET=your_plaid_sandbox_secret
BANKING_API_ENV=sandbox
BANKING_API_BASE_URL=https://sandbox.plaid.com
BANKING_INSTITUTION_ID=ins_20
BANKING_WEBHOOK_URL=https://www.genericwebhookurl.com/webhook
BANKING_OVERRIDE_USERNAME=user_good
BANKING_OVERRIDE_PASSWORD=pass_good
TRANSACTION_DELAY_MS=150
```

> [!NOTE]
> Environment variables in `.env` are automatically loaded into Spring Boot properties via [`DotenvLoader`](file:///d:/college%20related/SEM5/Laboratory%20courses/Java%20lab/src/main/java/com/ledgr/config/DotenvLoader.java) upon startup.

---

### 3. Build & Run the Application

#### Option A: Using the Maven Wrapper (Recommended)
From the project root directory:

**Windows (PowerShell / Command Prompt):**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS / Git Bash:**
```bash
./mvnw spring-boot:run
```

#### Option B: Using Installed Maven
```bash
mvn spring-boot:run
```

#### Option C: Build Executable JAR and Run
```powershell
# Build packaged JAR
.\mvnw.cmd clean package -DskipTests

# Run JAR
java -jar target/ledgr-backend-1.0.0-SNAPSHOT.jar
```

The web server will boot on port **`8085`**:
- **Interactive Web Dashboard**: [http://localhost:8085](http://localhost:8085)
- **API Overview & Directory**: [http://localhost:8085/api/v1](http://localhost:8085/api/v1)
- **Health Check**: [http://localhost:8085/api/v1/health](http://localhost:8085/api/v1/health) or [http://localhost:8085/actuator/health](http://localhost:8085/actuator/health)

---

### 4. Running Automated Tests
Run integration tests (including Plaid banking sandbox tests and entity persistence):

**Windows:**
```powershell
.\mvnw.cmd test
```

**Linux / macOS:**
```bash
./mvnw test
```

---

## 📡 API Endpoints Reference

### 1. System & Health
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Web UI Dashboard |
| `GET` | `/api` or `/api/v1` | Complete API directory & service metadata |
| `GET` | `/api/v1/health` | System health status & database entity metrics |
| `GET` | `/actuator/health` | Spring Boot Actuator health endpoint |

### 2. Plaid Banking Sandbox Gateway
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/banking/sync` | Master sync: Fetches live data from Banking API and ingests into Database with simulated delay |
| `POST` | `/api/v1/banking/transactions/sync` | Fetches transactions from Banking API and persists directly into Database |
| `GET` | `/api/v1/banking/transactions` | Lists all transactions currently stored in the database |
| `GET` | `/api/v1/banking/token` | Returns active or newly generated sandbox access token |
| `GET` | `/api/v1/banking/accounts` | Fetches account information from Banking Sandbox |

### 3. RBI AA Protocol Sandbox
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/aa/consents` | Create a new ReBIT AA consent request |
| `GET` | `/api/v1/aa/consents/{consentId}` | Fetch status of consent |
| `POST` | `/api/v1/aa/consents/{consentId}/approve` | Verify OTP (default `123456`) and activate consent |
| `POST` | `/api/v1/aa/consents/{consentId}/revoke` | Revoke active consent |
| `POST` | `/api/v1/aa/fi/request` | Initiate financial information fetch session |
| `GET` | `/api/v1/aa/fi/fetch/{sessionId}` | Fetch decrypted ReBIT statement payload |
| `GET` | `/api/v1/aa/mock/raw` | Get raw multi-account mock dataset |

### 4. FIU Data Ingestion & Ledger
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/fiu/sync` | One-click full AA handshake and database sync |
| `GET` | `/api/v1/fiu/accounts` | List all synchronized bank accounts |
| `GET` | `/api/v1/fiu/accounts/{accountId}` | Get details of a single account |
| `GET` | `/api/v1/fiu/accounts/{accountId}/transactions` | Get transactions for a specific account |
| `GET` | `/api/v1/fiu/transactions` | Search & filter transactions (`search`, `category`, `txnType`, `page`, `size`) |
| `GET` | `/api/v1/fiu/analytics/overview` | Student financial overview (balances, burn rate, runway, breakdown) |

### 5. Real-Time Transaction Simulator
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/simulator/transactions/generate` | Generate an instant realistic transaction and update balances |
| `POST` | `/api/v1/simulator/stream/start?intervalSeconds=3` | Start continuous background transaction stream |
| `POST` | `/api/v1/simulator/stream/stop` | Stop background stream |
| `GET` | `/api/v1/simulator/stream/status` | Check if streaming is currently running |
| `GET` | `/api/v1/simulator/stream/events` | **Server-Sent Events (SSE)** live stream feed |

### 6. Student FinTech Calculators
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/tools/runway` | Scholarship runway calculator (`customMonthlyBurn`, `includeFdBuffer`) |
| `GET` | `/api/v1/tools/salary-sandbox` | Post-graduation CTC-to-in-hand salary simulator (`ctc`, `city`) |
| `GET` | `/api/v1/tools/roommate-splits` | Roommate shared expense ledger & split debts calculation (`totalRoommates`) |

---

## ⚡ Quick Start Workflow via cURL / PowerShell

```powershell
# 1. Check health
curl http://localhost:8085/api/v1/health

# 2. Synchronize Bank Accounts from RBI AA Sandbox
curl -X POST http://localhost:8085/api/v1/fiu/sync

# 3. Synchronize Live Banking Sandbox Data (Plaid)
curl -X POST http://localhost:8085/api/v1/banking/sync

# 4. View Aggregated Financial Overview & Runway
curl http://localhost:8085/api/v1/fiu/analytics/overview

# 5. Search Transactions (e.g., Swiggy, UPI, Dining)
curl "http://localhost:8085/api/v1/fiu/transactions?search=swiggy"

# 6. Generate Instant Simulated Transaction
curl -X POST http://localhost:8085/api/v1/simulator/transactions/generate

# 7. Test Student Salary Sandbox
curl "http://localhost:8085/api/v1/tools/salary-sandbox?ctc=1200000&city=Bangalore"
```
