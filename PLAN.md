# Logplain Backend Plan

Status: Design phase. No implementation has started.

## Purpose

Logplain is a Java 26 integration runtime.

It accepts REST requests from external systems. It tracks each accepted request as a `Transaction`. It runs a versioned `Flow`. A `Flow` can call an external REST service.

Many `Tenant` objects will use the same runtime. Each Tenant has its own data, users, configuration, limits, and permissions.

The first release will use one modular monolith. A modular monolith is one deployed application with clear internal modules. Keep the module boundaries clear so the runtime can be split later if needed.

## Main Decisions

### Java and Spring Boot

- Use Java 26.
- Use Spring Boot 4.1.x for the runtime shell. Pin the project to the latest verified 4.1.x patch release during implementation.
- Use Spring Framework 7.0.9 or newer through Spring Boot 4.1.x.
- Use Maven as the build tool. Use Maven 3.6.3 or newer.
- Use Spring Boot for REST hosting, OpenAPI, health checks, metrics, and security integration.
- Keep the domain and application code independent of Spring.

### Multi-Tenant Runtime

- Use one shared runtime for many Tenants.
- Use shared PostgreSQL infrastructure.
- Every Tenant-owned record must include Tenant ownership.
- Use database row-level security. This makes the database block access to rows from another Tenant.
- Use Tenant-aware authorization, quotas, logs, cache keys, and object-storage keys.

### PostgreSQL

Use PostgreSQL for data that the runtime must keep and recover:

- Tenant and user permissions.
- Tenant configuration.
- Transactions and their current status.
- Transaction events.
- Work claims and retries.
- Idempotency records.
- Quotas and audit records.

### Object Storage

Use S3-compatible object storage for large request bodies, attachments, and archived history.

PostgreSQL stores the object reference and metadata. It does not store large request bodies by default. Every object key includes Tenant scope.

### Durable Processing

Durable processing means that the runtime stores work before it reports acceptance. The runtime can recover the work after a restart.

Each accepted request creates one root `Transaction`. The runtime stores an append-only event history and a current status record.

### Duplicate Requests

The runtime uses at-least-once processing. This means a request can run more than once if the runtime must recover from a failure.

The runtime uses idempotency to handle duplicates safely. An idempotency key is unique within this scope:

```text
tenantId + endpointId + clientKey
```

Transaction IDs are globally unique. A Tenant can use its transaction ID to access only its own transaction.

### Extensions

An `Extension` is a versioned Java artifact that adds a `Flow`, `Step`, `RestTrigger`, or `RestAction`.

- `PlatformShared` extensions can be made available to selected Tenants.
- `TenantSpecific(tenantId)` extensions belong to one Tenant.
- The platform reviews and packages all extensions.
- The runtime activates extensions during deployment and startup.
- Tenants cannot upload or hot-load arbitrary Java code in v1.

### Security

Tenant REST callers use credentials that are bound to a Tenant and an `Endpoint`. The credential can use an API key, OAuth2 client credentials, mTLS, or a signed request.

The runtime never trusts a Tenant ID from a request by itself. It uses the authenticated credential or a server-managed client binding to create the `TenantContext`.

Administration uses external identity and authorization. Secrets are stored in an external secret store. Configuration stores only secret references.

### Telemetry

Use OpenTelemetry for traces and metrics. Use structured logs for search and analysis.

Connect REST requests, Tenant context, Transactions, Steps, Attempts, outbound actions, and operator commands with safe correlation IDs. Do not write raw secrets or raw payloads to logs by default.

## v1 Features

- Tenant-owned REST inbound `Endpoint` objects.
- Tenant-owned `RestDestination` objects for outbound REST calls.
- `RestTrigger` objects that accept authenticated REST requests.
- `RestAction` objects that call external REST services.
- Synchronous REST responses for short-running Flows.
- Asynchronous REST responses for long-running Flows. Return `202 Accepted` and a globally unique transaction reference.
- Durable PostgreSQL-backed Transactions and work claims.
- Append-only lifecycle events and a current Transaction status.
- At-least-once processing with Tenant and Endpoint scoped idempotency.
- Tenant quotas for request rate, concurrency, queue depth, payload size, outbound calls, retention, and extension runtime.
- Tenant lifecycle and suspension controls.
- Platform administrator and Tenant administrator roles.
- Tenant-scoped operator and auditor roles.
- Versioned Java `Flow` and `Step` implementations.
- REST `Action` policies for timeout, retry, response classification, rate limits, and correlation.
- Shared platform Extensions and Tenant-specific Extensions.
- Durable waits and resumable Flow execution.
- Bounded fan-out and fan-in with an explicit result policy.
- Tenant-authorized Transaction search and status inspection.
- Audited retry, redrive, resume, cancel, and replay-from-step operations.
- Draft, validate, publish, retire, and roll back Tenant configuration.

