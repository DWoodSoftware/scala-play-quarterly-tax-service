# scala-play-quarterly-tax-service

[![Scala Tests](https://github.com/DWoodSoftware/scala-play-quarterly-tax-service/actions/workflows/scala-tests.yml/badge.svg)](https://github.com/DWoodSoftware/scala-play-quarterly-tax-service/actions/workflows/scala-tests.yml)

A Scala 3 and Play Framework portfolio project for a fictional quarterly tax reporting service. This repository is an independent technical demonstration using entirely fictional data and is not affiliated with HMRC or any government organisation.

## What this project is

This project demonstrates a production-style backend architecture in Scala with Play Framework. It focuses on clear separation of concerns, immutable domain modelling, test-driven development, and a simple layered structure that can grow into a fuller service over time.

## What is implemented right now

The current implementation includes:

- A minimal Play application bootstrap
- A health endpoint at GET /health returning JSON with the status UP
- A test-first controller spec covering the health endpoint behaviour
- Basic project tooling for formatting, testing, and local development

## Architecture at a glance

The application currently follows a thin MVC-style structure:

- Routes define the HTTP surface
- Controllers handle request/response concerns
- The domain and service layers are intentionally simple at this stage and are designed to grow into a fuller layered backend

## Core technology stack

- Scala 3
- Play Framework 3.x
- sbt
- ScalaTest
- scalafmt

## Running locally

Prerequisites:

- Java 17
- sbt

Start the application:

```bash
sbt run
```

Then open:

- http://localhost:9000/health

## Running tests

Run the test suite:

```bash
sbt test
```

Run formatting checks:

```bash
sbt scalafmtCheckAll
```

## Documentation

For additional context and planned follow-on material, see:

- [APPFLOW.md](APPFLOW.md)
- [TYPES.md](TYPES.md)
- [ROADMAP.md](ROADMAP.md)

## Disclaimer

This repository is an independent technical demonstration using fictional data. It is not affiliated with HMRC or any government organisation.
