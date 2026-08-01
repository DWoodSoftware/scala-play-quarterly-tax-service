# Application Flow

This document defines the intended behavioural and architectural flow of the Quarterly Tax Service.

It describes how requests move through the application, how quarterly updates progress through their lifecycle, where validation occurs, and which application layers are responsible for each operation.

Exact domain models, contracts, enums, and error types are defined separately in [TYPES.md](./TYPES.md).

Implementation progress is tracked in [ROADMAP.md](./ROADMAP.md).

---

## 1. Application Purpose

The application is a small Scala and Play Framework microservice modelling a fictional quarterly digital financial reporting system.

A client can:

1. Create a quarterly financial update.
2. Supply income and expense records.
3. Retrieve an existing update.
4. Validate the update against domain rules.
5. Submit a valid draft.
6. Receive explicit validation or state-transition errors when an operation cannot be completed.

The service calculates financial totals itself rather than trusting derived values supplied by the client.

The application does **not** calculate real tax liabilities or communicate with HMRC.

All data and behaviour are fictional and exist solely as a technical demonstration.

---

# 2. High-Level Architecture

The normal application flow is:

```text
HTTP Request
      │
      ▼
Play Router
      │
      ▼
Controller
      │
      ▼
Application Service
      │
      ├────► Domain Validation / Business Rules
      │
      ▼
Repository Contract
      │
      ▼
Persistence Implementation
      │
      ▼
Application Service
      │
      ▼
Controller
      │
      ▼
HTTP Response
```

Each layer has a deliberately limited responsibility.

HTTP concerns must not leak into the domain layer, and persistence implementation details must not influence business rules.

---

# 3. Quarterly Update Lifecycle

A quarterly update progresses through an explicit lifecycle.

The initial intended state model is:

```text
                 ┌─────────────┐
                 │    DRAFT    │
                 └──────┬──────┘
                        │
                     validate
                        │
                ┌───────┴────────┐
                │                │
                ▼                ▼
          validation         validation
           succeeds            fails
                │                │
                ▼                ▼
          ┌───────────┐    remains DRAFT
          │ VALIDATED │    + errors returned
          └─────┬─────┘
                │
              submit
                │
                ▼
          ┌───────────┐
          │ SUBMITTED │
          └───────────┘
```

`SUBMITTED` represents a terminal state for the initial implementation.

A submitted quarterly update cannot be modified or submitted again.

Exact state definitions are maintained in `TYPES.md`.

---

# 4. Creating a Quarterly Update

The primary creation endpoint will be:

```text
POST /api/v1/quarterly-updates
```

## Request flow

```text
Client
  │
  │ POST quarterly financial data
  ▼
Router
  │
  ▼
QuarterlyUpdateController
  │
  │ Parse JSON
  │ Validate request structure
  ▼
QuarterlyUpdateService
  │
  ▼
Domain Validator
  │
  │ Validate business invariants
  ▼
Calculate Derived Totals
  │
  ▼
Create DRAFT Submission
  │
  ▼
QuarterlyUpdateRepository
  │
  ▼
Persist
  │
  ▼
201 Created
```

### Controller responsibility

The controller is responsible for determining whether the incoming HTTP request can be converted into the appropriate application input.

It handles concerns such as:

* malformed JSON,
* missing required JSON properties,
* JSON type mismatches,
* HTTP status selection.

It does **not** determine whether the financial information is valid according to business rules.

### Service responsibility

Once a structurally valid request has been created, the service coordinates the application use case.

The service:

1. sends the input through domain validation,
2. calculates derived financial values,
3. constructs the appropriate domain entity,
4. coordinates persistence,
5. returns an explicit application outcome.

### Domain responsibility

The domain layer determines whether the supplied information represents a valid quarterly update.

Examples include:

* quarter must represent Q1-Q4,
* monetary amounts must satisfy the defined financial invariants,
* required financial information must be present,
* only legal state transitions may occur.

These rules should preferably be expressed using types and pure functions.

Where practical, invalid states should be made impossible to represent rather than repeatedly checked at runtime.

---

# 5. Financial Calculation Flow

The client supplies raw financial records.

For example:

```text
Income Entries
    │
    ├── Self Employment
    └── Property

Expense Entries
    │
    ├── Travel
    ├── Office Costs
    └── Professional Fees
```

The service/domain layer derives:

```text
Total Income
     │
     │ minus
     ▼
Total Expenses
     │
     ▼
Net Amount
```

