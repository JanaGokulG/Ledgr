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

### 1. Run with Maven Wrapper
From the project root:
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Or standard Maven
mvn spring-boot:run
```

The web server will start on port **`8085`** at `http://localhost:8085`.

### 2. Run Automated Tests
```powershell
.\mvnw.cmd test
```

---

## 📡 API Endpoints Reference

### 1. System & Health
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` or `/api` or `/api/v1` | Complete API directory & service metadata |
| `GET` | `/api/v1/health` | System health status & database entity metrics |
| `GET` | `/actuator/health` | Spring Boot Actuator health endpoint |

### 2. RBI AA Protocol Sandbox
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/aa/consents` | Create a new ReBIT AA consent request |
| `GET` | `/api/v1/aa/consents/{consentId}` | Fetch status of consent |
| `POST` | `/api/v1/aa/consents/{consentId}/approve` | Verify OTP (default `123456`) and activate consent |
| `POST` | `/api/v1/aa/consents/{consentId}/revoke` | Revoke active consent |
| `POST` | `/api/v1/aa/fi/request` | Initiate financial information fetch session |
| `GET` | `/api/v1/aa/fi/fetch/{sessionId}` | Fetch decrypted ReBIT statement payload |
| `GET` | `/api/v1/aa/mock/raw` | Get raw multi-account mock dataset |

### 3. FIU Data Ingestion & Ledger
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/fiu/sync` | One-click full AA handshake and database sync |
| `GET` | `/api/v1/fiu/accounts` | List all synchronized bank accounts |
| `GET` | `/api/v1/fiu/accounts/{accountId}` | Get details of a single account |
| `GET` | `/api/v1/fiu/accounts/{accountId}/transactions` | Get transactions for a specific account |
| `GET` | `/api/v1/fiu/transactions` | Search & filter transactions (`search`, `category`, `txnType`, `page`, `size`) |
| `GET` | `/api/v1/fiu/analytics/overview` | Student financial overview (balances, burn rate, runway, breakdown) |

### 4. Real-time Transaction Simulator
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/simulator/transactions/generate` | Generate an instant realistic transaction and update balances |
| `POST` | `/api/v1/simulator/stream/start?intervalSeconds=3` | Start continuous background transaction stream |
| `POST` | `/api/v1/simulator/stream/stop` | Stop background stream |
| `GET` | `/api/v1/simulator/stream/status` | Check if streaming is currently running |
| `GET` | `/api/v1/simulator/stream/events` | **Server-Sent Events (SSE)** live stream feed |

### 5. FinTech Tools
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

# 3. View Aggregated Financial Overview & Runway
curl http://localhost:8085/api/v1/fiu/analytics/overview

# 4. Search Transactions (e.g., Swiggy, UPI, Dining)
curl "http://localhost:8085/api/v1/fiu/transactions?search=swiggy"

# 5. Generate Instant Simulated Transaction
curl -X POST http://localhost:8085/api/v1/simulator/transactions/generate

# 6. Test Salary Sandbox
curl "http://localhost:8085/api/v1/tools/salary-sandbox?ctc=1200000&city=Bangalore"
```
