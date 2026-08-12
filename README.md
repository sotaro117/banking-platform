# Private Payment & Wallet Platform

A company-internal payment orchestration system: manage internal wallets
(employees, vendors, departments), distribute payments, and settle out to
external rails (bank transfer, card, crypto) — built as a set of real
microservices to learn distributed-systems patterns used in production
fintech/banking backends.

> Full architecture, database schema, and service breakdown live in
> [`STRUCTURE.md`](./STRUCTURE.md). This README covers the "why" and how to
> run it; `STRUCTURE.md` covers the "how it's built."

## Scope & Disclaimer

This is a **portfolio/learning project simulating a payment platform** —
it is not a licensed money transmitter and does not move real customer
funds. All external fund movement is delegated to **Stripe Treasury /
Issuing in sandbox (test) mode**, the way a real company would delegate
banking infrastructure to a licensed Banking-as-a-Service partner rather
than build it themselves. Going live would require Stripe's business-use
approval and real regulatory licensing — neither is in scope here.

## Why This Project Exists

Built specifically to learn **microservice architecture** — not just to
ship a working app. That's why the design deliberately takes on problems
a simpler monolith would sidestep: database-per-service, distributed
transactions via the saga pattern, event-driven coordination, and
inter-service resilience. Domain-wise, it targets banking-sector job
applications, so the ledger/accounting logic follows real double-entry
principles rather than a simplified "balance column" model.

## Architecture at a Glance

```
Client → API Gateway → Ledger Service (own DB)
                     → Payment Orchestrator (own DB) → Connector Layer → Stripe (sandbox)
                     → Audit Service (Kafka-derived store, read-only)
                     → Notification Service (no DB)

Kafka (MSK)     — event log: ledger.entries, payments.lifecycle, payments.dlq
RabbitMQ (MQ)   — task queue: email + receipt-PDF notifications
```

Each service that needs persistence owns its own database — no service
reaches into another's tables. Full diagram and per-service schema: see
`STRUCTURE.md` §2–§5.

## Tech Stack

| Layer                       | Choice                                                     |
| --------------------------- | ---------------------------------------------------------- |
| Services                    | Spring Boot (Java)                                         |
| Edge routing                | Spring Cloud Gateway                                       |
| Event log                   | Kafka (AWS MSK)                                            |
| Task queue                  | RabbitMQ (Amazon MQ)                                       |
| Databases                   | PostgreSQL (RDS), one instance/schema per service          |
| Cache / idempotency lookups | Redis                                                      |
| Inter-service resilience    | Resilience4j (circuit breaker, retry)                      |
| Tracing                     | Micrometer Tracing + Zipkin (local) / AWS X-Ray (deployed) |
| Service discovery           | Consul (AWS Cloud Map once deployed)                       |
| BaaS / external rails       | Stripe Treasury & Issuing (sandbox)                        |
| Secrets & encryption        | AWS Secrets Manager, KMS (envelope encryption via AES)     |
| Infra as code               | Terraform / CDK                                            |

## Key Design Decisions

A few choices worth calling out explicitly — these are the parts meant to
demonstrate real understanding, not just tech-stack breadth:

- **No `balance` column.** Every wallet's balance is derived by summing
  its `ledger_entries`. Balances are a query, not stored state.
- **Double-entry accounting**, with a real chart of accounts
  (`ASSET`/`LIABILITY`/`REVENUE`/`EXPENSE`) — not just "money in, money
  out." See `STRUCTURE.md` §4.
- **Database-per-service**, which means no cross-service ACID
  transactions — handled instead via a **choreography-based saga**
  (§6): each step reacts to the previous step's Kafka event, with an
  explicit **compensating reversal** if a later step (e.g. the Stripe
  payout) fails after the ledger already recorded it.
- **Event-staging table (`pending_events`)** written in the _same_
  local database transaction as the business write, so a Kafka publish
  failure can never silently lose an event (the transactional outbox
  pattern). See §7.
- **Idempotency keys** on every payment-creating request, checked before
  any write — the standard, non-negotiable pattern for any system that
  moves money.
- **Kafka and RabbitMQ used for different reasons, not interchangeably**
  — Kafka as a replayable event log (ledger/payment history, multiple
  independent consumers), RabbitMQ as a plain task queue (notifications,
  fire-and-forget). Deliberately not using Spring Cloud Stream, so the
  broker-specific mechanics (consumer groups/partitions vs.
  exchanges/routing keys) stay explicit in the code.
- **Two-layer encryption** — RDS encryption at rest as a baseline, plus
  explicit KMS/AES envelope encryption on the one field that's actually
  sensitive (`external_accounts.account_ref_encrypted`), rather than
  encrypting everything uniformly. See §10.
- **BaaS delegation, not custom banking infra** — real fund movement and
  card issuing go through Stripe's sandbox, matching how real fintechs
  operate (banking license + rails from a partner, orchestration logic
  built in-house).

## Project Structure

Monorepo, one service per folder, each independently buildable/deployable.
Full layout: `STRUCTURE.md` §14.

```
banking-platform/
├── ledger-service/
├── payment-orchestrator/
├── connector-service/
├── audit-service/
├── notification-service/
├── api-gateway/
├── common/            (shared event schemas + exceptions only)
├── infra/              (Terraform/CDK)
└── docker-compose.yml  (local: Postgres, Kafka, RabbitMQ, Consul, Zipkin)
```

## Getting Started (local dev)

```bash
git clone <repo-url>
cd banking-platform
docker-compose up -d      # Postgres instances, Kafka, RabbitMQ, Consul, Zipkin
```

Each service builds independently, e.g.:

```bash
cd ledger-service
./mvnw spring-boot:run
```

Environment variables per service (DB connection, Kafka brokers, Stripe
sandbox keys) are documented in each service's own `application.yml`.
