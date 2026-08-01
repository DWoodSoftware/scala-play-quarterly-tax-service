# Types and Contracts

This document is the canonical reference for the application's important Scala domain types and public contracts.

Detailed behaviour belongs in [APPFLOW.md](./APPFLOW.md).
Implementation progress belongs in [ROADMAP.md](./ROADMAP.md).

## Status

* **Implemented** — exists in the current codebase.
* **Planned** — designed but not yet implemented.
* **Deprecated** — retained only for historical reference.

---

# Implemented

## HealthResponse

**Status:** Implemented

```scala
final case class HealthResponse(
  status: String
)
```

Represents the JSON payload returned by `GET /health`.

Current invariant:

```text
status == "UP"
```

---

# Domain Types

## Quarter

**Status:** Planned

```scala
enum Quarter:
  case Q1, Q2, Q3, Q4
```

Represents a valid quarterly reporting period.

Using an enum prevents invalid quarter values from entering the domain.

---

## TaxYear

**Status:** Planned

```scala
final case class TaxYear(
  startYear: Int,
  endYear: Int
)
```

Represents a reporting tax year.

Invariant:

```text
endYear == startYear + 1
```

---

## IncomeCategory

**Status:** Planned

```scala
enum IncomeCategory {
    case SelfEmployment 
    case Property 
    case Investment 
    case Pension 
    case StateBenefits 
    case Dividends 
    case Other
}
```

Defines supported income classifications.

---

## ExpenseCategory

**Status:** Planned

```scala
enum ExpenseCategory {
    case Travel 
    case OfficeCosts 
    case ProfessionalFees 
    case Advertising 
    case Equipment 
    case Other
}
```

Defines supported expense classifications.

---

## IncomeEntry

**Status:** Planned

```scala
final case class IncomeEntry(
  category: IncomeCategory,
  amount: BigDecimal
)
```

Invariant:

```text
amount >= 0
```

---

## ExpenseEntry

**Status:** Planned

```scala
final case class ExpenseEntry(
  category: ExpenseCategory,
  amount: BigDecimal
)
```

Invariant:

```text
amount >= 0
```

---

## SubmissionStatus

**Status:** Planned

```scala
enum SubmissionStatus:
  case Draft
  case Validated
  case Submitted
```

Valid lifecycle:

```text
Draft → Validated → Submitted
```

`Submitted` is terminal in the initial implementation.

---

## QuarterlyUpdateInput

**Status:** Planned

```scala
final case class QuarterlyUpdateInput(
  taxpayerReference: String,
  taxYear: TaxYear,
  quarter: Quarter,
  income: List[IncomeEntry],
  expenses: List[ExpenseEntry]
)
```

Represents client-supplied quarterly financial data before persistence.

Derived totals are intentionally excluded from the input contract.

---

## QuarterlyUpdate

**Status:** Planned

```scala
final case class QuarterlyUpdate(
  id: String,
  taxpayerReference: String,
  taxYear: TaxYear,
  quarter: Quarter,
  income: List[IncomeEntry],
  expenses: List[ExpenseEntry],
  totalIncome: BigDecimal,
  totalExpenses: BigDecimal,
  netAmount: BigDecimal,
  status: SubmissionStatus,
  submittedAt: Option[Instant]
)
```

Represents the persisted domain entity.

Key invariants:

```text
totalIncome   == sum(income.amount)
totalExpenses == sum(expenses.amount)
netAmount     == totalIncome - totalExpenses
```

And:

```text
status == Submitted  ⇒ submittedAt.isDefined
status != Submitted  ⇒ submittedAt.isEmpty
```

---

# Validation Types

## ValidationError

**Status:** Planned

```scala
enum ValidationError:
  case MissingIncome
  case NegativeIncome
  case NegativeExpense
  case InvalidTaxYear
```

Represents expected domain validation failures.

Validation errors are domain outcomes, not exceptions.

---

## ValidationResult

**Status:** Planned

Conceptual contract:

```scala
Either[List[ValidationError], QuarterlyUpdateInput]
```

Meaning:

```text
Left(errors)  = validation failed
Right(input)  = validation succeeded
```

---

# Application Errors

## DomainError

**Status:** Planned

```scala
enum DomainError:
  case ValidationFailed(errors: List[ValidationError])
  case UpdateNotFound(id: String)
  case InvalidStateTransition(
    current: SubmissionStatus,
    requested: SubmissionStatus
  )
```

Represents expected failures from application operations.

HTTP mapping belongs outside the domain layer.

---

# Repository Contract

## QuarterlyUpdateRepository

**Status:** Planned

```scala
trait QuarterlyUpdateRepository:

  def save(
    update: QuarterlyUpdate
  ): Future[QuarterlyUpdate]

  def findById(
    id: String
  ): Future[Option[QuarterlyUpdate]]
```

Semantics:

```text
Future = asynchronous persistence operation
Option = entity may not exist
```

The initial implementation will be in-memory.

---

# Service Contract

## QuarterlyUpdateService

**Status:** Planned

```scala
trait QuarterlyUpdateService:

  def create(
    input: QuarterlyUpdateInput
  ): Future[Either[DomainError, QuarterlyUpdate]]

  def findById(
    id: String
  ): Future[Either[DomainError, QuarterlyUpdate]]

  def submit(
    id: String
  ): Future[Either[DomainError, QuarterlyUpdate]]
```

The service coordinates domain behaviour and repository access.

It must not depend on Play HTTP types.

---

# Wrapper Semantics

Use these consistently:

```text
Option[T]
```

A value may legitimately be absent.

```text
Either[E, T]
```

An operation may return an expected failure.

```text
Future[T]
```

The result depends on asynchronous I/O.

These wrapper types should communicate behaviour explicitly through function signatures rather than relying on hidden `null` values or normal-control-flow exceptions.
