# Bank App

A RESTful banking API built with Spring Boot for authentication, customers, accounts, users, roles, permissions, and transfers.

## Tech Stack

- Java 21 and Maven Wrapper
- Spring Boot 3.5.15, Spring Web, Spring Data JPA, Spring Security
- MySQL, MapStruct, Lombok
- JWT resource server (Nimbus JOSE/JWT) and BCrypt

## Architecture

Layered architecture: `Controller → Service → ServiceImpl → Repository → Entity → DB`

```
com.example.demo
├── controller
├── service
├── service.impl
├── repository
├── entity
├── dto
├── mapper
├── exception
├── specification
├── security
└── config
```

Successful API responses use an `ApiResponse<T>` envelope:

```json
{ "code": 1000, "message": "Thành công", "result": {} }
```

## Features

- JWT issuance and introspection
- Customer and account management with paginated queries
- User, role, and permission management
- Account transfers and paginated transaction history with optional date filters
- Centralized exception handling, validation, JPA Specifications, and MapStruct mapping

## API Endpoints

Only `POST /auth/token` and `POST /auth/introspect` are public. All other endpoints require `Authorization: Bearer <token>`.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/token` | Authenticate and issue a JWT |
| POST | `/auth/introspect` | Introspect a token |
| POST | `/customers` | Create an active customer |
| GET | `/customers` | List customers (`page`, `size`) |
| GET | `/customers/{id}` | Get a customer |
| PUT | `/customers/{id}` | Update a customer |
| DELETE | `/customers/{id}` | Deactivate a customer (idempotent when already inactive) |
| GET | `/customers/search` | Search customers (`page`, `size`, plus search fields); canonical route |
| GET | `/customers/by-field` | Same customer search; compatibility route |
| GET | `/customers/{id}/accounts` | List all accounts for a customer |
| GET | `/customers/{id}/accounts/active` | List active accounts for a customer; canonical route |
| GET | `/customers/{id}/accounts/inactive` | List inactive accounts for a customer |
| POST | `/accounts` | Create a pending account with zero balance |
| GET | `/accounts` | List accounts (`page`, `size`) |
| GET | `/accounts/{id}` | Get an account by ID |
| GET | `/accounts/number/{accountNumber}` | Get an account by number; canonical route |
| GET | `/accounts/by-number/{accountNumber}` | Same account lookup; compatibility route |
| GET | `/accounts/{id}/active` | List active accounts for customer ID `id`; compatibility route |
| PUT | `/accounts/{id}/approve` | Approve a pending account |
| PUT | `/accounts/{id}/reject` | Reject a pending account |
| PUT | `/accounts/{id}/freeze` | Freeze an active account |
| PUT | `/accounts/{id}/unfreeze` | Unfreeze a frozen account |
| PUT | `/accounts/{id}/close` | Close an active zero-balance account |
| POST | `/transactions/transfer` | Transfer funds |
| GET | `/accounts/{accountNumber}/transactions` | Get transaction history (`fromDate`, `toDate`, `page`, `size`) |
| POST | `/users` | Create a user |
| GET | `/users` | List visible users |
| GET | `/users/{id}` | Get a user |
| GET | `/users/me` | Get the authenticated user |
| PATCH | `/users/{id}` | Update a user |
| PATCH | `/users/me/password` | Change the authenticated user's password |
| POST | `/roles` | Create a role |
| GET | `/roles` | List roles |
| GET | `/roles/options` | List role options |
| GET | `/roles/{id}` | Get a role |
| PATCH | `/roles/{id}` | Update a role |
| GET | `/roles/{id}/users` | List users assigned to a role |
| POST | `/roles/{roleId}/permissions` | Add permissions to a role |
| POST | `/roles/{roleId}/permissions/remove` | Remove permissions from a role |
| POST | `/permissions` | Create a permission |
| GET | `/permissions` | List permissions |
| DELETE | `/permissions/{id}` | Delete a permission |

### Customer and account rules

- Pagination uses a zero-based `page` and a `size` from 1 through 100, inclusive. Invalid bounds are rejected.
- Customers use status `0` (`INACTIVE`) or `1` (`ACTIVE`). Creation requires status `1`; an inactive customer cannot be reactivated.
- Customer deletion is a soft deactivation. Deactivation by `DELETE` or update is blocked while any account is `PENDING`, `ACTIVE`, or `FROZEN`; already-inactive deletion succeeds without further changes.
- Accounts use `PENDING`, `ACTIVE`, `FROZEN`, and `INACTIVE`. Creation is allowed only for an active customer, always sets `PENDING`, and always stores a zero balance; a supplied non-zero initial balance is rejected.
- Implemented account transitions are `PENDING → ACTIVE` (approve), `PENDING → INACTIVE` (reject), `ACTIVE → FROZEN` (freeze), `FROZEN → ACTIVE` (unfreeze), and `ACTIVE → INACTIVE` (close). Closing additionally requires a zero balance.

### Banking authority matrix

Authorization is enforced by service-level authorities from the JWT `scope` claim. The currently configured role assignments relevant to customer/account operations are:

| Role | Customer permissions | Account permissions |
|------|----------------------|---------------------|
| Employee | `CUSTOMER_VIEW`, `CUSTOMER_CREATE`, `CUSTOMER_UPDATE` | `ACCOUNT_VIEW`, `ACCOUNT_CREATE` |
| Manager | `CUSTOMER_VIEW`, `CUSTOMER_CREATE`, `CUSTOMER_UPDATE` | Employee account permissions plus `ACCOUNT_APPROVE`, `ACCOUNT_FREEZE`, `ACCOUNT_CLOSE` |
| Admin | None | None |

The services also define `CUSTOMER_DELETE`, `ACCOUNT_REJECT`, and `ACCOUNT_UNFREEZE`; none of these three authorities is currently assigned to Employee, Manager, or Admin. Roles and permissions are mutable through the role-management API, so authorization follows the authorities actually present in the authenticated token.

User, role, permission, and transaction service operations additionally use method-level authority checks where configured.

## Data Model

| Entity | Key Fields |
|--------|------------|
| `User` | id, username, BCrypt password, name, roles |
| `Customer` | id, name, birthday, address, identityNo, mobile, customerType, status |
| `Account` | id, accountNumber, customer, balance, status |
| `CustomerType` | `INDIVIDUAL`, `CORPORATE` |

Hibernate currently manages the schema with `ddl-auto=update`, which is suitable for development rather than production migrations.

## Getting Started

### Prerequisites

- Java 21
- MySQL available locally or through a configured connection

### Configuration

- `application.properties`: existing datasource, JPA, and application-name configuration.
- `application.yml`: common non-secret JWT issuer/expiration, security, and CORS configuration; sets the default profile to `dev`.
- `application-dev.yml`: fixed synthetic local-development signing key only.
- `application-staging.yml`: staging signing-key placeholder requiring `JWT_SIGNER_KEY`.
- `application-prod.yml`: production signing-key placeholder requiring `JWT_SIGNER_KEY`.

The default profile is `dev`; an environment variable or command-line profile overrides it naturally. Production and staging YAML files contain placeholders only—never put a real signing key in them. Store `JWT_SIGNER_KEY` as a mandatory CI/server secret for both environments. Generate a suitable secret with:

```shell
openssl rand -base64 64
```

Create the configured database before starting the application. The current configuration enables SQL logging and automatic schema updates, so review both before production use.

### Running

```powershell
# Explicit local development profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# JVM system property must precede -jar
java -Dspring.profiles.active=dev -jar target/demo-0.0.1-SNAPSHOT.jar