Derived values must never be accepted as authoritative values from the client.

Conceptually:

```text
netAmount = totalIncome - totalExpenses
```

The exact monetary and category types are defined in `TYPES.md`.

The application deliberately does not calculate real-world tax liability.

---

# 6. Retrieving a Quarterly Update

The retrieval endpoint will be:

```text
GET /api/v1/quarterly-updates/:id
```

Flow:

```text
Request
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository.findById(id)
   │
   ▼
Future[Option[QuarterlyUpdate]]
   │
   ├── Some(update) ──► 200 OK
   │
   └── None ─────────► 404 Not Found
```

Absence is treated as an expected application outcome rather than an exceptional failure.

The repository contract therefore represents absence explicitly.

---

# 7. Validation Flow

Validation occurs at two distinct boundaries.

## Transport validation

Performed by the Play/controller layer.

This answers:

> "Can this HTTP request be converted into the expected application input?"

Examples:

* valid JSON,
* required properties present,
* correct JSON primitive types.

Failure results in:

```text
400 Bad Request
```

## Domain validation

Performed outside the controller.

This answers:

> "Does this structurally valid input satisfy our business rules?"

Conceptually:

```text
QuarterlyUpdateInput
       │
       ▼
     validate
       │
       ▼
Either[List[ValidationError], ValidQuarterlyUpdate]
```

A domain validation failure is an expected result.

It should therefore be represented through the application's type system rather than thrown as an exception.

---

# 8. Submission Flow

A valid draft may be submitted using:

```text
POST /api/v1/quarterly-updates/:id/submit
```

The intended flow is:

```text
Submission Request
       │
       ▼
Find Quarterly Update
       │
       ├── Not Found
       │      │
       │      ▼
       │   404 Not Found
       │
       ▼
Inspect Current State
       │
       ├── SUBMITTED
       │      │
       │      ▼
       │   409 Conflict
       │
       ▼
Validate
       │
       ├── Invalid
       │      │
       │      ▼
       │   Validation Errors
       │
       ▼
Transition
DRAFT → VALIDATED → SUBMITTED
       │
       ▼
Persist Updated State
       │
       ▼
Return Submission
```

State transitions are domain behaviour.

Controllers must never directly modify submission state.

---

# 9. State Transition Rules

The application should explicitly control state transitions.

Initially permitted:

```text
DRAFT → VALIDATED
VALIDATED → SUBMITTED
```

Not permitted:

```text
SUBMITTED → DRAFT

SUBMITTED → VALIDATED

SUBMITTED → SUBMITTED
```

Attempting an invalid transition should produce an explicit domain/application error.

It should not silently succeed.

---

# 10. Repository Flow

Persistence is accessed exclusively through repository contracts.

Application services should depend upon abstractions such as:

```text
QuarterlyUpdateRepository
```

rather than a particular database implementation.

The initial implementation will use:

```text
QuarterlyUpdateRepository
        ▲
        │ implements
        │
InMemoryQuarterlyUpdateRepository
```

This allows the application behaviour to be developed and tested without introducing database infrastructure.

A future persistence implementation could replace the in-memory repository without changing domain behaviour.

---

# 11. Asynchronous Boundaries

Domain calculations should remain synchronous and deterministic where possible.

For example:

```text
validate(update)

calculateTotals(update)

transitionState(update)
```

These operations do not require asynchronous execution.

Infrastructure operations may be asynchronous.

For example:

```text
repository.findById(id)

repository.save(update)
```

Repository contracts may therefore expose results such as:

```text
Future[Option[T]]
```

This distinction is intentional.

Pure domain logic should not become asynchronous simply because the surrounding application uses asynchronous infrastructure.

---

# 12. Error Flow

Errors are separated according to their origin.

```text
Incoming Request
      │
      ├── Malformed HTTP/JSON
      │       └── 400 Bad Request
      │
      ├── Domain Validation Failure
      │       └── 422 Unprocessable Entity
      │
      ├── Resource Missing
      │       └── 404 Not Found
      │
      ├── Illegal State Transition
      │       └── 409 Conflict
      │
      └── Unexpected Infrastructure Failure
              └── 500 Internal Server Error
```

Expected domain failures should use explicit typed outcomes.

Exceptions should primarily represent unexpected technical or infrastructure failures.

Exact error contracts are defined in `TYPES.md`.

---

# 13. Health Flow

**Status: Implemented**

