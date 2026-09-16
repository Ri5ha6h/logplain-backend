# Future Protocol Roadmap

Status: Deferred from the initial REST-only multi-tenant version.

This document captures future protocol capabilities without expanding the current v1 implementation scope. Any future connector must preserve the existing tenant context, transaction lifecycle, idempotency, retry, telemetry, quota, security, and retention principles.

## SFTP

### Candidate Capabilities

- Durable partner-server polling trigger.
- Outbound file-transfer action.
- Hosted file-transfer server as a separate capability if direct inbound hosting becomes necessary.

### Design Questions

- How should remote files receive stable delivery identities across polling cycles?
- Which claim, archive, move, rename, and quarantine policies should be supported?
- How should partially uploaded files be detected before processing?
- What happens when a remote server accepts a file but the connection times out?
- Should polling schedules share the general durable scheduler or use connector-specific polling leases?
- How should large files map to tenant-scoped blob storage and retention policies?
- Which tenant quotas apply to polling frequency, file size, concurrent transfers, and storage?
- Which authentication methods and external secret references are required?

### Constraints

SFTP polling, outbound transfer, and hosted server capabilities should remain separate connector roles. A future implementation must not make file-session behavior part of the generic REST contracts.

## SMTP

### Candidate Capabilities

- Outbound SMTP relay action.
- Direct inbound SMTP reception.
- Mailbox ingestion as a separate trigger capability.

### Design Questions

- Should inbound mail be accepted directly or collected from a mailbox provider?
- How should sender authentication, anti-abuse controls, rate limits, and tenant quotas work?
- How should attachments be represented in the canonical message envelope?
- What delivery states are available from the SMTP relay?
- How should uncertain delivery be reconciled after a timeout?
- Which tenant-scoped credentials, sender policies, and retention rules apply?
- How should raw message bodies and attachments be redacted, encrypted, and expired?

### Constraints

Outbound relay, direct inbound reception, and mailbox ingestion should remain distinct connector roles. Each must create or continue tenant-scoped transactions through the same durable intake and flow runtime contracts.

## Shared Future Principles

Future connectors must:

- Establish an authoritative `TenantContext` before intake or outbound execution.
- Enforce tenant ownership, quotas, authorization, and external secret references.
- Use globally unique transaction IDs and tenant/endpoint-scoped idempotency.
- Record protocol lifecycle facts without replacing the common transaction state machine.
- Support bounded retry and explicit uncertain-delivery handling.
- Emit tenant-safe traces, metrics, logs, and audit records.
- Store large payloads and attachments through tenant-scoped blob references.
- Respect tenant-specific retention, redaction, and deletion policies.
- Expose protocol behavior through explicit connector trigger/action contracts.

