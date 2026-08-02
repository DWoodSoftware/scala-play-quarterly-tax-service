# Roadmap

This document is the technical implementation plan for the Quarterly Tax Service.

It translates the behaviour defined in [APPFLOW.md](./APPFLOW.md) into an incremental delivery sequence and references [TYPES.md](./TYPES.md) as the canonical source for domain contracts.

Development should follow TDD throughout:

```text
Define behaviour
      ↓
Define / update contract
      ↓
Write failing test
      ↓
Implement minimum behaviour
      ↓
Refactor
      ↓
Update documentation
      ↓
Commit coherent change
```

---

## Status Legend

* [x] Complete
* [ ] Planned
* [~] In Progress
* [!] Blocked
* [-] Deferred

---

# Phase 0 — Project Bootstrap

**Status:** [x] Complete

Purpose:

Establish a minimal, testable Scala 3 + Play application before introducing domain behaviour.

Completed:

* [x] Initialise Scala 3 + Play Framework project
* [x] Configure sbt
* [x] Configure ScalaTest / Play testing support
* [x] Configure scalafmt
* [x] Add `.gitignore`
* [x] Implement `GET /health`
* [x] Add automated health endpoint test
* [x] Establish README documentation
* [x] Establish APPFLOW documentation
* [x] Establish TYPES documentation
* [x] Establish ROADMAP documentation

Relevant flow:

* `APPFLOW.md` → Health Flow

Relevant contracts:

* `HealthResponse` in `TYPES.md`

---

# Phase 1 — Core Domain Model

**Status:** [x] Complete

Purpose:

Establish the strongly typed domain model required by the quarterly update workflow before implementing application services or HTTP endpoints.

## 1.1 Quarter

Behaviour:

Only valid quarterly reporting periods should be representable.

Tasks:

* [x] Add failing tests for valid quarter values
* [x] Implement `Quarter`
* [x] Confirm invalid arbitrary integer quarter values cannot enter the domain
* [x] Refactor dependent test fixtures where required

Contract:

* `Quarter` in `TYPES.md`

---

## 1.2 Tax Year

Behaviour:

A tax year must represent two consecutive years.

Tasks:

* [x] Define expected valid and invalid tax-year behaviour
* [x] Add failing tests
* [x] Implement `TaxYear`
* [x] Prevent invalid year ranges where practical
* [x] Refactor construction API if tests reveal weak invariants

Contract:

* `TaxYear` in `TYPES.md`

---

## 1.3 Financial Categories

Tasks:

* [x] Implement `IncomeCategory`
* [x] Implement `ExpenseCategory`
* [x] Add focused type-level tests where meaningful

Contracts:

* `IncomeCategory`
* `ExpenseCategory`

---

## 1.4 Financial Entries

Behaviour:

Financial entries must carry a supported category and monetary amount.

Tasks:

* [x] Define valid income-entry behaviour
* [x] Define valid expense-entry behaviour
* [x] Add failing tests for invalid monetary values
* [x] Implement `IncomeEntry`
* [x] Implement `ExpenseEntry`
* [x] Decide whether non-negative monetary invariants belong in construction or validation

Contracts:

* `IncomeEntry`
* `ExpenseEntry`

Relevant flow:

* `APPFLOW.md` → Financial Calculation Flow
* `APPFLOW.md` → Validation Flow

---

## 1.5 Submission State

Behaviour:

Only recognised quarterly update states should be representable.

Tasks:

* [x] Implement `SubmissionStatus`
* [x] Define legal lifecycle transitions in tests
* [x] Define illegal lifecycle transitions in tests
* [x] Keep transition behaviour outside HTTP concerns

Contract:

* `SubmissionStatus`

Relevant flow:

* `APPFLOW.md` → Quarterly Update Lifecycle
* `APPFLOW.md` → State Transition Rules

---

## 1.6 Add TaxpayerReference

Behaviour:

Only structurally valid taxpayer references should be representable within the trusted domain. Validation confirms format plausibility only and does not verify the reference against any external tax authority.

Tasks:

