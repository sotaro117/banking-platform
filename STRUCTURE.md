# Private Payment & Wallet Platform — Structure

> **Scope note:** this is a simulation of a company-internal payment orchestration
> system built to learn microservice architecture, not a production
> money-transmission service. Real fund movement/card issuing is delegated to a
> Banking-as-a-Service sandbox (Stripe Treasury/Issuing test mode) rather than
> implemented directly — this is called out explicitly to avoid implying real
> regulatory/licensing compliance.

## 1. High-Level Concept

A single company (the "tenant") uses this platform to manage internal wallets
(employees, vendors, departments, the company's own operating wallet) and
distribute payments out to external rails via a **Banking-as-a-Service**
provider, all coordinated through a central, auditable, event-driven ledger.

Built as **true microservices** (database-per-service), not a monolith —
this is a deliberate learning choice, so the design leans into problems
(distributed transactions, sagas, service discovery) a monolith would avoid.

_saga_: a design method used to manage data consistency and a sequence of independent steps across distributed microservices during multi-step financial transactions

## 2. Architecture

```
                          ┌───────────────────────┐
                          │   API Gateway           │
                          │   (Spring Cloud Gateway) │
                          └───────────┬─────────────┘
                                      │
      ┌────────────────────┬─────────┴──────────┬────────────────────┐
      │                    │                     │                    │
┌─────▼──────┐     ┌───────▼────────┐    ┌───────▼───────┐    ┌───────▼───────┐
│ Ledger       │     │ Payment          │    │ Audit          │    │ Notification   │
│ Service      │     │ Orchestrator     │    │ Service        │    │ Service        │
│ (own DB)     │     │ (own DB)         │    │ (own store)    │    │ (no DB)        │
└─────┬──────┘     └───────┬────────┘    └───────▲───────┘    └───────▲───────┘
      │                    │                     │                    │
      │            ┌───────▼────────┐            │                    │
      │            │ Connector Layer  │            │                    │
      │            │ RailAdapter      │            │                    │
      │            │  → StripeAdapter │            │                    │
      │            └───────┬────────┘            │                    │
      │                    │                     │                    │
      │            ┌───────▼────────┐            │                    │
      │            │ Stripe Treasury/ │            │                    │
      │            │ Issuing (sandbox)│            │                    │
      │            └────────────────┘            │                    │
      │                                            │                    │
      └──────────────► Kafka (MSK) ────────────────┴────────────────────┘
                  topics: ledger.entries, payments.lifecycle, payments.dlq
                          │
                  RabbitMQ (Amazon MQ) ─── notifications.email, receipt-pdf

Cross-cutting: Resilience4j (circuit breakers on inter-service calls)
               Micrometer Tracing + Zipkin / X-Ray (trace ID per request)
               Service discovery: Consul (or Eureka, for learning the classic pattern)
```

![alt general scheme](./doc-img/bank-system-portfolio-general.pdf)

## 3. Services

| Service                  | Owns                      | Responsibility                                                                                                                     | Talks to                                              |
| ------------------------ | ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| **API Gateway**          | nothing (stateless)       | Routing, auth, rate limiting at the edge                                                                                           | All services                                          |
| **Ledger Service**       | own Postgres DB           | Source of truth for wallets + double-entry ledger. Never trusts a caller's stated balance — always derives it from ledger entries. | Its DB, Kafka (publishes `ledger.entries`)            |
| **Payment Orchestrator** | own Postgres DB           | Owns the payment lifecycle state machine + saga coordination. Enforces idempotency.                                                | Its DB, Ledger Service (REST), Connector Layer, Kafka |
| **Connector Layer**      | no DB                     | One adapter per rail behind `RailAdapter` interface. Currently: `StripeBankAdapter` (Treasury/Issuing sandbox).                    | Stripe API                                            |
| **Audit Service**        | own store (Kafka-derived) | Read-only. Rebuilds queryable history by consuming Kafka. Never writes back to other services.                                     | Kafka (consumes), optional OpenSearch                 |
| **Notification Service** | no DB                     | Sends emails, generates receipt PDFs. Decoupled via RabbitMQ so a slow provider never blocks a payment.                            | RabbitMQ, S3                                          |

**Database-per-service is intentional.** No service reaches into another's tables.
This is what makes it a real microservice split instead of a distributed
monolith — and it's why the saga pattern below exists (you lose cross-service
ACID transactions once DBs are separate).

