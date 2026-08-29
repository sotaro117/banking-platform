# Private Payment & Wallet Platform — Structure

> **Scope note:** this is a simulation of a company-internal payment orchestration
> system built to learn microservice architecture, not a production
> money-transmission service. Real fund movement/card issuing is delegated to a
> Banking-as-a-Service sandbox (Stripe Treasury/Issuing test mode) rather than
> implemented directly — this is called out explicitly to avoid implying real
> regulatory/licensing compliance.

## 1. High-Level Concept

A single company (the "tenant") uses this platform to manage internal wallets
(employees, vendors, the company's own operating wallet) and
distribute payments out to external rails via a **Banking-as-a-Service**
provider, all coordinated through a central, auditable, event-driven ledger.

Built as **true microservices** (database-per-service), not a monolith —
this is a deliberate learning choice, so the design leans into problems
(distributed transactions, sagas, service discovery) a monolith would avoid.

_saga_: a design method used to manage data consistency and a sequence of independent steps across distributed microservices during multi-step financial transactions

Assets and Expenses:

- Debit: Increases the balance.
- Credit: Decreases the balance.

Liabilities, Equity, and Revenue:

- Debit: Decreases the balance.
- Credit: Increases the balance.

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
│              │     │                  │    │                │    │                │
│              │     │ includes, as a   │    │                │    │                │
│              │     │ package (no      │    │                │    │                │
│              │     │ network hop):    │    │                │    │                │
│              │     │  connectors/     │    │                │    │                │
│              │     │  RailAdapter     │    │                │    │                │
│              │     │  → SwanAdapter    │──── calls ──► Swan API (GraphQL,/
│              │     │                  │               Issuing (sandbox)
└─────┬──────┘     └───────┬────────┘    └───────▲───────┘    └───────▲───────┘
      │                    │                     │                    │
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

| Service                  | Owns                      | Responsibility                                                                                                                        | Talks to                                       |
| ------------------------ | ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------- |
| **API Gateway**          | nothing (stateless)       | Routing, auth, rate limiting at the edge                                                                                              | All services                                   |
| **Ledger Service**       | own Postgres DB           | Source of truth for wallets + double-entry ledger. Never trusts a caller's stated balance — always derives it from ledger entries.    | Its DB, Kafka (publishes `ledger.entries`)     |
| **Payment Orchestrator** | own Postgres DB           | Owns the payment lifecycle state machine + saga coordination. Enforces idempotency. Includes rail integration internally (see below). | Its DB, Ledger Service (REST), Swan API, Kafka |
| **Audit Service**        | own store (Kafka-derived) | Read-only. Rebuilds queryable history by consuming Kafka. Never writes back to other services.                                        | Kafka (consumes), optional OpenSearch          |
| **Notification Service** | no DB                     | Sends emails, generates receipt PDFs. Decoupled via RabbitMQ so a slow provider never blocks a payment.                               | RabbitMQ, S3                                   |

**Database-per-service is intentional.** No service reaches into another's tables.
This is what makes it a real microservice split instead of a distributed
monolith — and it's why the saga pattern below exists (you lose cross-service
ACID transactions once DBs are separate).

## 4. Chart of Accounts

Standard double-entry accounting has five account categories. All of them
are structurally the same kind of row — an account that `ledger_entries`
post debits/credits against — which is why they live in **one table**
(`ledger_accounts`, §5), not split across separate tables. Splitting them
would break the "a transaction's entries must net to zero" invariant,
since a single transaction often needs to post to more than one category
at once (see the income example below).

| account_type | Represents                                                | party_id                                                                 | Example                                          |
| ------------ | --------------------------------------------------------- | ------------------------------------------------------------------------ | ------------------------------------------------ |
| `ASSET`      | Money the company actually holds                          | `COMPANY` party (or an employee/vendor if they hold a spendable balance) | Company operating account                        |
| `LIABILITY`  | Money owed but not yet paid out                           | The employee/vendor party it's owed to                                   | Employee account before payout (accrued, unpaid) |
| `REVENUE`    | Income recognized — a running tally, not spendable        | `COMPANY` party                                                          | "Service Revenue"                                |
| `EXPENSE`    | Money spent, categorized — a running tally, not spendable | `COMPANY` party                                                          | "Payroll Expense", "Vendor Payments"             |

**"Wallet" is a product-facing term, not a fourth account type.** It refers
to `ASSET`/`LIABILITY` rows that represent an actual, meaningful balance
someone could receive or owe — as opposed to `REVENUE`/`EXPENSE` rows,
which are **ledger accounts** in the pure bookkeeping sense: categories
explaining _why_ an asset/liability changed, not something with a
spendable balance of its own.

**Every account, including `REVENUE`/`EXPENSE`, has a `party_id` — they
belong to the `COMPANY` party, same as any other company-owned account.**
`party_id` is never null in practice; it's kept nullable in the schema
only as headroom for a genuine edge case, not as the mechanism that
distinguishes "wallet" from "category." That distinction is enforced
by `account_type` instead: **only `ASSET`/`LIABILITY` accounts can be
referenced by a `payment_request`** (§5, §11) — money can never be
routed to or from a `REVENUE`/`EXPENSE` account, because they aren't
places money lives, they're labels for why a real account's balance moved.
This is validated in the Payment Orchestrator, not implied by nullability.

**Example: company receives customer income**
`transaction` with two `ledger_entries`: **DEBIT** company operating account
(asset ↑, a real wallet), **CREDIT** `REVENUE` account — both belong to the
`COMPANY` party, but only one of them is a "wallet" in the product sense.
The two net to zero.

**Example: payroll/vendor obligation, before it's actually sent**
**DEBIT** company operating account, **CREDIT** employee or vendor account
(a liability — "we owe this," still a wallet since it's party-linked). A
_separate_ transaction later, once the Stripe payout settles, moves it out.
Employee and vendor accounts are both simply `LIABILITY` — the same
mechanics apply to either, `party.type` is only used to distinguish them
for identification/reporting, not for how the ledger treats them.

### Transaction Types → Account Pairing

Every `transactions.type` has an unambiguous DEBIT/CREDIT pairing —
this table is the reference for what a given type is allowed to touch.

| Type                         | Triggered by                         | DEBIT                               | CREDIT                              |
| ---------------------------- | ------------------------------------ | ----------------------------------- | ----------------------------------- |
| `PAYROLL` (accrual)          | Company payroll run                  | `EXPENSE` — Payroll Expense         | `LIABILITY` — employee's account    |
| `PAYROLL` (settlement)       | Stripe confirms payout               | `LIABILITY` — employee's account    | `ASSET` — company operating account |
| `VENDOR_PAYOUT` (accrual)    | Invoice approved                     | `EXPENSE` — Vendor Payments         | `LIABILITY` — vendor's account      |
| `VENDOR_PAYOUT` (settlement) | Stripe confirms payout               | `LIABILITY` — vendor's account      | `ASSET` — company operating account |
| `INTERNAL_TRANSFER`          | Company, between its own accounts    | `ASSET` — destination account       | `ASSET` — source account            |
| `INCOME`                     | Stripe `ReceivedCredit` webhook (§9) | `ASSET` — company operating account | `REVENUE` — Service Revenue         |
| `REVERSAL`                   | Saga compensating action (§6)        | mirrors whatever it's undoing       | mirrors whatever it's undoing       |

**`INTERNAL_TRANSFER`** — the one type where both sides are `ASSET`, no
`LIABILITY`/`REVENUE` involved: moving the company's own money between
its own accounts (e.g. between a EUR and a USD operating account, or into
an earmarked "payroll reserve" sub-account before a run). Still goes
through a real transaction rather than an implicit balance edit, because
every balance change — even ones with no external counterparty — must
stay derivable from `ledger_entries`, with no exceptions.

**`REVERSAL`** — undoes a transaction that already posted, used when a
settlement fails after the ledger already recorded the accrual (§6 step
6b). Ledger entries are immutable (no updates, no deletes), so a failed
payout is never deleted — a new transaction posts with the DEBIT/CREDIT
sides flipped relative to the original, bringing the balance back to
where it was while preserving a full, honest record of the attempt and
its correction. This is what keeps the audit trail truthful: "it went to
€0" is a materially weaker statement than "it went up, then was
correctly reversed, and here's exactly when."

**Deferred, not built — `DEPOSIT`/`WITHDRAWAL`:** these would represent
a party holding and moving their _own_ balance directly (deposit: their
external funds arriving into custody, credit to their `LIABILITY`;
withdrawal: the reverse) — the mechanism the "employee spends from their
internal account like a personal bank account" idea (§1) would need.
Out of scope for v1, which only ever pushes money _out_ to an employee's
external account via `PAYROLL` settlement — nothing sits in internal
custody waiting to be withdrawn. Noted here so the absence is a deliberate
scope decision, not an oversight, if that feature ever gets revisited.

Separate accounts per party (not one shared company pool) is necessary,
not optional — it's the only way to get correct per-party balances, track
liabilities distinctly from cash, isolate failures, and produce a clean
audit trail per entity.

## 5. Database Schema

![database scheme](./doc-img/bank-system-portfolio-database.pdf)

### Ledger Service DB (Postgres)

**`party`**

A thin identity layer — _not_ a system of record for HR/vendor master data.
Real employee/vendor details (salary, tax ID, address, employment status
changes) stay owned by an actual HR/vendor system; this table holds just
enough to route payments and label them meaningfully.

| Column             | Type                                | Notes                                                                |
| ------------------ | ----------------------------------- | -------------------------------------------------------------------- |
| id                 | UUID (PK)                           | what `wallet.party_id` references                                    |
| external_reference | VARCHAR                             | ID in the real HR/vendor system — the actual link out                |
| type               | ENUM(`EMPLOYEE`,`VENDOR`,`COMPANY`) |                                                                      |
| display_name       | VARCHAR                             | e.g. "Jane Doe" — for payment records/audit logs, not a full profile |
| status             | ENUM(`ACTIVE`,`INACTIVE`)           | lets a payroll batch job query "all active employees"                |
| created_at         | TIMESTAMPTZ                         |                                                                      |

> In production this would be kept in sync via events/a nightly job from
> the real HR system (new hire → new party + wallet, termination → status
> flips to `INACTIVE`) — for this project it can be manually seeded or
> managed via a simple admin endpoint.

**`wallet`**
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | |
| account_type | ENUM(`ASSET`,`LIABILITY`,`REVENUE`,`EXPENSE`) | see §4 |
| party_id | UUID (FK → parties.id, nullable) | who this wallet belongs to; **null** for system-level category accounts (e.g. a pure `REVENUE`/`EXPENSE` account not tied to one party) |
| currency | CHAR(3) | ISO 4217 |
| status | ENUM(`ACTIVE`,`FROZEN`,`CLOSED`) | |
| created_at | TIMESTAMPTZ | |

> No `balance` column — always `SUM(ledger_entries.amount)` for that wallet.
> "Pay a specific employee": query `parties WHERE type='EMPLOYEE' AND
status='ACTIVE'`, follow to their `wallet_id`, then to that wallet's
> linked `external_accounts` row for the actual payout destination.

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
| type | ENUM(`PAYROLL`,`VENDOR_PAYOUT`,`INTERNAL_TRANSFER`,`INCOME`,`REVERSAL`) | see §4 for account-type pairing per type |
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
| ledger_transaction_id | UUID | reference only — no FK across service DBs; set once, right after the Ledger Service accrual call (§6 step 2-3), never changed afterward |
| amount | NUMERIC(20,4) | duplicated from the ledger transaction — needed here because Orchestrator can't reach into Ledger Service's DB to look it up (database-per-service); this is what's actually sent to Swan |
| currency | CHAR(3) | |
| rail | ENUM(`BANK_TRANSFER`,`CARD`,`CRYPTO`) | derived from `external_account_id`'s own `rail` at request time — never supplied directly by the caller (see §12) |
| external_account_id | UUID (FK → external_accounts) | |
| state | ENUM(`INITIATED`,`PENDING`,`SETTLED`,`FAILED`,`COMPENSATING`,`REVERSED`) | drives saga + state machine |
| idempotency_key | VARCHAR (UNIQUE) | |
| swan_reference | VARCHAR | Swan Payment/Transaction ID — NULL until the rail adapter call (§6 step 5) returns; used to match later webhook confirmations back to this row |
| failure_reason | TEXT (nullable) | |
| created_at / updated_at | TIMESTAMPTZ | |

**`external_accounts`**

Two genuinely different row shapes share this table — the company's own
BaaS-issued account, and an employee/vendor's pre-existing external bank
account. Only one of `swan_account_id`/`issued_iban` vs.
`bank_details_encrypted` is populated per row, depending on which:

| Column                 | Type                                  | Notes                                                                                                                                                                                                                    |
| ---------------------- | ------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| id                     | UUID (PK)                             |                                                                                                                                                                                                                          |
| account_reference      | UUID                                  | which internal ledger account this is linked to (reference, not FK)                                                                                                                                                      |
| rail                   | ENUM(`BANK_TRANSFER`,`CARD`,`CRYPTO`) |                                                                                                                                                                                                                          |
| swan_account_id        | VARCHAR (nullable)                    | **company row only** — the operational ID returned when the Swan Account was opened; used for API calls and webhook matching (§9)                                                                                        |
| issued_iban            | VARCHAR (nullable)                    | **company row only** — the real IBAN Swan issues along with the Account; informational (e.g. what a customer wires `INCOME` to), not used operationally the way `swan_account_id` is                                     |
| bank_details_encrypted | BYTEA (nullable)                      | **employee/vendor rows only** — their own pre-existing external IBAN, provided by an admin at setup time (§11), KMS/AES envelope encrypted (§10); the BaaS provider never issues this, it's an admin-entered destination |
| label                  | VARCHAR                               | e.g. "Vendor X — main SEPA account"                                                                                                                                                                                      |
| created_at             | TIMESTAMPTZ                           |                                                                                                                                                                                                                          |

> **Why the direction differs between the two:** the company's Financial
> Account and its IBAN are _issued to_ the company by the BaaS provider
> when the account is opened — the company doesn't already have one to
> supply. An employee/vendor's IBAN is the opposite: their own,
> pre-existing external bank account, entered once by an admin so
> Payment Orchestrator knows where to send their payout — the BaaS
> provider never issues or knows about it until a payment references it.

**`idempotency_keys`**
| Column | Type | Notes |
|---|---|---|
| key | VARCHAR (PK) | client-supplied per payment intent |
| request_hash | VARCHAR | detects key reuse with a different payload |
| response_snapshot | JSONB | cached response, replayed on retry |
| created_at / expires_at | TIMESTAMPTZ | e.g. 24h TTL |

**`processed_swan_events`**

Separate from `idempotency_keys` above — that table protects against _your
own client_ retrying a request; this one protects against _Swan_
redelivering the same webhook (Swan's delivery guarantee is
at-least-once, so duplicates are expected, not exceptional).

| Column        | Type         | Notes                                                                       |
| ------------- | ------------ | --------------------------------------------------------------------------- |
| swan_event_id | VARCHAR (PK) | Swan's own webhook event ID, unique per delivery attempt of the same event  |
| event_type    | VARCHAR      | e.g. `treasury.received_credit.created`, `treasury.outbound_payment.posted` |
| processed_at  | TIMESTAMPTZ  |                                                                             |

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
payment can't be one ACID transaction across both. Instead it's an
**orchestration-based saga**: Payment Orchestrator explicitly drives every
step — calling out, waiting on the result, and deciding what happens next
— rather than services reacting independently to each other's events.
Kafka still gets used (§7), but only to _announce_ what already happened
to downstream observers (Audit, Notification) — it never drives what the
saga does next; that's entirely Orchestrator's own, explicit logic.

```
1. Payment Orchestrator: validate request, check idempotency key
2. Orchestrator calls Ledger Service directly (REST): "post this
   transaction" — and waits for the response
3. Ledger Service: writes transaction + ledger_entries (own DB, real ACID
   here — this one write IS atomic), writes to `pending_events`, returns
   success synchronously back to Orchestrator
4. (side effect only, not part of the driving sequence) a poller reads
   `pending_events` and publishes → Kafka `ledger.entries`
   (LEDGER_ENTRY_POSTED) → Audit Service consumes it for its own record.
   Orchestrator does not wait for or react to this — it already has its
   answer directly from step 2-3
5. Orchestrator, having gotten success back from Ledger Service, itself
   calls the rail adapter (connectors/ package, in-process) → Swan API
6a. SUCCESS: Swan webhook confirms → Orchestrator moves payment_request
    → SETTLED, then publishes `payments.lifecycle` (SETTLED) purely to
    notify Audit + Notification — they don't drive anything back
6b. FAILURE: Swan rejects/errors → Orchestrator moves payment_request
    → COMPENSATING, and itself calls Ledger Service again to post a
    REVERSAL transaction (equal and opposite ledger_entries) →
    payment_request → REVERSED
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

## 9. BaaS Integration — Swan (sandbox)

**Switched from Stripe Treasury/Issuing to Swan** — Stripe Treasury is
US-only; Swan is an EU-native BaaS provider (French e-money license,
covers Spain/EU), a better fit given where this project is actually
being built. Real fund movement and card issuing is still delegated
rather than built directly — same realistic pattern (banking license +
infra provided by a licensed partner, company builds the orchestration
layer on top) — just a different provider.

| App concept               | Swan equivalent                                                                                  |
| ------------------------- | ------------------------------------------------------------------------------------------------ |
| Ledger account (wallet)   | A Swan `Account` (has its own IBAN + BIC) under your Swan project                                |
| External payout           | SEPA Credit Transfer / SEPA Instant initiation                                                   |
| Card issuing              | Swan card issuing — virtual/physical cards, configurable spend controls                          |
| Company/entity onboarding | Swan's own KYB flow (Swan holds the e-money license, so no separate license needed on your side) |

- `RailAdapter` interface stays as designed; `SwanAdapter implements RailAdapter`
- **Swan's API is GraphQL, not REST** — no official Java SDK exists, so
  the `connectors/` package calls it via a plain HTTP client posting
  GraphQL queries/mutations (or a generic Java GraphQL client library),
  rather than a dedicated provider SDK the way a Stripe integration would've used
- Swan **webhooks** notify async on settlement/failure → mapped to saga
  step 6a/6b, same as before — Swan's webhook delivery is also
  **at-least-once**, so the same redelivery-dedup handling applies
- **Consent/SCA step, worth flagging as a real design difference from
  Stripe's simpler flow:** Swan's sensitive mutations (initiating a payment) can return
  a `Consent` requiring approval via a Swan-hosted URL (Strong Customer
  Authentication) before the operation actually executes — this doesn't
  apply the same way to pure server-to-server/project-level API access,
  but it's worth confirming against Swan's docs once you're actually
  integrating, since it could add a genuinely async approval step to
  what §6 currently models as one direct call-and-reference-back
- Sandbox and Live are fully isolated environments in Swan (separate
  credentials, no crossover) — permanently sandbox-mode for this
  project; going live requires Swan's own review, stated explicitly in
  the README, same as a Stripe going-live gate would've been

### Inbound funds (company income)

Mirrors the outbound saga, but triggered by Swan rather than initiated
by Payment Orchestrator — money landing on the company's Swan `Account`
(e.g. a customer paying an invoice via SEPA Credit Transfer) is what
turns into `REVENUE`.

```
1. Swan sandbox: an incoming SEPA Credit Transfer lands on the company's
   Swan Account (simulated via Swan's sandbox event simulator)
2. Swan sends webhook: Transaction.Booked
   → POST /webhooks/swan on Payment Orchestrator
3. Verify the webhook using the secret configured on the
   WebhookSubscription (HMAC-style signature check) before trusting the
   payload — unverified webhooks would let anyone forge "income" into
   the ledger
4. Check processed_swan_events for the event's ID — if already present,
   ack and return (Swan redelivers the same event at-least-once, and
   does not guarantee delivery order)
5. Look up the internal account: the webhook payload's accountId
   → external_accounts.swan_account_id
   → external_accounts.account_reference (the company's ASSET account)
6. Call Ledger Service's POST /internal/transactions:
     DEBIT  Company operating account (ASSET)   ↑ increases
     CREDIT Service Revenue (REVENUE)            income recognized
7. Record the event in processed_swan_events, return 200 to Swan
```

No saga/compensating-action logic needed here (unlike outbound payments)
— it's a single atomic booking triggered by a confirmed, already-settled
external event, not a multi-step attempt that can fail partway through.

## 10. Encryption

Two separate layers, solving different problems — you want both, not one instead of the other.

**Layer 1: Encryption at rest (baseline, whole-database)**
RDS encrypts the entire storage volume automatically (AES-256 under the hood) —
essentially a checkbox at provisioning time. Protects against someone
stealing the physical disk/snapshot. Does **not** protect against a SQL
injection or a compromised app-layer credential reading the DB normally —
once the app reads a row, it's already decrypted.

**Layer 2: Field-level encryption (explicit, for genuinely sensitive fields)**
Applies specifically to `external_accounts.account_ref_encrypted` — the
actual IBAN/bank account reference or crypto wallet address. Uses
**envelope encryption**, the standard AWS pattern:

1. Application asks **KMS** for a data key
2. Application encrypts the field locally using **AES** (the algorithm) with that data key
3. Both the AES-encrypted value and the KMS-encrypted data key are stored together in Postgres
4. KMS itself never sees the raw field value — it only ever manages the key

In practice this is implemented via the `aws-encryption-sdk-java` library rather
than hand-rolling AES calls — it handles key caching, rotation, and the
encrypt/decrypt envelope format correctly.

**Rule of thumb for what needs Layer 2 vs. just Layer 1:** anything that
identifies a real external account or could enable fraud if leaked
(`account_ref_encrypted`, any future card PAN/CVV if you go deeper into
Issuing) gets explicit field-level encryption. Ledger amounts, wallet IDs,
and transaction metadata are adequately covered by RDS encryption at rest
alone — encrypting every column would just add overhead with no real
security gain, since none of it is individually sensitive outside its
row context.

## 11. Authentication & Authorization

**Single role: `ADMIN`.** No tiering (no separate "ops" vs. some other
audience) — everyone who touches this system is company staff acting
with full authority over it, so one role covers every protected endpoint.
The value of having a role at all, even just one, is demonstrating the
system has _some_ real authentication boundary — a payments platform
being wide open would be a real gap, not just an incomplete feature.

**No dedicated `users`/credentials table — delegated, same pattern as
Stripe for banking rails.** Rolling your own credential storage (password
hashing, reset flows, breach liability) is a security-sensitive domain
that doesn't demonstrate anything about ledgers or sagas — it's exactly
the kind of concern worth delegating rather than building.

| Environment                   | Identity provider     | How it works                                                                                                                  |
| ----------------------------- | --------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| Local / demo                  | Spring Security + JWT | Single hardcoded/properties-based admin credential; login issues a JWT, API Gateway verifies it on every request              |
| Production (noted, not built) | AWS Cognito           | Cognito is the system of record for identity; Gateway only verifies the JWT signature + role claim, no local user data at all |

**Protected endpoints — all `hasRole('ADMIN')`, enforced via
`@PreAuthorize`, checked once at the API Gateway:**

| Action                                                   | Endpoint                                                  | Service              |
| -------------------------------------------------------- | --------------------------------------------------------- | -------------------- |
| Create a party (auto-creates linked `LIABILITY` account) | `POST /parties`                                           | Ledger Service       |
| View a party                                             | `GET /parties/{id}`                                       | Ledger Service       |
| Freeze or close an account                               | `PATCH /accounts/{id}/status`                             | Ledger Service       |
| View account balance / ledger history                    | `GET /accounts/{id}/balance`, `GET /accounts/{id}/ledger` | Ledger Service       |
| Move money between the company's own accounts            | `POST /accounts/internal-transfer`                        | Ledger Service       |
| Trigger a payroll or vendor payout (full saga)           | `POST /payments`                                          | Payment Orchestrator |
| Check a payment's status                                 | `GET /payments`, `GET /payments/{id}`                     | Payment Orchestrator |
| View the audit trail                                     | `GET /audit`                                              | Audit Service        |

`POST /internal/transactions` (§5, §6) stays internal-only regardless —
called by Payment Orchestrator during the saga, never directly by an
admin.

**Party/account creation is manually triggered, not synced from an HR
system.** An admin calls `POST /parties`, and the service layer creates
the `Party` and its linked `LIABILITY` account together in one call. In
production this would instead sync automatically from a real HR/vendor
system (new hire → webhook → same creation logic) — `parties.external_reference`
already exists specifically so that swap wouldn't require a schema
change, just a different trigger.

## 12. API Sketch (via API Gateway)

**Payment Orchestrator**

- `POST /payments` — `Idempotency-Key` header required; body:
  `{ accountId, externalAccountId, amount, currency }`. Rejects the
  request if the referenced account's `account_type` is
  `REVENUE`/`EXPENSE` (only `ASSET`/`LIABILITY` accounts can send/receive
  real payments — see §4). **`rail` is not a request field** — it's
  derived server-side from `externalAccountId`'s own `rail` (§5), since
  one external account is always tied to exactly one rail; asking the
  client to also state it would just be redundant data that could
  contradict what's already on the account
- `GET /payments/{id}` — current state + saga history
- `GET /payments?account_id=&status=`

**Ledger Service**

- `POST /wallet`
- `GET /wallet/{id}/balance`
- `GET /wallet/{id}/entry?from=&to=`
- `POST /internal/transactions` — called only by Payment Orchestrator, not public

**Audit Service**

- `GET /audit?entity_id=&entity_type=&from=&to=`
- _(phase 2 stretch: GraphQL here specifically — flexible nested queries
  for an ops dashboard; REST stays for all writes/payments)_

## 13. Microservice Toolkit (cross-cutting)

| Concern                       | Tool                                                                                                   | Notes                                                                                                                                                      |
| ----------------------------- | ------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Edge routing/auth             | Spring Cloud Gateway                                                                                   | single entry point                                                                                                                                         |
| Inter-service call resilience | Resilience4j                                                                                           | circuit breaker + retry on e.g. Orchestrator → Ledger REST calls                                                                                           |
| Distributed tracing           | Micrometer Tracing + Zipkin (local) / X-Ray (AWS)                                                      | trace_id threaded through every hop and Kafka event                                                                                                        |
| Event schema contracts        | Confluent Schema Registry (Avro) or versioned JSON Schema                                              |                                                                                                                                                            |
| Service discovery             | Consul (or Eureka, for learning the classic pattern)                                                   | AWS Cloud Map is the native alternative once deployed to ECS                                                                                               |
| **Explicitly skipped**        | Spring Cloud Stream, Spring Cloud Config Server, Spring Cloud Contract, saga frameworks (Axon/Camunda) | direct spring-kafka/spring-amqp is more explicit; env vars/Secrets Manager cover config; hand-rolled orchestration saga teaches more than a framework here |

## 14. AWS Mapping

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

## 15. Repository / File Structure

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
│   │   ├── connectors/       (RailAdapter, StripeBankAdapter — in-process,
│   │   │                      no separate service; called by saga/ directly)
│   │   ├── kafka/            (producer + consumer)
│   │   └── controller/
│   ├── Dockerfile
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

## 16. Build Order

1. **Ledger Service alone** — domain, balance-from-entries logic, REST API,
   own Postgres, unit tests proving entries always net to zero
2. **Payment Orchestrator**, calling Ledger Service over plain synchronous
   REST (no Kafka, no saga yet — intentionally "wrong" for true
   microservices, but gets two real services talking first)
3. Introduce **Kafka** + the pending_events pattern on Ledger Service; rework the flow
   into the **orchestration saga** (§6), including the compensating reversal
4. **Swan sandbox** integration in Payment Orchestrator's `connectors/` package,
   replacing a `NoopAdapter`; wire Swan webhooks into the saga
5. **RabbitMQ** notification path
6. **Audit Service** consuming Kafka
7. Cross-cutting: **Resilience4j** on the Orchestrator→Ledger call, then
   **tracing** (Zipkin locally), then **API Gateway** with Spring Security
   JWT + the `ADMIN` role (§11), then **service discovery** (Consul/Eureka)
8. **AWS deployment** (RDS ×N, MSK, ECS, Cloud Map, Cognito replacing the
   local JWT credential) + IaC
9. Stretch goals: GraphQL on Audit Service, Spring Batch for payroll runs
   - reconciliation, OpenSearch transaction search
