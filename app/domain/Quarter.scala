package domain

enum Quarter:
  case Q1, Q2, Q3, Q4

object Quarter:
  val all: List[Quarter] = List(Q1, Q2, Q3, Q4)