* [x] Implement `TaxpayerReference`
* [x] Define valid taxpayer reference format behaviour
* [x] Define invalid taxpayer reference format behaviour
* [x] Add failing tests for taxpayer reference invariants
* [x] Protect construction so invalid references cannot enter the trusted domain
* [x] Refactor `QuarterlyUpdateInput` to use `TaxpayerReference` instead of `String`
* [x] Update affected `QuarterlyUpdateInput` tests

Contract:

* `TaxpayerReference`
* `QuarterlyUpdateInput`

Relevant flow:

* `APPFLOW.md` → Creating a Quarterly Update
* `APPFLOW.md` → Validation Flow

Notes:

* Taxpayer reference validation is structural only.
* The service must not imply that a reference has been verified against HMRC or any external system.
* Invalid raw references should be rejected before construction of `QuarterlyUpdateInput`.
---

# Phase 2 — Input and Domain Validation

**Status:** [ ] Planned

Purpose:

Define the application input contract and explicit domain validation outcomes.

## 2.1 QuarterlyUpdateInput

Tasks:

* [x] Implement `QuarterlyUpdateInput`
* [x] Ensure client input excludes generated fields
* [x] Keep derived totals, IDs, lifecycle state, and submission timestamps server-controlled

Contract:

* `QuarterlyUpdateInput`

Relevant flow:

* `APPFLOW.md` → Creating a Quarterly Update

---

## 2.2 ValidationError

Tasks:

* [x] Implement `ValidationError`
* [x] Define expected failure cases
* [x] Keep validation errors independent from HTTP status codes

Initial validation cases:

* [x] Missing income

Contract:

* `ValidationError`

---

## 2.3 Domain Validation

Behaviour:

Structurally valid input may still fail business validation.

Tasks:

* [x] Add failing validation tests first
* [x] Implement pure validation functions
* [x] Return explicit validation outcomes
* [x] Use `Either[List[ValidationError], QuarterlyUpdateInput]`
* [x] Avoid exceptions for expected validation failures
* [x] Keep validation side-effect free

Contract:

* `ValidationResult`

Relevant flow:

* `APPFLOW.md` → Validation Flow

---

# Phase 3 — Financial Calculation

**Status:** [ ] Planned

Purpose:

Implement deterministic financial aggregation independently from HTTP and persistence concerns.

Behaviour:

The server calculates all derived financial values from client-supplied entries.

Tasks:

* [x] Add failing tests for total income
* [x] Implement total income calculation
* [x] Add failing tests for total expenses
* [x] Implement total expense calculation
* [x] Add failing tests for net amount
* [x] Implement `netAmount = totalIncome - totalExpenses`
* [x] Verify derived values cannot be supplied authoritatively by the client
* [x] Refactor calculation logic into small pure functions

Relevant contracts:

* `IncomeEntry`
* `ExpenseEntry`
* `QuarterlyUpdateInput`
* `QuarterlyUpdate`

Relevant flow:

* `APPFLOW.md` → Financial Calculation Flow

---

# Phase 4 — Quarterly Update Entity

**Status:** [ ] Planned

Purpose:

Create the persisted domain representation produced from validated input and server-derived values.

Tasks:

* [x] Add tests defining creation behaviour
* [x] Implement `QuarterlyUpdate`
* [x] Generate server-controlled identifier
* [x] Populate derived financial totals
* [x] Set initial state to `Draft`
* [x] Ensure `submittedAt` is empty for non-submitted entities
* [x] Test entity invariants

Contract:

* `QuarterlyUpdate`

Relevant flow:

* `APPFLOW.md` → Creating a Quarterly Update
* `APPFLOW.md` → Quarterly Update Lifecycle

---

# Phase 5 — Repository Contract

**Status:** [ ] Planned

Purpose:

Introduce persistence as an asynchronous abstraction without coupling application behaviour to a specific storage technology.

## 5.1 Repository Interface

Tasks:

* [x] Implement `QuarterlyUpdateRepository`
* [x] Define asynchronous save contract
* [x] Define asynchronous lookup contract
* [x] Represent missing entities using `Option`
* [x] Keep repository free of HTTP and domain workflow logic

Contract:

* `QuarterlyUpdateRepository`

---

## 5.2 In-Memory Repository

Tasks:

