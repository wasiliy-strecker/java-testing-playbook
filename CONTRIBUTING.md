# Contributing

Keep contributions tied to a concrete reservation risk or testability
improvement. Before opening a pull request, run:

```bash
./mvnw clean verify
```

Use focused commits, add a behavioral test with each behavior change, and
explain why the chosen test layer is the cheapest reliable place to catch the
regression. PostgreSQL tests must use Testcontainers and must fail clearly when
Docker is unavailable.
