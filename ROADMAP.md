# Roadmap

This document is the living implementation plan for the project. It outlines the next phases of work, their intended order, and the current state of progress.

## Status legend

- [x] Complete
- [ ] Planned
- [ ] In Progress
- [ ] Blocked
- [ ] Deferred

## Phase 0: Bootstrap

Status: [x] Complete

Completed work:

- [x] Initialize a Scala 3 + Play Framework project
- [x] Configure sbt and supporting build tooling
- [x] Add ScalaTest support for controller-level testing
- [x] Add formatting configuration with scalafmt
- [x] Add a minimal health endpoint with a TDD-driven test
- [x] Add initial documentation files: README, APPFLOW, and TYPES

## Phase 1: Domain modelling

Status: [ ] Planned

Planned work:

- [ ] Define the core quarterly update domain model
- [ ] Introduce typed quarter and status representations
- [ ] Add domain-level invariants and value objects where appropriate
- [ ] Document the resulting contracts in [TYPES.md](TYPES.md)

Notes:

- This phase should follow a TDD approach where domain behaviour is specified before implementation.

## Phase 2: Validation

Status: [ ] Planned

Planned work:

- [ ] Define validation rules for quarter values and update state transitions
- [ ] Implement validation logic in a dedicated domain or service layer
- [ ] Add tests for invalid and valid states
- [ ] Document the intended validation flow in [APPFLOW.md](APPFLOW.md)

## Phase 3: Core service behaviour

Status: [ ] Planned

Planned work:

- [ ] Implement the service layer for creating and managing quarterly updates
- [ ] Keep HTTP concerns out of the service layer
- [ ] Use small, composable functions and explicit domain outcomes
- [ ] Cover service behaviour with unit tests

## Phase 4: Repository / persistence behaviour

Status: [ ] Planned

Planned work:

- [ ] Introduce a repository abstraction for quarterly updates
- [ ] Implement an initial in-memory repository
- [ ] Keep repository behaviour asynchronous and typed
- [ ] Ensure services depend on the repository abstraction rather than a concrete implementation

## Phase 5: HTTP / API integration

Status: [ ] Planned

Planned work:

- [ ] Add controller actions for creating and retrieving updates
- [ ] Connect routes to the controller layer
- [ ] Translate domain outcomes into HTTP responses
- [ ] Keep controllers thin and focused on transport concerns

## Phase 6: Hardening and error handling

Status: [ ] Planned

Planned work:

- [ ] Add structured error handling for invalid input and business failures
- [ ] Define how errors are translated into meaningful HTTP responses
- [ ] Improve tests around failure behaviour
- [ ] Keep the architecture aligned with [APPFLOW.md](APPFLOW.md)

## Phase 7: CI / CD and deployment

Status: [ ] Planned

Planned work:

- [ ] Add continuous integration checks for formatting, compilation, and tests
- [ ] Add containerisation support with Docker
- [ ] Document a reliable local and CI deployment path

## Notes on scope

The roadmap is intentionally constrained to the bootstrap and early service architecture work. It does not expand into unrelated features such as authentication, frontends, or external integrations unless they become part of the explicit project direction.
