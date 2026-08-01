package domain

enum SubmissionStatus:
  case Draft
  case Validated
  case Submitted

  def canTransitionTo(next: SubmissionStatus): Boolean = 
    (this, next) match
        case (Draft, Validated) => true
        case (Validated, Submitted) => true
        case _ => false