## 4. Chart of Accounts (Wallet Types)

Standard double-entry accounting has five account categories. `wallets.account_type`
reflects this rather than just "who owns it":

| account_type | Represents                                 | Example                                         |
| ------------ | ------------------------------------------ | ----------------------------------------------- |
| `ASSET`      | Money the company actually holds           | Company operating wallet                        |
| `LIABILITY`  | Money owed but not yet paid out            | Employee wallet before payout (accrued, unpaid) |
| `REVENUE`    | Income recognized, not a spendable balance | "Service Revenue" account                       |
| `EXPENSE`    | Money spent, categorized                   | "Payroll Expense", "Vendor Payments"            |

**Example: company receives customer income**
`transaction` with two `ledger_entries`: **DEBIT** company operating wallet (asset ↑),
**CREDIT** `REVENUE` account (income recognized). The two net to zero.

**Example: payroll obligation, before it's actually sent**
**DEBIT** company operating wallet, **CREDIT** employee wallet (a liability — "we owe this").
A _separate_ transaction later, once the Stripe payout settles, moves it out.

Separate wallets per entity (not one shared company pool) is necessary, not
optional — it's the only way to get correct per-party balances, track
liabilities distinctly from cash, isolate failures, and produce a clean
audit trail per entity.

## 5. Database Schema

![alt database scheme](./doc-img/bank-system-portfolio-database.pdf)

### Ledger Service DB (Postgres)

**`wallets`**
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | |
| account_type | ENUM(`ASSET`,`LIABILITY`,`REVENUE`,`EXPENSE`) | see §4 |
| owner_type | ENUM(`COMPANY`,`EMPLOYEE`,`VENDOR`,`DEPARTMENT`,`SYSTEM`) | |
| owner_reference | VARCHAR | external HR/vendor ID, not an FK — keeps this service bounded |
| currency | CHAR(3) | ISO 4217 |
| status | ENUM(`ACTIVE`,`FROZEN`,`CLOSED`) | |
| created_at | TIMESTAMPTZ | |

> No `balance` column — always `SUM(ledger_entries.amount)` for that wallet.

**`ledger_entries`**
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | |
| transaction_id | UUID (FK → transactions) | entries in one transaction must net to zero |
| wallet_id | UUID (FK → wallets) | |
| direction | ENUM(`DEBIT`,`CREDIT`) | |
| amount | NUMERIC(20,4) | always positive; direction gives sign |
| currency | CHAR(3) | must match wallet currency |
| created_at | TIMESTAMPTZ | immutable — no updates, no deletes |

**`transactions`**
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | |
| type | ENUM(`PAYROLL`,`VENDOR_PAYOUT`,`INTERNAL_TRANSFER`,`DEPOSIT`,`WITHDRAWAL`,`INCOME`) | |
| status | ENUM(`PENDING`,`POSTED`,`REVERSED`) | |
| description | TEXT | |
| created_at | TIMESTAMPTZ | |

**`pending_events`** _(transactional outbox pattern — see §7)_
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | |
| aggregate_id | UUID | e.g. transaction_id |
| event_type | VARCHAR | e.g. `LEDGER_ENTRY_POSTED` |
| payload | JSONB | |
| published | BOOLEAN | flipped by the outbox poller once sent to Kafka |
| created_at | TIMESTAMPTZ | |

### Payment Orchestrator DB (Postgres — separate instance/schema from Ledger)

**`payment_requests`**
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | |
| ledger_transaction_id | UUID | reference only — no FK across service DBs |
| rail | ENUM(`BANK_TRANSFER`,`CARD`,`CRYPTO`) | |
| external_account_id | UUID (FK → external_accounts) | |
| state | ENUM(`INITIATED`,`PENDING`,`SETTLED`,`FAILED`,`COMPENSATING`,`REVERSED`) | drives saga + state machine |
| idempotency_key | VARCHAR (UNIQUE) | |
| stripe_reference | VARCHAR | Stripe Treasury/Issuing object ID once created |
| failure_reason | TEXT (nullable) | |
| created_at / updated_at | TIMESTAMPTZ | |

