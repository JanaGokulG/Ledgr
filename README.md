# Ledgr 🛡️💸
### *RBI Account Aggregator (AA) & Student FinTech Desktop Platform*

![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Ready-informational.svg)
![H2 Database](https://img.shields.io/badge/H2-In--Memory%20Fallback-blue.svg)
![RBI AA Framework](https://img.shields.io/badge/RBI%20AA-ReBIT%20Standard-purple.svg)

**Ledgr** is a secure, native **JavaFX desktop fintech application** and **Spring Boot REST API engine** engineered for student personal financial planning. Operating under the **Reserve Bank of India (RBI) Account Aggregator (AA)** framework, Ledgr streams encrypted, standardized financial data directly from bank accounts into a local database, eliminating privacy risks through local statement processing.

The user interface is built with an authentic, professional dark fintech design system (no HTML/browser dependencies, no generic AI styling), featuring live balance streaming, bank non-maintenance protection, proactive auto-renewal early warnings, scholarship runway modeling, and post-graduation salary simulation.

---

## 📑 Table of Contents
1. [Prerequisites & System Requirements](#-prerequisites--system-requirements)
2. [Detailed Steps to Run](#-detailed-steps-to-run)
3. [Comprehensive Feature Guide & Explanations](#-comprehensive-feature-guide--explanations)
   - [Feature 1: Accounts & Net Worth Overview](#1-accounts--net-worth-overview)
   - [Feature 2: Live Ledger & Transaction Feed](#2-live-ledger--transaction-feed)
   - [Feature 3: Auto-Renewal Early Warnings](#3-auto-renewal-early-warnings-distinct-feature)
   - [Feature 4: Minimum Balance Shield](#4-minimum-balance-shield-distinct-feature)
   - [Feature 5: Scholarship Runway & CTC Salary Sandbox](#5-scholarship-runway--ctc-salary-sandbox)
   - [Feature 6: RBI AA Gateway & Real-Time Simulator](#6-rbi-aa-gateway--real-time-simulator)
   - [Feature 7: Global Privacy Hotkey Shield (Ctrl+Shift+H)](#7-global-privacy-hotkey-shield-ctrlshifth)
4. [Where Does the Data Come From? (Data Provenance & Storage)](#-where-does-the-data-come-from-data-provenance--storage)
5. [REST API Directory & Quick-Start](#-rest-api-directory--quick-start)
6. [Automated Test Suite](#-automated-test-suite)
7. [Troubleshooting & FAQ](#-troubleshooting--faq)

---

## 💻 Prerequisites & System Requirements

Before running Ledgr, ensure you have the following installed:

- **Java Development Kit (JDK) 21 LTS** or higher (Oracle JDK or Eclipse Temurin OpenJDK 21).
  Verify with:
  ```bash
  java -version
  ```
- **Operating System**: Windows 10/11, macOS, or Linux.
- **Maven 3.9+** (Optional — the repository includes the pre-configured Apache Maven Wrapper `mvnw.cmd` and `./mvnw`).
- **PostgreSQL** (Optional — Ledgr automatically falls back to an embedded in-memory H2 database running in PostgreSQL compatibility mode if no local PostgreSQL server is active).

---

## 🚀 Detailed Steps to Run

### Step 1: Clone and Navigate to the Repository
```powershell
cd "d:\college related\SEM5\Laboratory courses\Java lab"
```

### Step 2: Configure Environment Variables (Optional)
Ledgr comes pre-configured with zero-friction defaults. If you wish to customize database credentials or Plaid sandbox keys, configure a `.env` file in the root folder (or reference [`.env.example`](file:///.env.example)):

```env
# Server Port (Spring Boot REST Engine)
SERVER_PORT=8085

# Database Configuration (Defaults to local PostgreSQL with automatic H2 fallback)
DB_URL=jdbc:postgresql://localhost:5432/ledgr_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Banking Sandbox Credentials (Optional / Pre-configured for offline sandbox)
BANKING_API_ENV=sandbox
TRANSACTION_DELAY_MS=150
```

> [!NOTE]
> The application uses [`DotenvLoader`](file:///src/main/java/com/ledgr/config/DotenvLoader.java) to load `.env` variables automatically on boot.

---

### Step 3: Launch the Application

#### Option A: Run JavaFX Desktop Client (Recommended)
This runs the full native JavaFX desktop application alongside the Spring Boot backend service:

**Windows (PowerShell or Command Prompt):**
```powershell
.\mvnw.cmd javafx:run
```

**Linux / macOS:**
```bash
./mvnw javafx:run
```

#### Option B: Run via Spring Boot Plugin
```powershell
.\mvnw.cmd spring-boot:run
```

#### Option C: Build a Standalone Packaged JAR
To compile and package the application into a single executable JAR:
```powershell
# 1. Compile and package (skipping test execution for faster build)
.\mvnw.cmd clean package -DskipTests

# 2. Run the packaged JAR
java -jar target/ledgr-backend-1.0.0-SNAPSHOT.jar
```

#### Option D: Run Headless (REST API Only, without GUI)
If running on a remote Linux server or Docker container without a display:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.main.headless=true"
```

---

## 🌟 Comprehensive Feature Guide & Explanations

Ledgr is structured into distinct, modular financial intelligence and protection tools designed specifically for college students:

```
┌───────────────────────────────────────────────────────────────────────────┐
│                                 LEDGR                                     │
├────────────────────┬──────────────────────────────────────────────────────┤
│ FINANCIAL HUB      │ 1. Accounts & Net Worth (Multi-Account Overview)     │
│                    │ 2. Live Ledger & Transactions (Categorized Feed)     │
├────────────────────┼──────────────────────────────────────────────────────┤
│ BANKING PROTECTION │ 3. Auto-Renewal Early Warnings (7-Day Buffer Alerts) │
│                    │ 4. Minimum Balance Shield (MAB Breach Protection)    │
├────────────────────┼──────────────────────────────────────────────────────┤
│ PLANNING & TOOLS   │ 5. Scholarship Runway & Post-Grad CTC Sandbox        │
│                    │ 6. AA Gateway & Real-Time Transaction Simulator      │
├────────────────────┼──────────────────────────────────────────────────────┤
│ GLOBAL SECURITY    │ 7. Global Privacy Hotkey Shield (Ctrl + Shift + H)   │
└────────────────────┴──────────────────────────────────────────────────────┘
```

---

### 1. Accounts & Net Worth Overview

- **What it does**:
  Provides an aggregated summary of all assets held by the student across multiple linked accounts.
  - **Total Net Worth**: The exact mathematical sum of spendable savings, fixed deposits, and recurring deposits:
    $$\text{Net Worth} = \text{Liquid Savings} + \text{Fixed Deposit Buffer} + \text{Recurring Deposits}$$
  - **Liquid Savings**: Cumulative balance of all active checking/savings accounts available for immediate UPI transfers.
  - **Fixed Deposit Buffer**: Safe, locked emergency capital yielding guaranteed interest (e.g. 7.1% p.a.).
  - **Recurring Deposits (RD)**: Monthly compounding student savings deposits.
  - **Linked Account Cards**: Detailed cards showing masked account numbers, branch IFSC, holder name, nominee status, interest rates, and CKYC validation status.
- **Why students need it**:
  College students frequently juggle multiple bank accounts (e.g., student zero-balance accounts, scholarship disbursement accounts, and family savings accounts). This view centralizes total liquidity in one verified dashboard.
- **Where the data is from**:
  - **Source**: Directly queried from the `bank_accounts` database table ([`AccountEntity`](file:///src/main/java/com/ledgr/entity/AccountEntity.java)) via [`AccountRepository`](file:///src/main/java/com/ledgr/repository/AccountRepository.java).
  - **Seeding / Sync**: Initially ingested via the RBI Account Aggregator sandbox (`student@setuaa`) during initialization or refreshed on demand.

---

### 2. Live Ledger & Transaction Feed

- **What it does**:
  - Displays a chronological, searchable, and filterable ledger of all financial inflows and debits.
  - Each transaction displays timestamp, narration, merchant/payee tag, category (e.g., `FOOD_DINING`, `SUBSCRIPTIONS`, `TRAVEL_COMMUTE`, `ESCROW_SPLIT`), payment mode (`UPI`, `CARD`, `FT`, `ATM`), credit/debit badge, transaction amount, and the running account balance after transaction execution.
  - Includes a real-time search bar that filters merchants, categories, and narrations as you type, along with transaction type filters.
- **Why students need it**:
  Micro-transactions via UPI (chai, canteen, campus printing, auto rides) drain student budgets without visibility. The live ledger categorizes every rupee and displays running balance changes.
- **Where the data is from**:
  - **Source**: Queried directly from the `bank_transactions` database table ([`TransactionEntity`](file:///src/main/java/com/ledgr/entity/TransactionEntity.java)) via [`TransactionRepository`](file:///src/main/java/com/ledgr/repository/TransactionRepository.java).
  - **Updates**: Continuously generated and updated in real-time by the background [`RealtimeTransactionSimulatorService`](file:///src/main/java/com/ledgr/sandbox/service/RealtimeTransactionSimulatorService.java).

---

### 3. Auto-Renewal Early Warnings (Distinct Feature)

- **What it does**:
  - Continuously monitors active recurring mandates, UPI AutoPay, and NACH e-mandates (e.g., Netflix Student, Spotify Premium, Campus Wi-Fi & Maintenance, Simpl BNPL tab, Gym Membership, RD Installments, Term Health Insurance, and GitHub Copilot).
  - **7-Day Advance Notice**: Specifically filters and flags every recurring commitment scheduled to debit within the next **7 days**.
  - **Warning Headline**: Compares the sum of imminent renewals against the funding account's liquid balance. If the balance is insufficient, it displays an urgent critical warning indicating the exact deficit amount required before debit dates to prevent bank bounce charges (₹250–₹500 penalty per bounce) and protect credit scores.
- **Why students need it**:
  Subscriptions and BNPL bills renew silently. When auto-debits bounce on an empty student account, banks penalize the account with heavy non-maintenance and ECS bounce charges.
- **Where the data is from**:
  - **Source**: Queried from the dedicated `recurring_renewals` database table ([`RecurringRenewalEntity`](file:///src/main/java/com/ledgr/entity/RecurringRenewalEntity.java)) via [`RecurringRenewalRepository`](file:///src/main/java/com/ledgr/repository/RecurringRenewalRepository.java) using `findByActiveTrueOrderByDueDateAsc()`.

---

### 4. Minimum Balance Shield (Distinct Feature)

- **What it does**:
  - Dedicated bank account protection engine that enforces compliance with the bank's **Minimum Average Balance (MAB)** requirement (configurable: ₹500, ₹1,000, ₹2,500, or ₹5,000).
  - **Chronological Deduction Trajectory**: Projects the exact step-by-step running balance as upcoming recurring debits execute over the next 7 and 30 days.
  - **Breach Detection**: Flags any future transaction that causes the balance to breach the minimum threshold or drop into negative overdraft.
  - **Safety Top-Up Calculator**: Calculates the exact recommended deposit amount to maintain the minimum balance and avoid bank non-maintenance fees.
  - **Simulation Bar**: Allows students to test custom balance scenarios (e.g., *"What happens if my balance drops to ₹1,200?"*) without altering real database records.
- **Why students need it**:
  Most Indian private and public banks levy quarterly non-maintenance fees if the balance dips below the MAB threshold. This shield prevents fees before debits occur.
- **Where the data is from**:
  - **Source**: Dynamically computed by [`FintechToolsController.getMinBalanceShield(...)`](file:///src/main/java/com/ledgr/sandbox/controller/FintechToolsController.java) by evaluating real balances in `bank_accounts` against active commitments in `recurring_renewals`.

---

### 5. Scholarship Runway & CTC Salary Sandbox

#### A. Scholarship & Liquidity Runway Calculator
- **What it does**:
  Calculates how many months the student can survive without additional income based on current liquid savings and their monthly burn rate:
  $$\text{Runway (Months)} = \frac{\text{Liquid Savings} + (\text{FD Buffer if toggled})}{\text{Monthly Burn Rate}}$$
  - Computes initial burn rate automatically from the student's actual average debit history in the database (`transactionRepository.getTotalDebits() / 6`).
  - Includes a checkbox toggle to factor in Fixed Deposit emergency reserves.
  - Classifies safety status: `COMFORTABLE` ($\ge 6\text{ months}$), `MODERATE` ($3\text{--}6\text{ months}$), or `CRITICAL` ($< 3\text{ months}$).

#### B. Post-Graduation Salary Sandbox (New Tax Regime FY 2024-25)
- **What it does**:
  Simulates in-hand take-home pay from campus placement offer letters:
  - Takes Annual Cost to Company (CTC) (e.g. ₹12,00,000) and target placement city.
  - Applies Indian statutory New Tax Regime tax slabs, ₹75,000 standard deduction, and 12% Employee Provident Fund (EPF).
  - Deducts estimated city living cost indices (Bangalore: ₹35,000/mo, Mumbai: ₹45,000/mo, Delhi: ₹32,000/mo, Hyderabad: ₹28,000/mo, Pune: ₹27,000/mo).
  - Outputs: Monthly Gross, Monthly Net In-Hand, Annual Tax, and Projected Monthly Savings.
- **Where the data is from**:
  - Runway is calculated from live `bank_accounts` and `bank_transactions` database records.
  - Salary sandbox is calculated using statutory Indian Union Budget 2024 tax rules and verified city living cost metrics.

---

### 6. RBI AA Gateway & Real-Time Simulator

- **What it does**:
  - **AA 5-Phase Cryptographic Handshake**: Simulates complete end-to-end RBI Account Aggregator protocol flows:
    1. Consent Negotiation (defines FI Types, data frequency, date ranges).
    2. User Consent Approval & OTP Verification.
    3. FIP Banking Key-Exchange & Session Initiation.
    4. Encrypted JSON Data Retrieval.
    5. Local FIU Decryption and Database Ingestion.
  - **Real-Time Transaction Stream**: A background engine generating realistic student transactions (Swiggy, Zomato, Blinkit, Ola, BookMyShow, DRDO stipend, freelance payments) with random delays between 1.2s and 3.2s.
  - Features pause/resume stream controls and an instant "Inject 1 Mock Transaction" button.
- **Where the data is from**:
  - Ingestion transforms ReBIT Account Aggregator standardized JSON payloads into `AccountEntity` and `TransactionEntity` database rows.
  - Simulated transactions update account balances in `bank_accounts` and append to `bank_transactions`.

---

### 7. Global Privacy Hotkey Shield (`Ctrl + Shift + H`)

- **What it does**:
  - Instantly obfuscates all financial metrics, account balances, and transaction amounts across the entire application, replacing numbers with `₹ ••••••`.
  - Can be activated/deactivated at any time using the global shortcut **`Ctrl + Shift + H`** or by clicking the Privacy button in the header.
- **Why students need it**:
  Allows students to manage finances in public libraries, classrooms, or hostel rooms without exposing balances to shoulder-surfing.
- **Where the data is from**:
  - Client-side reactive JavaFX state (`SimpleBooleanProperty`) that intercepts value formatters.

---

## 🗄️ Where Does the Data Come From? (Data Provenance & Storage)

All data in Ledgr is persisted in relational database tables. No figures are hardcoded in memory or UI templates.

```
                    ┌────────────────────────────────────────┐
                    │    DATA SOURCES & EXTERNAL SCHEMAS     │
                    └───────────────────┬────────────────────┘
                                        │
           ┌────────────────────────────┴────────────────────────────┐
           ▼                                                         ▼
┌──────────────────────────────┐                          ┌──────────────────────┐
│  RBI Account Aggregator      │                          │ Realtime Transaction │
│  Sandbox (ReBIT Standard)    │                          │ Simulator Engine     │
│  • Account Profiles          │                          │ • UPI & Canteen Txns │
│  • Term & Recurring Deposits │                          │ • Parent Top-ups     │
└──────────────┬───────────────┘                          └──────────┬───────────┘
               │                                                     │
               └──────────────────────┬──────────────────────────────┘
                                      │ Ingestion & JPA Persistence
                                      ▼
             ┌──────────────────────────────────────────────────┐
             │       LOCAL RELATIONAL DATABASE ENGINE           │
             │       (PostgreSQL / Zero-Config H2 Fallback)     │
             ├──────────────────────────────────────────────────┤
             │ • bank_accounts        (AccountEntity)           │
             │ • bank_transactions    (TransactionEntity)       │
             │ • recurring_renewals   (RecurringRenewalEntity)  │
             │ • aa_consents          (ConsentEntity)           │
             │ • aa_fi_sessions       (FiSessionEntity)         │
             └────────────────────────┬─────────────────────────┘
                                      │
               ┌──────────────────────┴─────────────────────────┐
               │ Spring Data JPA Repositories & REST Services   │
               └──────────────────────┬─────────────────────────┘
                                      │
                                      ▼
               ┌────────────────────────────────────────────────┐
               │   Native JavaFX Desktop UI (Dark Theme)        │
               │   • Accounts & Net Worth Overview              │
               │   • Live Ledger Table                          │
               │   • Auto-Renewal Early Warnings Banner         │
               │   • Minimum Balance Shield Trajectory          │
               │   • Scholarship Runway & CTC Sandbox           │
               └────────────────────────────────────────────────┘
```

### Database Tables & Entities:
1. **`bank_accounts`** ([`AccountEntity`](file:///src/main/java/com/ledgr/entity/AccountEntity.java)):
   Stores account number, branch IFSC, holder details, PAN, CKYC compliance, currency, account type (`SAVINGS`, `FIXED`, `RECURRING`), interest rate, maturity amount, and current balance.
2. **`bank_transactions`** ([`TransactionEntity`](file:///src/main/java/com/ledgr/entity/TransactionEntity.java)):
   Stores transaction reference ID, narration, payee, category, payment mode (`UPI`, `CARD`, `FT`, `ATM`), debit/credit type, amount, and the post-transaction balance.
3. **`recurring_renewals`** ([`RecurringRenewalEntity`](file:///src/main/java/com/ledgr/entity/RecurringRenewalEntity.java)):
   Stores subscription name, category, monthly amount, due date, funding account number, mandate type (`UPI_AUTOPAY`, `NACH_E_MANDATE`, etc.), and compliance advisory notes.
4. **`aa_consents` & `aa_fi_sessions`** ([`ConsentEntity`](file:///src/main/java/com/ledgr/sandbox/entity/ConsentEntity.java), [`FiSessionEntity`](file:///src/main/java/com/ledgr/sandbox/entity/FiSessionEntity.java)):
   Stores cryptographic AA consent artifacts, session tokens, and digital signatures.

---

## 📡 REST API Directory & Quick-Start

In addition to the JavaFX desktop interface, Ledgr exposes a full REST API on port **`8085`**:

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/health` | System health, database connectivity, and record counts |
| `POST` | `/api/v1/fiu/sync` | Executes end-to-end AA handshake and database sync |
| `GET` | `/api/v1/fiu/accounts` | Lists all synchronized bank accounts from database |
| `GET` | `/api/v1/fiu/transactions` | Filterable, paginated transaction ledger |
| `GET` | `/api/v1/tools/auto-renewals` | **Feature 1**: Auto-renewals with 7-day advance early warnings |
| `GET` | `/api/v1/tools/min-balance-shield` | **Feature 2**: Minimum Balance Shield breach trajectory |
| `GET` | `/api/v1/tools/runway` | Scholarship runway calculation from database accounts |
| `GET` | `/api/v1/tools/salary-sandbox` | Post-grad CTC take-home salary simulator |
| `POST` | `/api/v1/simulator/transactions/generate` | Injects one realistic simulated transaction |
| `GET` | `/api/v1/simulator/stream/events` | Server-Sent Events (SSE) live transaction feed |

### Quick cURL Test Commands:
```powershell
# 1. Health check
curl http://localhost:8085/api/v1/health

# 2. Check Auto-Renewal Early Warnings
curl "http://localhost:8085/api/v1/tools/auto-renewals?lookaheadDays=30"

# 3. Check Minimum Balance Shield
curl "http://localhost:8085/api/v1/tools/min-balance-shield?minBalanceThreshold=1000"

# 4. Simulate Salary in Bangalore
curl "http://localhost:8085/api/v1/tools/salary-sandbox?ctc=1200000&city=Bangalore"
```

---

## 🧪 Automated Test Suite

To run the automated integration tests verifying database persistence, ReBIT AA ingestion, and warning logic:

**Windows:**
```powershell
.\mvnw.cmd test
```

**Linux / macOS:**
```bash
./mvnw test
```

### Verified Test Classes:
- [`BankingSyncServiceTest`](file:///src/test/java/com/ledgr/BankingSyncServiceTest.java): Verifies multi-endpoint Plaid/banking sandbox synchronization, schema mapping, and database transaction insertion.
- [`RenewalWarningsTest`](file:///src/test/java/com/ledgr/RenewalWarningsTest.java): Verifies 7-day advance notice calculations, buffer deficits, MAB breach detection, and top-up recommendations.

---

## 🔧 Troubleshooting & FAQ

#### 1. Why does the terminal show strange characters like `Γé╣` instead of `₹`?
- **Cause**: Windows default terminal codepage is CP437 or Windows-1252.
- **Solution**: Run `chcp 65001` in your terminal prior to running Maven, or use Windows Terminal / PowerShell 7. The JavaFX GUI uses native Unicode escape sequences (`\u20B9`) and displays correctly regardless of terminal codepage.

#### 2. Port `8085` or `8080` is already in use
- Change the port in your `.env` file (`SERVER_PORT=8090`) or run:
  ```powershell
  .\mvnw.cmd javafx:run -Dspring-boot.run.arguments="--server.port=8090"
  ```

#### 3. Do I need to install or configure PostgreSQL?
- **No**. If PostgreSQL is not running locally, Ledgr automatically detects the absence and initializes an in-memory **H2 database** with full PostgreSQL compatibility syntax.

#### 4. Can I pause the live transaction stream?
- **Yes**. Navigate to the **AA Sandbox & Realtime Simulator** tab in the desktop application and click **"Pause Simulator Stream"**, or make an HTTP POST call to `/api/v1/simulator/stream/stop`.
