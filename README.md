# Order Processing: Products and Orders Microservice

This microservice is part of the [Order Processing](https://github.com/petru-acsinte-dev) 
portfolio project. It was extracted from the 
[monolith](https://github.com/petru-acsinte-dev/order-processing-monolith).

It manages the product catalogue and the full order lifecycle — from creation 
through confirmation. On confirmation, it notifies the shipments service to 
initiate fulfillment.

**REST API** — explore and test via Swagger UI:
- Local: `http://localhost:8081/orders/swagger-ui/index.html`
- Live: `http://52.205.87.85/orders/swagger-ui/index.html`

### Responsibilities
- Product catalogue management (admin only)
- Order creation, status transitions, and confirmation
- Publishes `OrderConfirmedEvent` to RabbitMQ on order confirmation
- Consumes `OrderShippedEvent` from RabbitMQ to update order status

### Key Technologies
- Spring Boot 3.5 · Spring Security · PostgreSQL · Flyway · MapStruct · RabbitMQ · Testcontainers

### Related
- [order-processing-common](https://github.com/petru-acsinte-dev/order-processing-common) — shared library
- [order-processing-users](https://github.com/petru-acsinte-dev/order-processing-users)
- [order-processing-shipments](https://github.com/petru-acsinte-dev/order-processing-shipments)
- [User Story](https://github.com/petru-acsinte-dev/order-processing-monolith/blob/master/OrdersProcessor/docs/UserStory.md)
- [Design Document](https://github.com/petru-acsinte-dev/order-processing-monolith/blob/master/OrdersProcessor/docs/DesignDoc.md)
- [Development journal — monolith phase](https://github.com/petru-acsinte-dev/order-processing-monolith/blob/master/OrdersProcessor/docs/journal/daily-journal.md)
- [Development journal](docs/journal/daily-journal.md)
