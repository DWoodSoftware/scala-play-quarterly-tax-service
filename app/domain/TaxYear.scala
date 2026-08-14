package domain

final case class TaxYear private (
  startYear: Int,
  endYear: Int
)

object TaxYear {
  def create(
    startYear: Int,
    endYear: Int
  ): Either[String, TaxYear] =
    if endYear == startYear + 1 then Right(TaxYear(startYear, endYear))
    else Left("Tax year must span two consecutive years")
}
