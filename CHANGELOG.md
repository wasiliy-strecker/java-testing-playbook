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