* [x] Add repository contract tests
* [x] Implement `InMemoryQuarterlyUpdateRepository`
* [x] Verify save behaviour
* [x] Verify lookup behaviour
* [x] Verify missing lookup returns `None`
* [x] Keep storage implementation hidden behind repository abstraction

Relevant flow:

* `APPFLOW.md` → Repository Flow
* `APPFLOW.md` → Asynchronous Boundaries

---

# Phase 6 — Application Error Model

**Status:** [ ] Planned

Purpose:

Represent expected application failures explicitly before building service orchestration.

Tasks:

* [x] Implement `DomainError`
* [x] Add `ValidationFailed`
* [x] Add `UpdateNotFound`
* [x] Add `InvalidStateTransition`
* [x] Verify application failures remain independent of HTTP concerns

Contract:

* `DomainError`

Relevant flow:

* `APPFLOW.md` → Error Flow

---

# Phase 7 — Application Service

**Status:** [ ] Planned

Purpose:

Implement use-case orchestration over domain behaviour and repository contracts.

## 7.1 Create

Behaviour:

A structurally valid and domain-valid request becomes a persisted draft quarterly update.

Tasks:

* [ ] Add failing service test
* [ ] Validate input
* [ ] Calculate derived totals
* [ ] Construct draft entity
* [ ] Persist entity
* [ ] Return explicit `Either` outcome
* [ ] Keep service independent from Play HTTP types

Contract:

* `QuarterlyUpdateService.create`

Relevant flow:

* `APPFLOW.md` → Creating a Quarterly Update

---

## 7.2 Find By ID

Behaviour:

Existing updates are returned; missing updates become explicit application errors.

Tasks:

* [ ] Add failing lookup tests
* [ ] Implement repository lookup orchestration
* [ ] Translate `None` into `UpdateNotFound`

Contract:

* `QuarterlyUpdateService.findById`

Relevant flow:

* `APPFLOW.md` → Retrieving a Quarterly Update

---

## 7.3 Submit

Behaviour:

Only valid drafts may transition through validation into the submitted state.

Tasks:

* [ ] Add failing tests for successful submission
* [ ] Add failing tests for missing update
* [ ] Add failing tests for invalid submission
* [ ] Add failing tests for duplicate submission
* [ ] Validate current state
* [ ] Perform `Draft → Validated`
* [ ] Perform `Validated → Submitted`
* [ ] Set `submittedAt`
* [ ] Persist updated entity
* [ ] Return explicit application outcome

Contract:

* `QuarterlyUpdateService.submit`

Relevant flow:

* `APPFLOW.md` → Submission Flow
* `APPFLOW.md` → State Transition Rules

---

# Phase 8 — HTTP Transport Layer

**Status:** [ ] Planned

Purpose:

Expose implemented application behaviour through Play without leaking HTTP concerns into the service or domain layers.

## 8.1 Create Endpoint

Endpoint:

```text
POST /api/v1/quarterly-updates
```

Tasks:

* [ ] Define request JSON contract
* [ ] Add controller test for valid request
* [ ] Add controller test for malformed JSON
* [ ] Add controller test for domain validation failure
* [ ] Implement route
* [ ] Implement thin controller action
* [ ] Map successful creation to `201 Created`

Relevant flow:

* `APPFLOW.md` → Creating a Quarterly Update

---

## 8.2 Retrieve Endpoint

Endpoint:

```text
GET /api/v1/quarterly-updates/:id
```

Tasks:

* [ ] Add successful retrieval test
* [ ] Add missing-resource test
* [ ] Implement route
* [ ] Implement controller action
* [ ] Map success to `200 OK`
* [ ] Map missing entity to `404 Not Found`

Relevant flow:

* `APPFLOW.md` → Retrieving a Quarterly Update

---

## 8.3 Submit Endpoint

Endpoint:

```text
POST /api/v1/quarterly-updates/:id/submit
```

Tasks:

* [ ] Add successful submission test
* [ ] Add missing-resource test
* [ ] Add validation-failure test
* [ ] Add illegal-transition test
* [ ] Implement route
* [ ] Implement thin controller action

Relevant flow:

* `APPFLOW.md` → Submission Flow

---

# Phase 9 — HTTP Error Translation

**Status:** [ ] Planned

Purpose:

