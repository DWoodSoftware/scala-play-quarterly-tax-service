package domain

enum DomainError:
  case ValidationFailed(
    errors: List[ValidationError]
  )

  case UpdateNotFound(
    id: String
  )

  case InvalidStateTransition(
    current: SubmissionStatus,
    requested: SubmissionStatus
  )
