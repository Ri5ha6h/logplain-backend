# Logplain Backend

Logplain is a Java 26 integration runtime for many Tenants.

The first release is designed as a modular monolith. It accepts REST requests, creates durable Transactions, runs versioned Flows, and can call external REST services through Tenant-owned RestDestinations.

## Project documents

- [PLAN.md](PLAN.md) defines the implementation phases and product scope.
- [ARCHITECTURE.md](ARCHITECTURE.md) defines the system boundaries and domain language.
- [IMPLEMENTED.md](IMPLEMENTED.md) records the implementation status.
- [LATER.md](LATER.md) records deferred protocol work.

## Technology

- Java 26
- Maven
- Spring Boot 4.1.x is planned for the runtime shell.
- PostgreSQL is planned for durable multi-Tenant data.
- S3-compatible object storage is planned for large payloads and archived history.

## Build and test

Run the Phase 1 build and tests with:

```text
mvn verify
```

The domain code is framework-independent. Spring Boot, PostgreSQL, REST hosting, and durable execution are added in later phases.
