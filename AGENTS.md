# Repository Guidelines

## Purpose

This repository demonstrates production-minded Java testing through one stock
reservation service. Every example must protect a concrete behavior or failure
mode. Do not add isolated calculator, string utility, or mocking demonstrations.

## Module boundaries

- `reservation-core` contains framework-independent domain and application
  code.
- `reservation-testkit` contains reusable fixtures, fakes, and behavioral
  contracts. Production modules may depend on it only from test scope.
- `reservation-service` owns Spring Boot, HTTP, PostgreSQL, and external
  catalog adapters.

Core code must not depend on Spring, Jakarta Persistence, servlet APIs, JDBC,
or a test framework. Adapters depend inward on core ports.

## Verification

- `./mvnw clean verify`: run all checks available without external containers.
- Java 21 is the language baseline; CI also verifies Java 25.
- PostgreSQL behavior must use Testcontainers. Never replace it with H2 or
  silently skip tests when Docker is unavailable.
- Concurrency tests must coordinate with latches or barriers, not sleeps.
- Time and generated identifiers must be controllable in tests.
- Mutation and coverage thresholds are quality signals, not targets to game
  with meaningless assertions or exclusions.

## Style and documentation

Use Google Java Format through Spotless and keep compiler lint warnings fatal.
Name tests after behavior and expected outcome. Document why a test belongs at
its chosen layer and record meaningful toolchain trade-offs as ADRs.

Never commit credentials, generated reports, local databases, build output, or
downloaded corpora.