**`external_accounts`**
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | |
| wallet_reference | UUID | which internal wallet this is linked to (reference, not FK) |
| rail | ENUM(`BANK_TRANSFER`,`CARD`,`CRYPTO`) | |
| stripe_financial_account_id | VARCHAR | Stripe Treasury Financial Account ID |
| label | VARCHAR | e.g. "Vendor X — main SEPA account" |
| created_at | TIMESTAMPTZ | |

**`idempotency_keys`**
| Column | Type | Notes |
|---|---|---|
| key | VARCHAR (PK) | client-supplied per payment intent |
| request_hash | VARCHAR | detects key reuse with a different payload |
| response_snapshot | JSONB | cached response, replayed on retry |
| created_at / expires_at | TIMESTAMPTZ | e.g. 24h TTL |

### Audit Service store (built from Kafka, not written to directly)

**`audit_log`**
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | |
| actor | VARCHAR | which service published the event |
| action | VARCHAR | e.g. `PAYMENT_STATE_CHANGED` |
| entity_type / entity_id | VARCHAR / UUID | |
| before_state / after_state | JSONB | |
| created_at | TIMESTAMPTZ | |
| trace_id | VARCHAR | ties back to distributed tracing (§9) |

## 6. Saga Pattern — Payment Flow

Since Ledger Service and Payment Orchestrator have separate databases, a
payment can't be one ACID transaction across both. Instead it's a
**choreography-based saga**: each step reacts to the previous step's event,
with a **compensating action** if a later step fails.

```
1. Payment Orchestrator: validate request, check idempotency key
2. → REST call to Ledger Service: "post this transaction"
3. Ledger Service: writes transaction + ledger_entries (own DB, real ACID
   here — this one write IS atomic), writes to outbox, returns success
4. Outbox poller publishes → Kafka `ledger.entries` (LEDGER_ENTRY_POSTED)
5. Payment Orchestrator consumes it, moves payment_request → PENDING,
   calls Connector Layer → Stripe Treasury/Issuing
6a. SUCCESS: Stripe webhook confirms → payment_request → SETTLED
    → publish `payments.lifecycle` (SETTLED) → Audit + Notification react
6b. FAILURE: Stripe rejects/errors → payment_request → COMPENSATING
    → publish a compensating event → Ledger Service posts a REVERSAL
    transaction (equal and opposite ledger_entries) → payment_request → REVERSED
```

The compensating reversal (6b) is the actual "hard part" of microservices
that a monolith would've hidden behind a plain DB rollback. Worth calling
out explicitly in a README/interview — it's the clearest signal of
understanding _why_ sagas exist.

## 7. Kafka Topics (MSK) + Reliability

| Topic                | Key                  | Payload                                            | Purpose                                                    |
| -------------------- | -------------------- | -------------------------------------------------- | ---------------------------------------------------------- |
| `ledger.entries`     | `wallet_id`          | ledger entry posted / reversed                     | source for balance recompute, audit, triggers orchestrator |
| `payments.lifecycle` | `payment_request_id` | state transition (`from`,`to`,`reason`,`trace_id`) | drives saga, feeds Audit                                   |
| `payments.dlq`       | `payment_request_id` | failed event + error context                       | manual review / replay                                     |

**Transactional outbox pattern:** a DB write and a Kafka publish are two
separate operations — if the publish fails after the DB write succeeds,
you'd silently lose an event. Fix: write the event into the same-DB
`pending_events` table inside the same transaction as the ledger write, then a
poller (or Debezium via CDC) reads `pending_events` and publishes to Kafka
reliably, marking rows `published = true`.

**Schema registry:** once Audit + Payment Orchestrator both consume
`ledger.entries`, they need a shared contract for what that event looks
like. Confluent Schema Registry + Avro (or versioned JSON Schema) prevents
one service silently breaking another by changing a field.

## 8. RabbitMQ Queues (Amazon MQ — non-critical async tasks)

| Queue                       | Purpose                                   |
| --------------------------- | ----------------------------------------- |
| `notifications.email`       | payment confirmation / failure emails     |
| `notifications.receipt-pdf` | generate + store a PDF receipt in S3      |
| `notifications.dlq`         | failed notification tasks after N retries |

