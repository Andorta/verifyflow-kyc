# Changelog

All notable changes to VerifyFlow KYC are documented here.

## [1.0.0] - 2026-08-31

### Added

- Applicant creation and retrieval APIs
- Verification-case creation, status retrieval, and applicant history
- Controlled verification lifecycle with validated state transitions
- Verification-provider abstraction with a simulated provider
- HMAC-SHA256 authenticated provider webhooks
- Transactional and idempotent webhook processing
- Atomic duplicate-event protection with PostgreSQL
- Structured API error responses
- PostgreSQL persistence with Flyway migrations
- Optimistic locking for concurrent updates
- Interactive OpenAPI and Swagger documentation
- Multi-stage, non-root Docker image
- Complete Docker Compose application stack
- GitHub Actions testing against PostgreSQL
- Automated Docker-image verification
- 46 automated tests

### Security and reliability

- Constant-time webhook-signature comparison
- Payload hashing instead of raw webhook retention
- Database constraints for event uniqueness and workflow integrity
- Transactional webhook registration and state updates
- Non-root container runtime