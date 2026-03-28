OrderProcessor - Orders microservice - Daily journal
=

2026-03-25
-
- created new GitHub repository for orders
- created Spring Boot project stub for orders with dependencies matching the monolith, plus the new common library
- ported product and order classes from the monolith project: dao, entities, repos, service, controllers, configurations, mappers, props, exceptions, constants
- reworked the Flyway scripts into a fresh, clean products and orders initialization script
- moved from using users repository to obtaining the customer external id from JWT
- ported and adapted unit and integration tests from monolith
- housekeeping custom properties

2026-03-26
-
- added fulfillments(i.e. shipments service) Feign client to mark orders as confirmed and generate fulfillments
- enabled actuator (more endpoints), logback
- revisited serviceability

2026-03-27
-
- serviceability revisited
- updated readme with more details about this orders service

2026-03-28
-
- Introduced RabbitMQ for orders-shipments communication
- Adopted common 0.4.0 with enhanced RabbitMQ events