Translate explicit application outcomes into consistent HTTP responses.

Required mappings:

```text
Malformed HTTP / JSON           → 400 Bad Request
Domain validation failure       → 422 Unprocessable Entity
Missing quarterly update        → 404 Not Found
Illegal state transition        → 409 Conflict
Unexpected infrastructure error → 500 Internal Server Error
```

Tasks:

* [ ] Define JSON error response format
* [ ] Add controller tests for each mapping
* [ ] Centralise mapping where appropriate
* [ ] Prevent domain types from depending on Play HTTP types

Relevant flow:

* `APPFLOW.md` → Error Flow

Relevant contracts:

* `ValidationError`
* `DomainError`

---

# Phase 10 — Dependency Injection and Composition

**Status:** [ ] Planned

Purpose:

Compose controllers, services, validators, and repositories through explicit dependencies.

Tasks:

* [ ] Configure constructor dependency injection
* [ ] Bind repository abstraction to in-memory implementation
* [ ] Verify service depends on repository trait
* [ ] Verify controller depends on service abstraction where appropriate
* [ ] Remove accidental global state
* [ ] Ensure components remain independently testable

Relevant flow:

* `APPFLOW.md` → Architectural Boundaries

---

# Phase 11 — Integration and Behavioural Coverage

**Status:** [ ] Planned

Purpose:

Verify the complete application flow across Play routing, service behaviour, domain logic, and persistence abstraction.

Tasks:

* [ ] Test create → retrieve lifecycle
* [ ] Test create → submit lifecycle
* [ ] Test duplicate submission behaviour
* [ ] Test validation failures do not persist invalid entities
* [ ] Test derived financial values are calculated server-side
* [ ] Test expected HTTP status mappings
* [ ] Review test duplication and refactor fixtures/builders

Relevant flow:

* Full `APPFLOW.md`

---

# Phase 12 — CI

**Status:** [ ] Planned

Purpose:

Ensure every change is automatically verified.

Tasks:

* [ ] Add GitHub Actions workflow
* [ ] Run formatting checks
* [ ] Run compilation
* [ ] Run complete test suite
* [ ] Fail CI on any verification failure
* [ ] Add CI status badge to README when stable

Target checks:

```text
sbt scalafmtCheckAll
sbt compile
sbt test
```

---

# Phase 13 — Containerisation

**Status:** [ ] Planned

Purpose:

Produce a reproducible runnable application artifact.

Tasks:

* [ ] Add Dockerfile
* [ ] Prefer multi-stage build
* [ ] Verify container build
* [ ] Verify `GET /health` inside container
* [ ] Document local container execution

---

# Phase 14 — Deployment

**Status:** [ ] Planned

Purpose:

Expose the technical demonstration through a publicly reachable deployment.

Tasks:

* [ ] Select zero-cost hosting target
* [ ] Deploy containerised Play service
* [ ] Configure runtime environment
* [ ] Verify health endpoint
* [ ] Verify core quarterly update workflow
* [ ] Add public demo endpoint to README

---

# Deferred / Explicitly Out of Scope

The initial project deliberately excludes:

* real HMRC integration
* real taxpayer data
* authentication
* authorisation
* PostgreSQL
* Redis
* Kafka / Pub/Sub
* Kubernetes
* frontend development
* actual UK tax-liability calculations
* production financial processing

These may only be introduced if they materially improve the project's technical value.

---

# Documentation Discipline

After each completed behaviour:

* update `TYPES.md` if a contract changed,
* update `APPFLOW.md` if behaviour changed,
* update this roadmap to reflect implementation status,
* update `README.md` only when user-visible capability changes.

Documentation should evolve with the implementation rather than being reconstructed at the end.

---

# Git Discipline

Commits should represent meaningful engineering steps.

Typical TDD progression:

```text
test: define negative expense validation
feat: reject negative expense values
refactor: simplify financial validation
```

or:

```text
test: define missing quarterly update behaviour
feat: implement quarterly update lookup
```

Avoid:

* large unrelated commits,
* artificial commit splitting,
* generated-code dumps,
* documentation claiming behaviour that is not implemented.

The Git history should make the application's technical evolution understandable without requiring the reviewer to reconstruct it from the final codebase.