# Verify the full build and test suite
.\mvnw.cmd clean verify
```

In IntelliJ IDEA, set **Active profiles** to `dev`, or add `SPRING_PROFILES_ACTIVE=dev` to the run configuration environment variables.

For production, set `SPRING_PROFILES_ACTIVE=prod` and provide mandatory `JWT_SIGNER_KEY` from the CI/server secret store. For staging, set `SPRING_PROFILES_ACTIVE=staging` and provide mandatory `JWT_SIGNER_KEY` the same way.

The API is available at `http://localhost:8080` unless the server configuration is overridden.

## Security State

- Stateless Spring Security filter chain with CSRF disabled and configured CORS
- JWTs validated by the resource-server filter using HS512
- Only token issuance and introspection are public; all other requests require authentication
- Authorities are read from the JWT `scope` claim without an added prefix
- Method-level authorization protects configured user, role, permission, and transaction operations
- Passwords created or changed through the user service are BCrypt-encoded
- Replace all development credentials, bootstrap credentials, and signing-key defaults before deployment

## Testing

The current suite contains 61 tests across:

- Spring context loading
- Authentication facade behavior
- User, role, permission, and transaction service behavior
- Transaction controller behavior

Run the complete verification on Windows with:

```powershell
.\mvnw.cmd clean verify
```

The suite is primarily unit and controller-slice coverage; database integration and Testcontainers coverage are not present.

## Known Limitations

- Hibernate schema auto-update is used instead of versioned migrations
- Security authority checks are applied only where currently annotated
- Integration coverage remains limited
- Development bootstrap/configuration defaults must not be used in production

## License

This project is for educational/portfolio purposes.
