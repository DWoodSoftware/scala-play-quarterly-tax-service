# scala-play-quarterly-tax-service

A small portfolio project demonstrating a production-style Scala 3 and Play Framework backend for a fictional quarterly tax reporting service.

## Purpose

This repository is an independent technical demonstration using entirely fictional data. It is not affiliated with HMRC or any government organisation.

## Technology stack

- Scala 3
- Play Framework
- sbt
- ScalaTest
- scalafmt

## Architectural approach

The project is structured around a layered MVC-style backend with clear separation between routes, controllers, services, domain logic, and persistence abstractions.

## TDD approach

Business behavior is introduced through tests first, then implemented with the minimum production code needed to satisfy the expectation, followed by refactoring while keeping tests green.

## Local development

1. Install Java 17.
2. Install sbt.
3. Start the application:

   ```bash
   sbt run
   ```

4. Open http://localhost:9000/health

## Testing

Run the test suite:

```bash
sbt test
```

Run the formatting check:

```bash
sbt scalafmtCheckAll
```

## Disclaimer

This project is an independent technical demonstration using fictional data and is not affiliated with HMRC or any government organisation.