Deliberately **not** Kafka — these are fire-and-forget tasks, not an
event log other services need to replay. Direct `spring-amqp` usage
(not Spring Cloud Stream) keeps the exchange/routing-key mechanics explicit.

## 9. BaaS Integration — Stripe Treasury / Issuing (sandbox)

Real fund movement and card issuing is delegated to Stripe rather than
built directly — this is the realistic pattern real fintechs use (banking
license + infra provided by a licensed partner, company builds the
orchestration layer on top).

| Your concept              | Stripe equivalent                                                  |
| ------------------------- | ------------------------------------------------------------------ |
| Wallet                    | Financial Account under a Connect connected account                |
| External payout           | Treasury outbound payment / ACH transfer                           |
| Card issuing              | Stripe Issuing — virtual/physical card tied to a Financial Account |
| Company/entity onboarding | Stripe Connect (KYC handled by Stripe)                             |

- `RailAdapter` interface stays as designed; `StripeBankAdapter implements RailAdapter`
- Official `stripe-java` SDK, called from the Connector Layer only
- Stripe **webhooks** notify async on settlement/failure → mapped to saga step 6a/6b
- Permanently sandbox-mode for this project — going live requires Stripe's
  business-use-case approval, stated explicitly in the README

## 10. API Sketch (via API Gateway)

**Payment Orchestrator**

- `POST /payments` — `Idempotency-Key` header required
- `GET /payments/{id}` — current state + saga history
- `GET /payments?wallet_id=&status=`

**Ledger Service**

- `POST /wallets`
- `GET /wallets/{id}/balance`
- `GET /wallets/{id}/ledger?from=&to=`
- `POST /internal/transactions` — called only by Payment Orchestrator, not public

**Audit Service**

- `GET /audit?entity_id=&entity_type=&from=&to=`
- _(phase 2 stretch: GraphQL here specifically — flexible nested queries
  for an ops dashboard; REST stays for all writes/payments)_

## 11. Microservice Toolkit (cross-cutting)

| Concern                       | Tool                                                                                                   | Notes                                                                                                                                                     |
| ----------------------------- | ------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Edge routing/auth             | Spring Cloud Gateway                                                                                   | single entry point                                                                                                                                        |
| Inter-service call resilience | Resilience4j                                                                                           | circuit breaker + retry on e.g. Orchestrator → Ledger REST calls                                                                                          |
| Distributed tracing           | Micrometer Tracing + Zipkin (local) / X-Ray (AWS)                                                      | trace_id threaded through every hop and Kafka event                                                                                                       |
| Event schema contracts        | Confluent Schema Registry (Avro) or versioned JSON Schema                                              |                                                                                                                                                           |
| Service discovery             | Consul (or Eureka, for learning the classic pattern)                                                   | AWS Cloud Map is the native alternative once deployed to ECS                                                                                              |
| **Explicitly skipped**        | Spring Cloud Stream, Spring Cloud Config Server, Spring Cloud Contract, saga frameworks (Axon/Camunda) | direct spring-kafka/spring-amqp is more explicit; env vars/Secrets Manager cover config; hand-rolled choreography saga teaches more than a framework here |

## 12. AWS Mapping

| Concern                      | Service                                          |
| ---------------------------- | ------------------------------------------------ |
| Per-service DB               | RDS (Postgres) — one instance/schema per service |
| Event log                    | MSK (Kafka)                                      |
| Task queue                   | Amazon MQ (RabbitMQ)                             |
| Secrets (Stripe keys)        | Secrets Manager                                  |
| Field-level encryption       | KMS                                              |
| Compute                      | ECS/Fargate — one task definition per service    |
| Service discovery (native)   | AWS Cloud Map                                    |
| Tracing                      | X-Ray                                            |
| Logs/metrics                 | CloudWatch                                       |
| Receipt PDFs                 | S3                                               |
| Transaction search (stretch) | OpenSearch                                       |

## 13. Repository / File Structure

