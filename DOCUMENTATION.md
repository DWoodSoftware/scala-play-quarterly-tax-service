# Documentation relationship rules

This project uses four primary documentation files, each with a clear purpose and a clear relationship to the others.

## README.md

README.md is the public-facing entry point for the repository.

It should:

- explain what the project is
- explain what is implemented today
- summarise the architecture at a high level
- explain how to run the application locally
- explain how to run tests
- link to APPFLOW.md, TYPES.md, and ROADMAP.md

It should not duplicate detailed behaviour or type definitions that belong elsewhere.

## APPFLOW.md

APPFLOW.md is the behavioural and architectural description of how the application works.

It should:

- explain how requests move through the system
- describe the roles of routes, controllers, services, domain logic, validators, and repositories
- explain validation and state transition behaviour where it exists
- distinguish implemented behaviour from planned behaviour

When exact contracts or type shapes are needed, APPFLOW.md should reference TYPES.md rather than repeating the full definition.

## TYPES.md

TYPES.md is the single source of truth for domain contracts and important Scala types.

It should:

- define the important domain models and enums or sealed hierarchies
- define request and response DTOs where relevant
- define domain error types and public service/repository contracts
- describe invariants and wrapper-type semantics such as Option, Either, and Future
- mark each contract as Implemented, Planned, or Deprecated

This file should be the authoritative source for type-level information. Other documents should link to it rather than copying full type definitions.

## ROADMAP.md

ROADMAP.md is the living implementation plan for the project.

It should:

- break development into small, coherent phases
- show completed, in-progress, blocked, and planned work
- reflect the intended TDD and implementation order
- link to APPFLOW.md and TYPES.md where roadmap items depend on behaviour or contracts

It should not duplicate full behavioural specifications or contract definitions.

## General rules

- Avoid duplication. If information has a canonical home, link to it rather than copying it into multiple files.
- Keep the documentation aligned with the codebase as the implementation evolves.
- Treat the documents as living documentation, not static snapshots.
- Prefer concise, high-signal writing over exhaustive repetition.
