# Service Communication Architecture - Current State

**Date:** December 16, 2025  
**Status:** Scanned and Documented  
**Project:** JobApplicant Backend Microservices

---

## 📋 Table of Contents
- [System Overview](#system-overview)
- [Service Architecture](#service-architecture)
- [Communication Methods](#communication-methods)
- [Current Implementation Status](#current-implementation-status)
- [Configuration Details](#configuration-details)
- [Application Service Integration Plan](#application-service-integration-plan)

---

## 🎯 System Overview

This document captures the current state of inter-service communication in the JobApplicant microservices platform. The system uses a combination of **Service Discovery (Eureka)**, **Event Streaming (Kafka)**, **API Gateway**, and **Feign Clients** for service-to-service communication.

### Infrastructure Stack
- **Service Discovery:** Netflix Eureka (Port 8761)
- **Message Broker:** Apache Kafka (Port 9092)
- **API Gateway:** Spring Cloud Gateway (Port 8080)
- **Caching:** Redis
- **Databases:** PostgreSQL (separate databases per service)
- **Monitoring:** Kafka UI (Port 8090)

---

## 🏗️ Service Architecture

### Service Ports & Roles

| Service | Port | Role | Status |
|---------|------|------|--------|
| **API Gateway** | 8080 | Request routing & load balancing | ✅ Configured |
| **Auth Service** | 8081 | User authentication, JWT tokens | ✅ Active |
| **Application Service** | 8082 | Job applications management | 🚀 In Development |
| **Notification Service** | 8084 | Email/push notifications | ✅ Configured |
| **User Service** | 8085 | User profiles, education, skills | ✅ Active |
| **Subscription Service** | 8086 | Premium subscriptions | ✅ Configured |
| **Admin Service** | 8087 | Admin dashboard & management | ✅ Configured |
| **Eureka Server** | 8761 | Service registry & discovery | ✅ Active |

### Service Topology

```
┌─────────────────────────────────────────────────────────┐
│                    External Clients                      │
│              (Web, Mobile, Third-party APIs)            │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP/REST
                           ↓
                 ┌──────────────────────┐
                 │   API Gateway        │
                 │   (Port 8080)        │
                 └─┬─────┬────┬────┬───┬┘
                   │     │    │    │   │
         ┌─────────┘     │    │    │   └──────────┐
         │               │    │    │              │
    ┌────▼───────┐  ┌────▼──┐ │ ┌─▼──────┐  ┌───▼────┐
    │Auth Service│  │ User  │ │ │Notif   │  │Admin   │
    │  (8081)    │  │Service│ │ │Service │  │Service │
    │            │  │(8085) │ │ │(8084)  │  │(8087)  │
    └────────────┘  └───┬───┘ │ └────────┘  └────────┘
                        │     │
                        │  ┌──▼──────────────┐
                        │  │Application      │
                        │  │Service (8082)   │
                        │  └────────┬────────┘
                        │           │
                        └─────┬─────┘
                              │ Eureka Service Discovery
                              ↓
                    ┌──────────────────────┐
                    │  Eureka Server       │
                    │  (8761)              │
                    └──────────────────────┘
                              ↑
                              │ Event Streaming
                              ↓
                    ┌──────────────────────┐
                    │  Kafka Broker        │
                    │  (9092)              │
                    │  Event Bus           │
                    └──────────────────────┘
```

---

## 📡 Communication Methods

### 1. Eureka Service Discovery

**Purpose:** Service-to-service DNS resolution & registration

**Implementation:**
- All services register with Eureka on startup
- Services can discover other services by name
- Built-in health checks and automatic deregistration
- Load balancing capabilities

**Configuration Pattern (in each service):**
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    prefer-ip-address: false
    hostname: service-name
    leaseRenewalIntervalInSeconds: 10
```

**Services Using Eureka:**
- ✅ Auth Service
- ✅ User Service
- ✅ Application Service (configured)
- ✅ Notification Service
- ✅ Subscription Service
- ✅ Admin Service
- ✅ API Gateway

---

### 2. Kafka Event Streaming (Async Communication)

**Purpose:** Decoupled asynchronous event publishing/consumption

**Architecture:**
```
Producer Service     Event Topic          Consumer Service
    (sends)    ──→  (Kafka Queue)   ───→   (receives)
      │                                        │
      └─ Fire & Forget                        └─ Process in background
      └─ No blocking                           └─ Guaranteed delivery
      └─ Event sourcing                        └─ Retry logic included
```

#### Currently Implemented Topics

**KafkaTopics.java (in common module):**
```java
public final class KafkaTopics {
    public static final String USER_REGISTERED = "user-registered";
    public static final String USER_UPDATED = "user-updated";
    // Additional topics to be defined as services grow
}
```

#### Current Event Flow: User Registration

**Step 1: Auth Service Publishes Event**
```java
// File: services/auth-service/src/main/java/com/team/ja/auth/kafka/UserRegisteredProducer.java

@Service
@RequiredArgsConstructor
public class UserRegisteredProducer {
    
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Publishing user-registered event for userId: {}", event.getUserId());
        
        CompletableFuture<SendResult<String, UserRegisteredEvent>> future = 
            kafkaTemplate.send(
                KafkaTopics.USER_REGISTERED,                    // Topic
                event.getUserId().toString(),                   // Key (partition routing)
                event                                           // Value (event data)
            );
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event sent successfully [partition: {}, offset: {}]",
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send event", ex);
            }
        });
    }
}
```

**Step 2: User Service Consumes Event**
```java
// File: services/user-service/src/main/java/com/team/ja/user/kafka/UserRegisteredConsumer.java

@Service
@RequiredArgsConstructor
public class UserRegisteredConsumer {
    
    private final UserRepository userRepository;

    @KafkaListener(
        topics = KafkaTopics.USER_REGISTERED,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received user-registered event for userId: {}", event.getUserId());

        // Idempotency check
        if (userRepository.existsById(event.getUserId())) {
            log.warn("User already exists. Skipping.");
            return;
        }

        // Create user profile from auth service data
        User user = User.builder()
            .id(event.getUserId())              // Same ID from auth-service
            .email(event.getEmail())
            .firstName(event.getFirstName())
            .lastName(event.getLastName())
            .build();

        userRepository.save(user);
        log.info("User profile created successfully");
    }
}
```

#### Kafka Producer Configuration

**File: services/auth-service/src/main/java/com/team/ja/auth/config/KafkaProducerConfig.java**
```java
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, UserRegisteredEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");           // Wait for all replicas
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);            // Retry failed sends
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

#### Event Model

**File: common/src/main/java/com/team/ja/common/event/UserRegisteredEvent.java**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime registeredAt;
}
```

---

### 3. Feign Clients (Sync Service-to-Service Calls)

**Status:** ❌ Not yet implemented in codebase  
**Purpose:** Synchronous HTTP-based service-to-service communication

**Use Cases:**
- Service A needs immediate response from Service B
- Data validation against another service
- Retrieving detailed information from another service

**Spring Cloud Eureka Integration:**
- Feign can use Eureka service names instead of hardcoded URLs
- Built-in load balancing
- Automatic failover capability

**Implementation Pattern (for future use):**
```java
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable UUID id);
}

