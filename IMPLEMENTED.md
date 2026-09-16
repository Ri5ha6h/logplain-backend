# Logplain Implementation Status

This file records the implementation status for each phase in `PLAN.md`.

## Current status

Phase 1 is complete. The code contains domain rules and framework-neutral contracts.

| Phase | Status | Main result |
| --- | --- | --- |
| 1. Domain, Tenant, and Core Contracts | Complete | Tenant lifecycle, Tenant scope, quotas, Transaction states, Step states, Attempt states, REST contracts, Extension visibility, retry rules, and idempotency scope are implemented and tested. |
| 2. Durable Multi-Tenant Execution | Not started | PostgreSQL storage, row-level security, events, work claims, leases, retries, outbox records, and audit records are pending. |
| 3. Flow Runtime | Not started | Flow execution, waits, cancellation, fan-out, fan-in, version pinning, and recovery commands are pending. |
| 4. REST Connectors | Not started | Spring REST intake, response handling, outbound HTTP execution, authentication, and delivery classification are pending. |
| 5. Administration and Operations | Not started | Tenant administration, configuration publication, roles, telemetry, retention, quotas, and health endpoints are pending. |
| 6. Hardening and Validation | Not started | Isolation, recovery, duplicate request, quota, uncertain delivery, compatibility, retention, rollback, and connector tests are pending. |

## Phase 1 details

### Tenant and TenantContext

- `Tenant` uses the lifecycle: `Provisioning`, `Active`, `Suspended`, `Retiring`, `Deleted`.
- New REST intake is allowed only when the Tenant is `Active`.
- `TenantContext` contains trusted Tenant identity, principal, permissions, and explicit platform scope.
- A request Tenant ID is not an authority source.
- Tenant-owned objects can check access with `TenantContext`.

### Quota

`QuotaPolicy` checks request rate, concurrent Transactions, queue depth, payload size, outbound calls, retention, and Extension runtime.

### Transaction, Step, and Attempt

- A `Transaction` starts as `Accepted` and stores the Endpoint and exact `FlowVersion`.
- A `Transaction` allows only documented state changes.
- A `StepExecution` contains ordered `Attempt` records.
- A retry creates another `Attempt` under the same Step and Transaction.
- An uncertain Attempt moves the Step to `Waiting`.

### REST and Extension contracts

- `RestTrigger` defines authentication and Tenant REST intake.
- `RestAction` defines Tenant-authorized outbound REST calls.
- `RestDestination` stores a secret reference, not a secret value.
- `ExtensionDescriptor` stores an immutable version and `ExtensionVisibility`.
- Shared Extensions require an explicit Tenant grant. Tenant-specific Extensions match one Tenant only.

### Error, retry, and idempotency rules

- `DomainError` has a category, code, and safe message.
- `RetryPolicy` limits attempts and uses a bounded backoff.
- `UNCERTAIN_DELIVERY` is never retried automatically.
- `IdempotencyScope` is exactly `TenantId + EndpointId + clientKey`.

## Verification

Run:

```text
mvn test
```

The Phase 1 tests cover Tenant lifecycle, Tenant isolation, quota rejection, Transaction state changes, retry Attempts, uncertain delivery, retry policy, and Extension visibility.
