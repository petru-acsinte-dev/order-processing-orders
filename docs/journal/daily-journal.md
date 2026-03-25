OrderProcessor - Orders microservice - Daily journal
=

2026-03-25
-
- created new GitHub repository for orders
- created Spring Boot project stub for orders with dependencies matching the monolith, plus the new common library 
- ported user classes from the monolith project: dao, entities, repos, service, controllers, configurations, mappers, props, exceptions, constants
- reworked the Flyway scripts into a fresh, clean products and orders initialization script
- moved from using users repository to obtaining the customer external id from JWT
- ported and adapted unit and integration tests from monolith
- housekeeping custom properties
