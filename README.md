# Ledgr 🛡️💸
### *Privacy-First Student Financial Planning OS (RBI Account Aggregator Desktop Edition)*

![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-informational.svg)
![RBI AA Framework](https://img.shields.io/badge/RBI%20AA-ReBIT%20Standard-purple.svg)

**Ledgr** is a secure, JavaFX-based desktop fintech application engineered specifically for student personal financial planning. Operating as a **Financial Information User (FIU)** under the **Reserve Bank of India (RBI) Account Aggregator (AA)** framework, it natively streams encrypted, standardized financial data directly from banks—eliminating privacy risks through local statement processing and zero-cloud data storage.

---

## 🚀 Key Features

- **🏛️ RBI Account Aggregator (AA) Integration (Setu Gateway Standard)**:
  - Stepped cryptographic handshake (Consent Negotiation $\rightarrow$ OTP Signature $\rightarrow$ ECDH Key-Exchange $\rightarrow$ 256-bit Decryption).
  - Multi-account ReBIT ingestion: **Savings Accounts**, **Fixed Deposits** (tenure & compounding), and **Recurring Deposits** (installment schedules).
- **🛡️ Global Privacy Hotkey Shield (`Ctrl + Shift + H`)**:
  - Instant hardware-level UI masking that blurs balances (`₹•••,•••.••`) and account numbers to prevent shoulder-surfing in dorms and classrooms.
- **⚡ Real-Time Live Transaction Stream**:
  - Live background transaction pump that simulates realistic student cash flows (Swiggy, Ola, Amazon, Netflix subscriptions, University stipends, Freelance credits, and Roommate split IOUs).
- **🎓 Scholarship Runway Calculator**:
  - Projects safe burn-rate runway (in months) by analyzing historical living costs against liquid savings and fixed deposit buffers.
- **🏷️ Intelligent Student FinTech Categorizer**:
  - Automatically identifies UPI merchant handles (`swiggy@icici`, `simpl@axis`, `amzn@sbi`, `nps@trust`) into clean financial categories.
- **🤝 Roommate Escrow & Expense Splitter**:
  - Dedicated ledger for tracking shared rent, utilities, and grocery debts with auto-settlement detection.
- **💼 Post-Graduation Salary Sandbox**:
  - Interactive CTC-to-in-hand salary simulator factoring in New Tax Regime, EPF, and city cost-of-living indices (Bangalore, Mumbai, Delhi, Hyderabad, Pune).

---

## 🏗️ Architecture Overview

```mermaid
graph TD
    subgraph Desktop Process (JVM 21)
        subgraph JavaFX Native Window
            GUI[Ledgr Desktop GUI<br/>Dark Modern Interface]
            HOTKEY[Global Privacy Shield<br/>Ctrl + Shift + H Hook]
            SEARCH[Instant Filter Engine]
        end

        subgraph Embedded Spring Runtime
            FIU[FIU Data Ingestion Service]
            CAT[Fintech Category Engine]
            AA[Setu AA Sandbox Gateway<br/>Local In-Memory Protocol]
            SIM[Real-Time Transaction Pump]
        end
    end

    subgraph Local Storage
        DB[(Local PostgreSQL ledgr_db<br/>Zero Data Leakage)]
    end

    GUI --> FIU
    HOTKEY --> GUI
    SEARCH --> GUI
    FIU --> AA
    SIM --> DB
    SIM --> GUI
    FIU --> CAT --> DB
    DB --> GUI
```

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java 21 (LTS) |
| **Desktop GUI** | JavaFX 21 (OpenJFX) + Modern CSS Design System |
| **Backend & IoC** | Spring Boot 3.3.4 (Embedded in-process runtime) |
| **Data Persistence** | Spring Data JPA + Hibernate 6 + PostgreSQL 18 (with H2 fallback) |
| **JSON Serialization** | Jackson Data Processor (JSR310 Datatype) with lenient ReBIT aliasing |
| **Boilerplate Reduction** | Project Lombok |
| **Build Tool** | Apache Maven 3.9+ |

---

## 🏁 Getting Started

### 1. Prerequisites
- **Java 21 JDK** installed (`java -version`)
- **PostgreSQL** running on port `5432` (Default database: `ledgr_db`)

### 2. Configuration
Verify database credentials in [`src/main/resources/application.yml`](src/main/resources/application.yml):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ledgr_db
    username: postgres
    password: YOUR_PASSWORD
```

### 3. Launching the Desktop Application
Run using the included Maven Wrapper:
```powershell
# Windows
.\mvnw.cmd javafx:run

# Linux / macOS
./mvnw javafx:run
```

### 4. Running Automated Tests
```powershell
.\mvnw.cmd test
```

---

## 📂 Project Structure

```
Ledgr/
├── .tools/                               # Local portable build tools
├── src/
│   ├── main/
│   │   ├── java/com/ledgr/sandbox/
│   │   │   ├── config/                   # PostgreSQL pre-check & DB initializers
│   │   │   ├── controller/               # Setu AA & FIU REST endpoints
│   │   │   ├── dto/
│   │   │   │   ├── aa/                   # Setu AA protocol DTOs (Consent, FI Session)
│   │   │   │   └── rebit/                # ReBIT standard financial schemas
│   │   │   ├── entity/                   # JPA Entities (Accounts, Transactions, Consents)
│   │   │   ├── repository/               # Spring Data JPA repositories
│   │   │   ├── service/                  # AA Gateway simulator, Ingestion & Live Pump
│   │   │   ├── ui/                       # JavaFX Desktop Application & View Components
│   │   │   └── util/                     # Merchant & student transaction categorizers
│   │   └── resources/
│   │       ├── static/index.html         # Embedded web test console (Port 8085)
│   │       ├── styles/ledgr-dark.css     # Premium dark-theme JavaFX stylesheet
│   │       ├── application.yml           # Primary PostgreSQL configuration
│   │       └── application-h2.yml        # In-memory test profile
│   └── test/                             # Unit & End-to-End integration test suite
├── designmode_sample.json                # ReBIT multi-account mock dataset
├── pom.xml                               # Maven project dependencies & plugins
├── .gitignore                            # Git ignore rules
└── README.md                             # Project documentation
```

---

## ⌨️ Desktop Shortcuts

- **`Ctrl + Shift + H`**: Toggle **Privacy Shield** (Instantly masks all balances and account numbers).
- **`⚡ Sync AA Gateway`**: Executes the 5-step ReBIT Account Aggregator statement fetch and PostgreSQL ingestion.
- **`🔴 / 🟢 Stream Toggle`**: Starts/Stops the real-time background student transaction simulator (3s interval).

---

## 📄 License

This project is developed for student financial literacy and laboratory research. Built under the **RBI Account Aggregator Guidelines**.