@Service
@RequiredArgsConstructor
public class SomeService {
    private final UserServiceClient userServiceClient;
    
    public void processUser(UUID userId) {
        // Call user-service and get immediate response
        ApiResponse<UserResponse> response = userServiceClient.getUserById(userId);
        UserResponse user = response.getData();
        // Process user data
    }
}
```

---

### 4. API Gateway Routing

**Status:** ✅ Configured  
**Port:** 8080 (external entry point)  
**Purpose:** Single entry point for all client requests, request routing

**Routing Logic:**
```
Client Request → API Gateway → Route to appropriate service
                    (8080)        based on path/rules
                                  
Examples:
/api/v1/auth/**       → Auth Service (8081)
/api/v1/users/**      → User Service (8085)
/api/v1/applications/**  → Application Service (8082)
/api/v1/notifications/**  → Notification Service (8084)
```

---

## ✅ Current Implementation Status

### Fully Implemented & Active
- ✅ **Eureka Service Discovery** - All services registered
- ✅ **Kafka Events** - User registration flow working
- ✅ **API Gateway** - Routing configured
- ✅ **Auth Service** - User registration, JWT tokens
- ✅ **User Service** - Profile management

### Configured but Not Yet Active
- 🟡 **Application Service** - Skeleton in place, Eureka configured
- 🟡 **Notification Service** - Ready to consume Kafka events
- 🟡 **Subscription Service** - Database configured

### Not Yet Implemented
- ❌ **Feign Clients** - No service-to-service sync calls yet
- ❌ **Application Events** - Kafka topics for applications not yet defined

---

## 🔧 Configuration Details

### Common Configuration Files

#### application.yml (Base Config)
Located in each service's `src/main/resources/`

**Kafka Configuration:**
```yaml
spring:
  kafka:
    bootstrap-servers: kafka:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      bootstrap-servers: kafka:9092
      group-id: ${spring.application.name}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
```

**Eureka Configuration:**
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    prefer-ip-address: false
    hostname: ${spring.application.name}
    leaseRenewalIntervalInSeconds: 10
    leaseExpirationDurationInSeconds: 30
```

**Database Configuration:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/jobapplicant
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_schema: ${service.schema.name}
```

---

## 🚀 Application Service Integration Plan

### Current Status
- **Phase 0:** Dependencies review completed
- **Architecture:** Aligned with existing patterns
- **Ready for:** Phase 0 implementation (dependencies & configuration)

### Integration Points

#### 1. Kafka Events (Planned)

**Topics to Create:**
```java
// Add to common/src/main/java/com/team/ja/common/event/KafkaTopics.java
public static final String APPLICATION_CREATED = "application-created";
public static final String APPLICATION_STATUS_CHANGED = "application-status-changed";
public static final String APPLICATION_DELETED = "application-deleted";
```

**Event Models to Create:**
- `ApplicationCreatedEvent.java`
- `ApplicationStatusChangedEvent.java`
- `ApplicationDeletedEvent.java`

**Services Publishing Events:**
- Application Service (producer)

**Services Consuming Events:**
- Notification Service (consumer - to send emails/notifications)

#### 2. Feign Clients (Mock Placeholder)

**Clients to Create:**
```java
// services/application-service/src/main/java/com/team/ja/application/client/UserServiceClient.java
@FeignClient(name = "user-service", url = "${services.user-service.url:http://user-service:8085}")
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable UUID id);
}

// services/application-service/src/main/java/com/team/ja/application/client/JobManagerServiceClient.java
@FeignClient(name = "job-manager", url = "${services.job-manager.url:http://job-manager:8088}")
public interface JobManagerServiceClient {
    @GetMapping("/api/v1/job-posts/{id}")
    ApiResponse<JobPostResponse> getJobPostById(@PathVariable UUID id);
}
```

**Configuration (application.yml):**
```yaml
services:
  user-service:
    url: http://user-service:8085
  job-manager:
    url: http://job-manager:8088  # TODO: Update when Job Manager service is ready

# Feign client configuration
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
        loggerLevel: basic
```

#### 3. MinIO Configuration

**Current State:** Configured in User Service  
**Implementation:** Will mirror User Service pattern

```yaml
minio:
  endpoint: http://minio:9000
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket-name: job-applications
  file-max-size: 52428800  # 50MB
  file-allowed-types: 
    - application/pdf
```

---

## 📊 Summary Table

### Communication Method Comparison

| Method | Status | Use Case | Example |
|--------|--------|----------|---------|
| **Eureka** | ✅ Active | Service discovery & registration | Services finding each other by name |
| **Kafka** | ✅ Active | Async events, decoupling | User registration event flow |
| **Feign** | ❌ Not used | Sync service calls | Getting user details (planned for App Service) |
| **REST API** | ✅ Active | Client-service communication | API Gateway → services |
| **API Gateway** | ✅ Active | Request routing | External clients → services |

### Dependency Status

| Dependency | Service | Version | Status |
|-----------|---------|---------|--------|
| spring-cloud-starter-netflix-eureka-client | All | Latest | ✅ |
| spring-kafka | Auth, User, Notif | Latest | ✅ |
| spring-cloud-starter-openfeign | - | Latest | ❌ (Not yet used) |
| springdoc-openapi | User, Auth | 2.7.0 | ✅ |
| mapstruct | User | 1.6.3 | ✅ |

---

## 🎯 Next Steps

### Phase 0: Application Service Setup
- [ ] Update `pom.xml` with missing dependencies (MapStruct, OpenAPI, MinIO, Feign, Kafka)
- [ ] Update `application.yml` with service configuration
- [ ] Create Feign client interfaces (with mock endpoints)
- [ ] Add Kafka event classes to common module
- [ ] Update `ApplicationServiceApplication.java` with `@EnableFeignClients`

### Phase 1-7: Continue with implementation plan
- Follow APPLICATION_SERVICE_PLAN.md phases

---

## 📌 Key Takeaways

1. **Service Discovery (Eureka)** - Enables automatic service-to-service discovery
2. **Event Streaming (Kafka)** - Enables async, decoupled communication
3. **API Gateway** - Single entry point for all clients
4. **Feign Clients** - Ready infrastructure for sync service calls (not yet used)
5. **Application Service** - Will use Kafka for events + Feign for lookups (with mocks initially)

---

## 📚 Related Documents

- [APPLICATION_SERVICE_PLAN.md](APPLICATION_SERVICE_PLAN.md) - Detailed implementation plan
- [TECH_STACK.md](../../TECH_STACK.md) - Technology stack documentation
- [Service.md](Service.md) - Service architecture diagram
- [Database.md](Database.md) - Database schema diagram

---

**Status:** ✅ Complete Scan & Documentation  
**Last Updated:** December 16, 2025  
**Next Review:** Upon starting Phase 0 implementation
