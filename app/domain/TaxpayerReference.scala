package domain

final case class TaxpayerReference private (value: String)

object TaxpayerReference {
  private val Pattern = "^TAX-\\d{8}$".r

  def create(value: String): Either[String, TaxpayerReference] =
    value match {
      case Pattern() => Right(TaxpayerReference(value))
      case _         => Left("Invalid taxpayer reference format. Expected format: TAX-XXXXXXXX where X is a digit.")
    }
}
