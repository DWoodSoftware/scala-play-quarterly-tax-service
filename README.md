# scala-play-quarterly-tax-service

[![Scala Tests](https://github.com/DWoodSoftware/scala-play-quarterly-tax-service/actions/workflows/scala-tests.yml/badge.svg)](https://github.com/DWoodSoftware/scala-play-quarterly-tax-service/actions/workflows/scala-tests.yml)

A Scala 3 and Play Framework portfolio project for a fictional quarterly tax reporting service. This repository is an independent technical demonstration using entirely fictional data and is not affiliated with HMRC or any government organisation.

## What this project is

This project demonstrates a production-style backend architecture in Scala with Play Framework. It focuses on clear separation of concerns, immutable domain modelling, test-driven development, and a simple layered structure that can grow into a fuller service over time.

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
- Docker 

## Live demo

The interactive demo is available through GitHub Pages:

- Frontend: https://dwoodsoftware.github.io/scala-play-quarterly-tax-service/
- Backend API: https://scala-play-quarterly-tax-service.onrender.com
- Health check: https://scala-play-quarterly-tax-service.onrender.com/health

The demo supports the complete create → retrieve → submit quarterly update lifecycle against the deployed Scala/Play backend.

> The backend uses Render's free tier and may require a short cold start after a period of inactivity.

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

## Running with Docker

The service is packaged as a multi-stage Docker image and runs using the
production Play distribution.

### Build

```bash
docker build -t quarterly-tax-service:local .
```

### Run locally

```bash
docker run --rm \
  -p 9000:9000 \
  --name quarterly-tax-service \
  quarterly-tax-service:local
```

The container entrypoint automatically generates an ephemeral application
secret for local execution when `APPLICATION_SECRET` is not provided.

This allows the same startup command to be used across Windows, macOS, and
Linux without requiring host-specific secret-generation scripts.

The service is available on port `9000`.

### Verify

```bash
curl http://localhost:9000/health
```

Expected response:

```json
{
  "status": "UP"
}
```

### Production configuration

Automatic secret generation is intended for local execution only.

When running with `APP_ENV=production`, an application secret must be
provided explicitly:

```bash
docker run --rm \
  -p 9000:9000 \
  -e APP_ENV=production \
  -e APPLICATION_SECRET="<secure-secret>" \
  quarterly-tax-service:local
```

The container will refuse to start in production mode without
`APPLICATION_SECRET`.

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
