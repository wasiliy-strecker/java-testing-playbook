# ADR 0001: Keep the property and mutation toolchain on JUnit Platform 1.x

- Status: Accepted
- Date: 2026-07-31

## Context

Java 21 is the repository baseline and JUnit 6 is available. The planned
property-based tests use jqwik 1.10, which targets JUnit Platform 1.14. The
current PIT JUnit plugin also tracks Platform 6 support separately.

Forcing those tools onto an unsupported platform would make the quality gate
less trustworthy than the code it verifies.

## Decision

Use Spring Boot 3.5.16 and JUnit 5.14.4 for this repository while compiling all
production sources for Java 21. Verify the complete reactor on Java 21 and Java
25.

Do not mix JUnit Platform major versions inside the reactor. Re-evaluate the
decision when both jqwik and the PIT JUnit plugin officially support Platform
6, then migrate in one explicit dependency change.

## Consequences

- Property and mutation tests use supported integration points.
- The portfolio already demonstrates Spring Boot 4 elsewhere; this repository
  additionally reflects the Spring Boot 3 generation common in maintained
  enterprise systems.
- Dependabot may propose a JUnit 6 upgrade, but it must not be merged until the
  compatibility condition is satisfied.