The currently implemented endpoint is:

```text
GET /health
```

Flow:

```text
GET /health
     │
     ▼
Play Router
     │
     ▼
HealthController
     │
     ▼
200 OK

{
  "status": "UP"
}
```

This endpoint intentionally does not involve the service, domain, or repository layers because it contains no business behaviour.

It currently acts as the minimal vertical slice proving that the Play application can route, execute, serialise JSON, and be tested successfully.

---

# 14. Layer Responsibilities

## Routes

Responsible for:

* HTTP method mapping,
* URL mapping,
* controller action selection.

Routes contain no business logic.

## Controllers

Responsible for:

* HTTP request handling,
* JSON parsing,
* transport validation,
* invoking application services,
* translating application outcomes into HTTP responses.

Controllers remain thin.

## Services

Responsible for:

* coordinating use cases,
* invoking domain behaviour,
* coordinating repository operations,
* controlling application workflow.

Services should not depend on HTTP concepts.

## Domain

Responsible for:

* business entities,
* business rules,
* state transitions,
* deterministic calculations,
* domain validation.

The domain must not depend on Play HTTP APIs.

## Validators

Responsible for:

* evaluating business invariants that cannot be encoded directly into types,
* returning explicit validation outcomes.

Validation should preferably be deterministic and side-effect free.

## Repositories

Responsible for:

* persistence contracts,
* retrieving domain data,
* storing domain data.

Repositories must not contain business rules.

---

# 15. Architectural Boundaries

Dependencies should generally flow inward:

```text
HTTP / Play
     │
     ▼
Application Services
     │
     ▼
Domain
```

Persistence is accessed through abstractions:

```text
Service
   │
   ▼
Repository Contract
   ▲
   │
Infrastructure Implementation
```

The domain should not know:

* which HTTP framework is being used,
* how JSON is represented,
* which database is being used,
* how the application is deployed.

This keeps business behaviour independently testable.

---

# 16. TDD Development Flow

Application behaviour is developed incrementally using:

```text
RED
 │
 │ Define expected behaviour
 ▼
Failing Test
 │
 ▼
GREEN
 │
 │ Minimum implementation
 ▼
Passing Test
 │
 ▼
REFACTOR
 │
 │ Improve design while preserving behaviour
 ▼
Next Behaviour
```

Tests should focus on externally meaningful behaviour and domain contracts rather than implementation details.

The expected workflow for a feature is therefore:

```text
Define behaviour
      ↓
Define/update required contracts
      ↓
Write failing test
      ↓
Implement minimum behaviour
      ↓
Run tests
      ↓
Refactor
      ↓
Update documentation
      ↓
Commit coherent change
```

Where implementation reveals that the existing type model permits invalid states unnecessarily, the preferred response is to improve the type model rather than add repeated defensive checks.

---

# 17. Documentation Relationships

The repository documentation has four distinct responsibilities.

```text
README.md
    │
    └── What is this project?

APPFLOW.md
    │
    └── How does the system behave?

TYPES.md
    │
    └── What contracts and domain types exist?

ROADMAP.md
    │
    └── What are we implementing and in what order?
```

`TYPES.md` is the canonical source for exact contract definitions.

This document should reference those contracts rather than duplicate them unnecessarily.

---

# 18. Current Implementation Status

Currently implemented:

* Play application bootstrap.
* `GET /health`.
* JSON health response.
* Automated health endpoint test.

Designed but not yet implemented:

* Quarterly update domain model.
* Financial entry models.
* Validation contracts.
* Creation workflow.
* Retrieval workflow.
* Submission state machine.
* Repository abstraction.
* In-memory repository.
* Structured domain errors.
* HTTP error translation.
* Asynchronous repository operations.

Implementation order is maintained in `ROADMAP.md`.

---

# 19. Contributor Rules

When extending the application:

1. Define expected behaviour before implementation.
2. Update or define relevant contracts in `TYPES.md`.
3. Add the failing behavioural test.
4. Implement the minimum code required to satisfy the behaviour.
5. Refactor while keeping tests green.
6. Preserve architectural boundaries.
7. Update `APPFLOW.md` when application behaviour changes.
8. Update `ROADMAP.md` when implementation status changes.
9. Keep `README.md` aligned with functionality that actually exists.

The goal is not simply to produce a functioning API.

The repository should demonstrate deliberate domain modelling, type-driven design, TDD, clear architectural boundaries, incremental delivery, and professional Git discipline.