## Tenant Rules

### Tenant Identity

The runtime creates `TenantContext` from an authenticated credential or a server-managed client binding.

A Tenant ID in a path, header, or request body is only a routing hint. The runtime rejects it when it does not match the authenticated Tenant.

### Tenant Isolation

Every Tenant-owned record carries Tenant scope. This includes:

- Database rows.
- Repository calls.
- Work items.
- Blob references.
- Cache entries.
- Idempotency records.
- Telemetry records.
- Audit records.

The application checks Tenant scope. PostgreSQL row-level security checks it again at the database level.

### Tenant Lifecycle

```text
Provisioning → Active → Suspended → Retiring → Deleted
```

- `Provisioning`: the platform creates the Tenant resources.
- `Active`: the Tenant can use the runtime within its quotas.
- `Suspended`: new intake stops. Authorized recovery can still run.
- `Retiring`: new configuration and intake stop while retention and cleanup run.
- `Deleted`: access is removed after controlled, retention-aware deletion.

### Roles and Platform Operations

- Platform administrators manage Tenants and can perform approved cross-Tenant operations.
- Tenant administrators manage resources inside their Tenant.
- Operators and auditors are Tenant-scoped by default.
- Cross-Tenant operations require explicit platform scope.
- The runtime records the actor, reason, Tenant scope, and affected resources for each platform operation.

### Quotas

The runtime checks quotas before it accepts a request. Quotas control:

- Request rate.
- Concurrent Transactions.
- Queue depth.
- Payload size.
- Outbound calls.
- Data retention.
- Extension runtime.

## Deferred Work

- Interpreted source snippets and general-purpose scripting runtimes.
- Untrusted Tenant-uploaded code.
- Runtime hot-loading of Java artifacts.
- Full event sourcing.
- Dedicated message-broker infrastructure.
- Arbitrary asynchronous execution graphs.
- Exactly-once external effects.
- Multi-region active-active deployment.
- Visual flow authoring.
- Future protocol work described in `LATER.md`.

## Implementation Phases

### Phase 1: Domain, Tenant, and Core Contracts

- Define Tenant lifecycle and Tenant ownership rules.
- Define `TenantContext` and quota rules.
- Define `Transaction`, `Step`, and `Attempt` states.
- Define `RestTrigger` and `RestAction` contracts.
- Define Extension visibility and version rules.
- Define error, retry, and idempotency rules.

### Phase 2: Durable Multi-Tenant Execution

- Store Tenant data and Transaction data in PostgreSQL.
- Add row-level security.
- Store lifecycle events and current status.
- Add Tenant-aware work claims and leases.
- Add retries, idempotency, quota checks, and restart recovery.
- Add outbox records and audit records.

### Phase 3: Flow Runtime

- Run typed sequential Steps.
- Store and resume asynchronous waits.
- Add timeouts and cancellation.
- Add bounded fan-out and fan-in.
- Pin Flow and Extension versions to each Transaction.
- Enforce Extension visibility.
- Add Tenant-scoped recovery commands.

### Phase 4: REST Connectors

- Add Tenant-scoped REST intake.
- Add synchronous and asynchronous response handling.
- Add Tenant-owned REST destinations and outbound actions.
- Add REST authentication policies.
- Add response classification, rate limits, retries, and correlation.

### Phase 5: Administration and Operations

- Add Tenant lifecycle management.
- Add versioned configuration publication.
- Add Tenant and platform administration endpoints.
- Add OIDC/mTLS authentication and role checks.
- Add OpenTelemetry instrumentation.
- Add payload retention and quota monitoring.
- Add health checks.

### Phase 6: Hardening and Validation

- Test cross-Tenant isolation.
- Test restart recovery and duplicate requests.
- Test quota enforcement and Tenant suspension.
- Test uncertain outbound delivery.
- Test Extension compatibility and visibility.
- Test payload redaction and retention.
- Test deployment rollback.
- Test platform-scoped operations.
- Test REST connector contracts.

## Before Implementation Starts

Implementation can start only when:

- Tenant ownership and isolation rules are stable.
- Tenant lifecycle and quota rules are agreed.
- REST trigger and action contracts are clear.
- Transaction states and valid state changes are documented.
- Retry, idempotency, timeout, cancel, redrive, and replay rules are clear.
- Endpoint, destination, Flow, Extension, and configuration version rules are clear.
- Shared and Tenant-specific Extension visibility can be tested.
- REST authentication and Tenant resolution rules are agreed.
- Payload, secret, telemetry, and retention rules are agreed.
- Failure and uncertain-delivery behavior can be tested.
- The domain code does not depend on Spring.
- `PLAN.md`, `ARCHITECTURE.md`, and `LATER.md` describe the same v1 scope.