Monorepo — one Git repo, multiple independently-buildable services inside.
(Separate repos per service is a team-ownership pattern you don't need solo.)

```
banking-platform/
├── ledger-service/
│   ├── src/main/java/com/yourname/ledger/
│   │   ├── domain/          (Wallet, LedgerEntry, Transaction)
│   │   ├── repository/
│   │   ├── service/         (balance calc, entry creation)
│   │   ├── controller/
│   │   ├── pendingEvents/          (outbox writer + poller)
│   │   └── kafka/           (producer)
│   ├── Dockerfile
│   └── pom.xml
│
├── payment-orchestrator/
│   ├── src/main/java/com/yourname/payments/
│   │   ├── domain/          (PaymentRequest, state machine)
│   │   ├── saga/             (saga steps + compensating actions)
│   │   ├── client/           (REST client → Ledger Service, w/ Resilience4j)
│   │   ├── kafka/            (producer + consumer)
│   │   └── controller/
│   ├── Dockerfile
│   └── pom.xml
│
├── connector-service/
│   ├── src/main/java/com/yourname/connectors/
│   │   ├── RailAdapter.java
│   │   └── StripeBankAdapter.java
│   └── pom.xml
│
├── audit-service/
│   ├── src/main/java/com/yourname/audit/
│   │   ├── kafka/            (consumers, both topics)
│   │   ├── domain/           (denormalized audit record)
│   │   └── controller/       (read-only)
│   └── pom.xml
│
├── notification-service/
│   ├── src/main/java/com/yourname/notifications/
│   │   ├── rabbitmq/
│   │   └── pdf/
│   └── pom.xml
│
├── api-gateway/
│   └── pom.xml               (Spring Cloud Gateway config/routes)
│
├── common/                    (shared library — NOT a service)
│   └── src/main/java/com/yourname/common/
│       ├── events/            (Kafka event DTOs/schemas)
│       └── exceptions/
│
├── infra/                     (Terraform/CDK)
│   ├── rds.tf
│   ├── msk.tf
│   ├── ecs.tf
│   └── ...
│
├── docker-compose.yml          (local: N Postgres, Kafka, RabbitMQ, Consul, Zipkin, all services)
├── README.md
└── STRUCTURE.md
```

**Boundary rule:** the only code allowed in `common/` is event schemas and
generic exceptions. If you want to share a domain class (e.g. `Wallet.java`)
between services, that's a signal the boundary is wrong — each service
owns its own domain model, even where models look similar.

## 14. Build Order

1. **Ledger Service alone** — domain, balance-from-entries logic, REST API,
   own Postgres, unit tests proving entries always net to zero
2. **Payment Orchestrator**, calling Ledger Service over plain synchronous
   REST (no Kafka, no saga yet — intentionally "wrong" for true
   microservices, but gets two real services talking first)
3. Introduce **Kafka** + outbox pattern on Ledger Service; rework the flow
   into the **choreography saga** (§6), including the compensating reversal
4. **Stripe Treasury/Issuing sandbox** integration in the Connector Layer,
   replacing a `NoopAdapter`; wire Stripe webhooks into the saga
5. **RabbitMQ** notification path
6. **Audit Service** consuming Kafka
7. Cross-cutting: **Resilience4j** on the Orchestrator→Ledger call, then
   **tracing** (Zipkin locally), then **API Gateway**, then **service
   discovery** (Consul/Eureka)
8. **AWS deployment** (RDS ×N, MSK, ECS, Cloud Map) + IaC
9. Stretch goals: GraphQL on Audit Service, Spring Batch for payroll runs
   - reconciliation, OpenSearch transaction search

## Roadmap

- [ ] Ledger Service — domain model, balance-from-entries logic, REST API
- [ ] Payment Orchestrator — synchronous REST call to Ledger (no Kafka yet)
- [ ] Kafka + `pending_events` outbox pattern; rework into the saga
- [ ] Stripe Treasury/Issuing sandbox integration + webhooks
- [ ] RabbitMQ notification path
- [ ] Audit Service (Kafka consumer, read-only API)
- [ ] Resilience4j, distributed tracing, API Gateway, service discovery
- [ ] AWS deployment (RDS, MSK, ECS, Cloud Map) + Terraform/CDK
- [ ] Stretch: GraphQL on Audit Service, Spring Batch for payroll runs
      and reconciliation, OpenSearch transaction search
