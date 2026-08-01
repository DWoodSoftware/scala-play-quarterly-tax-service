# Application flow

This document describes how the application behaves today and how the architecture is intended to evolve. It is the canonical behavioural description for the repository at the current stage of implementation.

## Overview

The project is a small Play Framework backend that exposes a simple health endpoint and is structured to support a larger quarterly reporting service in future iterations. The current implementation is intentionally minimal, but the architecture is already organised around the layered pattern used by the larger system.

## Current implemented behaviour

At present, the application exposes one HTTP endpoint:

- GET /health

When this endpoint is requested, the request enters the Play router, is routed to the health controller, and the controller returns a JSON response with the shape:

```json
{
  "status": "UP"
}
```

This is a simple, synchronous request flow. The controller is responsible for handling the HTTP response and does not contain any business logic beyond returning the health payload.

## Request flow

A request enters the application through the Play server and follows the standard MVC-style path:

1. The router matches the incoming request path.
2. The controller receives the request and prepares the response.
3. The controller returns a Play result to the framework.
4. Play serialises the result and sends the HTTP response back to the client.

At this stage, the flow is intentionally simple and does not yet involve services, validators, repositories, or persistence.

## Layer responsibilities

### Routes

Routes are responsible only for mapping HTTP paths to controller actions. They do not perform business logic or contain domain rules.

### Controllers

Controllers handle HTTP concerns such as request parsing, response generation, and status code selection. They should remain thin and delegate behaviour to the application layer when the behaviour becomes more complex.

### Services

Services coordinate application use cases and orchestrate behaviour. In the current implementation, there is no service layer yet because the health endpoint is trivial. In the broader project direction, services will contain the application workflow and coordinate domain validation and persistence concerns.

### Domain logic

Domain logic represents business rules and deterministic behaviour. In this project, the domain layer is intentionally minimal at the moment. As the application grows, domain objects and pure functions will describe the rules of quarterly reporting without depending on Play or HTTP.

### Validators

Validators enforce business rules before an operation is accepted. In the future, validation will occur before state transitions such as moving from draft to submitted. Validation outcomes should be explicit and should be handled in a structured way rather than embedded inside the controller.

### Repositories

Repositories abstract persistence concerns. At the moment, no repository layer is implemented. The intended design is that services depend on repository abstractions rather than concrete persistence implementations, with an in-memory implementation used initially.

## Boundaries between layers

The architectural boundary is intentionally clear:

- HTTP concerns belong to routes and controllers.
- Business rules belong to the domain layer and services.
- Persistence concerns belong to repositories.
- Controllers should not contain business rules.
- Domain models should not depend on Play HTTP APIs.

## Asynchronous behaviour

The application is currently synchronous for the health endpoint. The architecture is intended to support asynchronous operations through Future-based service and repository interfaces as the product evolves. This is part of the design direction for later stages and is not yet exercised by the implemented behaviour.

## Validation and failure behaviour

There is no business validation flow implemented yet. The health endpoint is always successful when called. In future iterations, validation failures will be handled in a structured manner so that invalid input does not reach persistence or state-changing business logic.

## Planned behaviour

The following behaviour is planned for future implementation and is not part of the current system behaviour:

- Quarterly update creation and retrieval
- Validation of quarter identifiers and submission state
- Transitioning entities from Draft to Submitted
- Repository-backed storage with an in-memory initial implementation
- Structured error translation into HTTP responses

These planned areas should be treated as future work and are intentionally not described as implemented behaviour.

## Notes for contributors

When changing this application, keep the architecture consistent with the layered approach:

- Keep routes focused on routing.
- Keep controllers focused on HTTP.
- Keep business rules in the domain and service layers.
- Keep persistence behind repository abstractions.
- Update this document whenever the actual application behaviour changes.
