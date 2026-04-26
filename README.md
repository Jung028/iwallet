# iWallet Business Service

iWallet Business Service is a high-concurrency, event-driven digital wallet backend built on the **SOFABoot** framework. It provides core business logic for wallet operations, including fund transfers, top-ups via Stripe, QR code generation, and transaction management.

## 🚀 Features

- **Fund Transfers**: Two-step transfer process (Init & Confirm) with security validation.
- **Top-Up System**: Integration with **Stripe** for processing card payments and handling asynchronous webhooks.
- **QR Code Engine**: Dynamic generation of QR codes for payments and user identification.
- **Transaction Management**: Detailed querying of transaction history and balance.
- **Idempotency Guard**: Robust idempotency key management to ensure financial consistency across retries.
- **Event-Driven Architecture**: Uses **Kafka** for asynchronous processing of transaction events.
- **High Availability**: Built on SOFABoot with SOFA RPC for distributed service communication and Nacos for service discovery.

## 🛠 Tech Stack

- **Framework**: [SOFABoot](https://github.com/sofastack/sofa-boot) 4.6.0 (based on Spring Boot 3.3.2)
- **Database**: PostgreSQL
- **ORM**: MyBatis with MyBatis Generator
- **Messaging**: Apache Kafka
- **Service Discovery**: Nacos
- **Security**: Spring Security with JWT-based authentication
- **Payment Gateway**: Stripe SDK
- **Observability**: Spring Boot Actuator & Logback

## 📂 Project Structure

```text
├── app/
│   ├── bootstrap/          # Application entry point and startup configuration
│   ├── web/                # REST Controllers, Filters, and Web Security
│   ├── biz/
│   │   ├── shared/         # Shared business logic
│   │   └── service/impl/   # Business service implementations
│   ├── core/
│   │   ├── model/          # Domain models, Enums, and Converters
│   │   └── service/        # Core domain services
│   ├── common/
│   │   ├── dal/            # Data Access Layer (MyBatis Mappers & DOs)
│   │   ├── service/facade/ # API Facades and DTOs
│   │   ├── service/integration/ # External system integrations
│   │   └── util/           # Shared utilities (Log, Event, Tenant)
│   └── test/               # Test suites
├── kafka-docker/           # Docker Compose for local Kafka/Zookeeper
└── pom.xml                 # Root Maven configuration
```

## 🚦 Getting Started

### Prerequisites

- **JDK 17** or higher
- **Maven 3.8+**
- **PostgreSQL 14+**
- **Docker & Docker Compose** (for Kafka)
- **Nacos Server** (running on `localhost:8848`)

### Setup

1. **Database**: Create a database named `wallet` in PostgreSQL.
   ```sql
   CREATE DATABASE wallet;
   ```
   Update `app/web/src/main/resources/application.properties` with your PostgreSQL credentials.

2. **Infrastructure**: Start Kafka and Zookeeper using Docker Compose.
   ```bash
   cd kafka-docker
   docker-compose up -d
   ```

3. **Nacos**: Ensure Nacos is running.
   ```bash
   # Quick start with Docker
   docker run --name nacos-standalone -e MODE=standalone -p 8848:8848 -d nacos/nacos-server:v2.2.3
   ```

4. **Build**: Install dependencies and build the project.
   ```bash
   mvn clean install
   ```

5. **Run**: Start the application from the bootstrap module.
   ```bash
   mvn -pl app/bootstrap spring-boot:run
   ```

## 🔗 API Endpoints

| Category | Endpoint | Description |
| :--- | :--- | :--- |
| **Transfer** | `/business/basic/transferInit.json` | Initialize a fund transfer |
| | `/business/basic/transferConfirm.json` | Confirm a fund transfer |
| **Top-Up** | `/business/basic/topup/createTopUpIntent.json` | Create a Stripe Payment Intent |
| | `/business/basic/topup/chargeCard.json` | Charge a card directly |
| | `/business/topup/webhook/stripe` | Stripe Webhook handler |
| **QR Code** | `/business/qr/generateQrCode.json` | Generate payment/identity QR code |
| **Balance** | `/business/basic/queryBalance.json` | Check user wallet balance |
| **Transactions**| `/business/basic/queryTransactionHistory.json` | Query user transaction list |

## 🛡 Security

The application uses Spring Security. For development:
- **Default Username**: `admin`
- **Default Password**: `devtest123`
- JWT authentication is applied to most business endpoints. The secret key is managed via `transfer.token.secret`.

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.
