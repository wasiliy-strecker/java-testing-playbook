# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Multi-module Maven foundation for the reservation core, testkit, and service
- Java 21 baseline with Java 21 and Java 25 continuous integration
- Documented testing strategy, module boundaries, and toolchain rationale
- Framework-independent reservation lifecycle with explicit catalog, stock,
  persistence, transaction, time, and identity boundaries
- Deterministic domain and use-case tests for validation, insufficient stock,
  product eligibility, lookup, and idempotent release
- Reusable deterministic clock, identifier generator, fixture builder, and
  thread-safe in-memory adapters in `reservation-testkit`
- Replayable jqwik properties for stock conservation and reservation lifecycle
  invariants
- Spring JDBC reservation and stock adapters behind the framework-independent
  application ports
- Flyway-managed PostgreSQL schema with lifecycle and stock constraints
- Spring Boot integration coverage against a disposable PostgreSQL 17 container
- Reusable reservation and stock port contracts executed against both in-memory
  and PostgreSQL adapters
- Consistent application exceptions for duplicate reservations and missing
  reservation or stock records
- PostgreSQL rollback verification for failed reservation creation and release
  workflows
- Coordinated Virtual Thread integration scenario proving that concurrent
  reservations cannot oversell stock
