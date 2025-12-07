# User Service

User profile and management service for JobApplicant platform.

## Quick Start

### Prerequisites
- Java 21
- Maven
- PostgreSQL (Supabase)

### Run the Service

```bash
# From project root
./mvnw spring-boot:run -pl services/user-service

# Or from user-service directory
cd services/user-service
../../mvnw spring-boot:run
```

### Access Points

| URL | Description |
|-----|-------------|
| http://localhost:8085 | Service base URL |
| http://localhost:8085/swagger-ui.html | Swagger UI |
| http://localhost:8085/v3/api-docs | OpenAPI spec |
| http://localhost:8085/actuator/health | Health check |

## API Endpoints

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users` | Get all users |
| GET | `/api/v1/users/{id}` | Get user by ID |
| GET | `/api/v1/users/email/{email}` | Get user by email |
| GET | `/api/v1/users/health` | Health check |

### Countries
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/countries` | Get all countries |

## Project Structure

```
user-service/
└── src/main/java/com/team/ja/user/
    ├── api/                    # REST Controllers
    │   ├── UserController.java
    │   └── CountryController.java
    ├── dto/
    │   └── response/           # Response DTOs
    ├── mapper/                 # MapStruct mappers
    ├── model/                  # JPA Entities
    ├── repository/             # Spring Data repositories
    ├── service/                # Service interfaces
    │   └── impl/               # Service implementations
    └── UserServiceApplication.java
```

## Database Schema

Uses `user_schema` in PostgreSQL with tables:
- `users` - User profiles
- `countries` - Country reference data
- `skills` - Skill reference data
- `user_education` - User education history
- `user_skills` - User-skill associations
- `user_work_experience` - User work history

## Configuration

Key properties in `application.properties`:
- `server.port=8085`
- `spring.datasource.url` - PostgreSQL connection
- `spring.jpa.properties.hibernate.default_schema=user_schema`

## Security

**Note:** Spring Security is not yet implemented. All endpoints are currently open.
Security will be added later via auth-service integration.

## Dependencies

- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL Driver
- MapStruct
- Lombok
- SpringDoc OpenAPI (Swagger)
- Common module (shared DTOs, exceptions, etc.)

## Next Steps

- [ ] Add request DTOs for create/update operations
- [ ] Implement create, update, delete endpoints
- [ ] Add Spring Security integration
- [ ] Add pagination support
- [ ] Add search/filter functionality

