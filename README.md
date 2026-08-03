# Java Testing Playbook

[![Verify](https://github.com/wasiliy-strecker/java-testing-playbook/actions/workflows/verify.yml/badge.svg)](https://github.com/wasiliy-strecker/java-testing-playbook/actions/workflows/verify.yml)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](LICENSE)

A production-minded Java testing reference built around one coherent stock
reservation service.

The repository will connect each testing technique to a concrete risk:
invalid business transitions, overselling under concurrency, transaction
rollback, database contract drift, malformed upstream responses, and unstable
HTTP behavior.

> **Current milestone — PostgreSQL transaction safety:** rollback tests now prove
> that partial reservation workflows leave no committed changes. A coordinated
> Virtual Thread scenario sends 100 concurrent reservation attempts against 37
> units and verifies exactly 37 reservations without negative inventory.

## Planned proof points

| Risk | Verification approach |
|---|---|
| Invalid reservation transitions | deterministic domain tests |
| Unexplored input combinations | property-based invariants |
| Assertions that never detect regressions | mutation testing |
| Adapter implementations drifting apart | reusable contract tests |
| Negative stock under concurrent requests | real PostgreSQL concurrency test |
| Catalog protocol or error drift | WireMock boundary tests |
| Wiring and JSON mismatches | black-box component tests |

The project will favor explicit clocks, identifiers, barriers, and fakes over
global state, sleeps, and broad mocking.

## Module map

| Module | Responsibility |
|---|---|
| `reservation-core` | Framework-independent domain and application boundary |
| `reservation-testkit` | Reusable fixtures, fakes, and behavioral contracts |
| `reservation-service` | Spring Boot, PostgreSQL, REST, and catalog adapters |

Production code must not depend on `reservation-testkit`. The service may use
it only from test scope.

## Adapter contract strategy

`reservation-testkit` exports abstract contracts for the reservation and stock
ports. Every adapter supplies only its setup and state-inspection hooks; all
observable behavior and failure semantics are asserted once. The in-memory
fakes run those contracts as fast unit tests, while the JDBC implementations run
them unchanged as PostgreSQL integration tests.

## Transaction safety scenarios

Spring-managed transactions are exercised against real PostgreSQL row locks.
One test forces a duplicate reservation after stock has already been decreased;
another fails after a reservation release and restock. Both assert the original
database state after rollback.

The overselling scenario starts every Virtual Thread through explicit latches,
uses bounded waits, and never relies on timing sleeps. Successful transactions,
stored reservations, and remaining stock must agree exactly.

## Build

Requirements: a full JDK from version 21 through 25 and a Docker-compatible
container runtime. The Maven Wrapper downloads the pinned Maven distribution.

```bash
./mvnw clean verify
```

CI runs the same reactor on Java 21 and Java 25.

Database integration tests always start a disposable PostgreSQL container.
They never fall back to H2 and never skip silently when Docker is unavailable.

## Deliberate toolchain boundary

Java 21 is the production baseline. Spring Boot 3.5 and JUnit 5.14 are selected
for the testing example because jqwik and the planned PIT integration still
target JUnit Platform 1.x. The decision is recorded in
[ADR 0001](docs/decisions/0001-testing-toolchain.md) and will be revisited when
both tools support JUnit Platform 6.

## Roadmap

- [x] Reproducible multi-module build and Java 21/25 CI
- [x] Reservation domain with deterministic unit tests
- [x] Testkit and property-based invariants
- [x] PostgreSQL contracts and concurrency scenarios
- [ ] HTTP boundary and component tests
- [ ] Coverage and mutation quality gates
- [ ] First tagged release

## License

Licensed under the [Apache License 2.0](LICENSE).
