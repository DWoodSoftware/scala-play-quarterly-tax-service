# Types and contracts

This document is the canonical reference for the domain contracts and important Scala types used by this project. It focuses on the data shapes and architectural contracts that matter to engineers working on the service.

## Status legend

- Implemented: available in the current codebase.
- Planned: intended for future implementation.
- Deprecated: no longer part of the intended design.

## Current implementation

The current codebase is intentionally minimal, so the implemented type surface is small. The project is already structured around the expectation that the domain layer will grow into richer contracts over time.

## Health response

Name: HealthResponse

Status: Implemented

Purpose: A simple JSON response returned by the health endpoint.

Scala shape:

```scala
final case class HealthResponse(status: String)
```

Field meanings:

- status: A string describing the service health state. In the current implementation it is always "UP".

Invariants:

- The status value is a non-empty string.
- In the current implementation, the value is always "UP".

Related types:

- None

Notes:

- This type exists to make the health endpoint response explicit and easy to document.
- It is currently represented as a simple case class rather than a more complex sealed model because the behaviour is intentionally minimal.

## Controller action contract

Name: HealthController.health

Status: Implemented

Purpose: Exposes the current health check behaviour over HTTP.

Scala shape:

```scala
def health: Action[AnyContent]
```

Field meanings:

- No input parameters are required.

Invariants:

- The action always returns a successful HTTP response.
- The response body contains JSON with a status field.

Related types:

- HealthResponse

Notes:

- This is an HTTP-facing contract rather than a domain contract.
- It is intentionally simple and should remain thin as the application grows.

## Planned domain types

The following types are planned for future implementation and are not yet part of the codebase.

### QuarterlyUpdate

Name: QuarterlyUpdate

Status: Planned

Purpose: Represents a quarterly reporting submission or draft update for a fictional reporting period.

Scala shape:

```scala
final case class QuarterlyUpdate(
  id: String,
  quarter: Quarter,
  status: UpdateStatus,
  submittedAt: Option[Instant],
  payload: QuarterlyUpdatePayload
)
```

Field meanings:

- id: Unique identifier for the quarterly update.
- quarter: The reporting period represented by the update.
- status: The lifecycle state of the update.
- submittedAt: The submission timestamp when the update has been submitted.
- payload: The core reporting content for the update.

Invariants:

- The id must be unique within the repository.
- The quarter must be a valid reporting period.
- A submitted update must have a defined submittedAt value.
- A draft update must not have a submittedAt value.

Related types:

- Quarter
- UpdateStatus
- QuarterlyUpdatePayload

### Quarter

Name: Quarter

Status: Planned

Purpose: Represents a reporting period.

Scala shape:

```scala
sealed trait Quarter
```

Field meanings:

- A quarter is a strongly typed representation of a reporting period.

Invariants:

- Only valid reporting periods should be constructible.

Related types:

- QuarterlyUpdate

### UpdateStatus

Name: UpdateStatus

Status: Planned

Purpose: Represents the lifecycle state of a quarterly update.

Scala shape:

```scala
sealed trait UpdateStatus
```

Field meanings:

- Draft: the update is still being prepared.
- Submitted: the update has been submitted for processing.

Invariants:

- A transition must follow the intended lifecycle order.

Related types:

- QuarterlyUpdate

### QuarterlyUpdatePayload

Name: QuarterlyUpdatePayload

Status: Planned

Purpose: Contains the reporting values that belong to a quarterly update.

Scala shape:

```scala
final case class QuarterlyUpdatePayload(
  periodStart: LocalDate,
  periodEnd: LocalDate,
  reportedAmount: BigDecimal,
  notes: String
)
```

Field meanings:

- periodStart: The start date of the reporting period.
- periodEnd: The end date of the reporting period.
- reportedAmount: The financial amount reported for the period.
- notes: Optional narrative context for the update.

Invariants:

- periodStart must be before or equal to periodEnd.
- reportedAmount must be non-negative.
- notes should be non-empty when present.

Related types:

- QuarterlyUpdate

## Planned service contracts

### QuarterlyUpdateService

Name: QuarterlyUpdateService

Status: Planned

Purpose: Coordinates the application flow for creating, validating, and managing quarterly updates.

Scala shape:

```scala
trait QuarterlyUpdateService {
  def create(update: QuarterlyUpdate): Future[Either[DomainError, QuarterlyUpdate]]
}
```

Field meanings:

- create: Accepts a quarterly update and returns a future result that may succeed or fail with a domain error.

Invariants:

- Validation should happen before persistence.
- The method should not directly depend on Play HTTP types.

Related types:

- QuarterlyUpdate
- DomainError

### QuarterlyUpdateRepository

Name: QuarterlyUpdateRepository

Status: Planned

Purpose: Abstracts persistence for quarterly updates.

Scala shape:

```scala
trait QuarterlyUpdateRepository {
  def save(update: QuarterlyUpdate): Future[QuarterlyUpdate]
  def findById(id: String): Future[Option[QuarterlyUpdate]]
}
```

Field meanings:

- save: Persists a quarterly update.
- findById: Retrieves a quarterly update by identifier.

Invariants:

- The repository should expose asynchronous interfaces.
- The repository should not leak HTTP-specific concerns.

Related types:

- QuarterlyUpdate

## Planned error types

### DomainError

Name: DomainError

Status: Planned

Purpose: Represents a business-level failure that may be returned by a service.

Scala shape:

```scala
sealed trait DomainError
```

Field meanings:

- Domain errors describe the reason a requested business action could not proceed.

Invariants:

- Errors should be explicit and meaningful.
- Domain errors should not be tied to HTTP transport details.

Related types:

- QuarterlyUpdateService

## Notes on wrapper semantics

- Option is intended for absence. For example, a missing entity lookup may return None.
- Either is intended for recoverable failures. A service can return Either[DomainError, T] when the business operation may fail without crashing the application.
- Future is intended for asynchronous work such as repository access or other I/O-bound operations.

## Design guidance

The project is intended to follow a clear separation of concerns:

- HTTP concerns stay in routes and controllers.
- Domain rules stay in domain types and service logic.
- Persistence stays behind repository abstractions.
- Error types stay domain-focused rather than HTTP-focused.
