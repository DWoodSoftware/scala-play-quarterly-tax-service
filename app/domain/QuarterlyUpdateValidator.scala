
package domain

object QuarterlyUpdateValidator {

    def validate(
        input: QuarterlyUpdateInput
    ): Either[List[ValidationError], QuarterlyUpdateInput] =
        if input.income.isEmpty then
            Left(List(ValidationError.MissingIncome))
        else
            Right(input)
}