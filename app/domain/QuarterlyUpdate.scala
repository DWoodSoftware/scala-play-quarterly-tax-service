package domain

import java.time.Instant
import java.util.UUID

final case class QuarterlyUpdate private (
  id: String,
  taxpayerReference: TaxpayerReference,
  taxYear: TaxYear,
  quarter: Quarter,
  income: List[IncomeEntry],
  expenses: List[ExpenseEntry],
  totalIncome: BigDecimal,
  totalExpenses: BigDecimal,
  netAmount: BigDecimal,
  status: SubmissionStatus,
  submittedAt: Option[Instant]
) {
  def transitionTo(
    next: SubmissionStatus
  ): Either[DomainError, QuarterlyUpdate] =
    if status.canTransitionTo(next) then Right(copy(status = next))
    else
      Left(
        DomainError.InvalidStateTransition(
          current = status,
          requested = next
        )
      )

  def markSubmitted(
    submittedAt: Instant
  ): Either[DomainError, QuarterlyUpdate] =
    if status == SubmissionStatus.Validated then
      Right(
        copy(
          status = SubmissionStatus.Submitted,
          submittedAt = Some(submittedAt)
        )
      )
    else
      Left(
        DomainError.InvalidStateTransition(
          current = status,
          requested = SubmissionStatus.Submitted
        )
      )
}

object QuarterlyUpdate {

  def create(input: QuarterlyUpdateInput): QuarterlyUpdate = {
    val totalIncome =
      FinancialCalculator.totalIncome(input.income)

    val totalExpenses =
      FinancialCalculator.totalExpenses(input.expenses)

    val netAmount =
      FinancialCalculator.netAmount(
        totalIncome,
        totalExpenses
      )

    QuarterlyUpdate(
      id = UUID.randomUUID().toString,
      taxpayerReference = input.taxpayerReference,
      taxYear = input.taxYear,
      quarter = input.quarter,
      income = input.income,
      expenses = input.expenses,
      totalIncome = totalIncome,
      totalExpenses = totalExpenses,
      netAmount = netAmount,
      status = SubmissionStatus.Draft,
      submittedAt = None
    )
  }
}
