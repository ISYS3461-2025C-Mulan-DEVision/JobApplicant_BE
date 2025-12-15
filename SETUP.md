# Job Applicant Backend - Setup Guide

## 📋 Prerequisites

| Tool | Version | Check Command |
|------|---------|---------------|
| **Java** | 21+ | `java -version` |
| **Maven** | 3.9+ | `mvn -version` |
| **Docker** | Latest | `docker --version` |
| **Docker Compose** | Latest | `docker-compose --version` |

---

## 🚀 Quick Start

```bash
# 1. Clone the repository
git clone <repository-url>
cd JobApplicant_BE

# 2. Start infrastructure
docker-compose up -d user-db auth-db kafka redis eureka-server

# 3. Wait ~30 seconds for services to start

# 4. Run your service
cd services/user-service
mvn spring-boot:run

# Or Run all services
docker-compose up -d # Run all infrastructure
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Docker Compose                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  DATABASES (separate containers per service):                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ user-db  │ │ auth-db  │ │  app-db  │ │notif-db  │           │
│  │  :5433   │ │  :5434   │ │  :5435   │ │  :5436   │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│  ┌──────────┐ ┌──────────┐                                      │
│  │ subs-db  │ │admin-db  │                                      │
│  │  :5437   │ │  :5438   │                                      │
│  └──────────┘ └──────────┘                                      │
│                                                                  │
│  INFRASTRUCTURE:                                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                        │
│  │  Kafka   │ │  Redis   │ │  Eureka  │                        │
│  │  :9092   │ │  :6379   │ │  :8761   │                        │
│  └──────────┘ └──────────┘ └──────────┘                        │
│                                                                  │
│  SERVICES (run manually or via docker-compose):                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ Gateway  │ │   Auth   │ │   User   │ │   App    │           │
│  │  :8080   │ │  :8081   │ │  :8085   │ │  :8083   │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Service Ports

### Databases

| Database | Container | Port | Database Name |
|----------|-----------|------|---------------|
| User DB | `ja-user-db` | 5433 | user_db |
| Auth DB | `ja-auth-db` | 5434 | auth_db |
| Application DB | `ja-application-db` | 5435 | application_db |
| Notification DB | `ja-notification-db` | 5436 | notification_db |
| Subscription DB | `ja-subscription-db` | 5437 | subscription_db |
| Admin DB | `ja-admin-db` | 5438 | admin_db |

### Infrastructure

| Service | Port | URL |
|---------|------|-----|
| Eureka Dashboard | 8761 | http://localhost:8761 |
| Kafka | 9092 | localhost:9092 |
| Kafka UI | 8090 | http://localhost:8090 |
| Redis | 6379 | localhost:6379 |
| Redis Commander | 8091 | http://localhost:8091 |

### Application Services

| Service | Port | Swagger UI |
|---------|------|------------|
| API Gateway | 8080 | http://localhost:8080/swagger-ui.html |
| Auth Service | 8081 | http://localhost:8081/swagger-ui.html |
| User Service | 8085 | http://localhost:8085/swagger-ui.html |
| Application Service | 8083 | http://localhost:8083/swagger-ui.html |
| Notification Service | 8086 | http://localhost:8086/swagger-ui.html |
| Subscription Service | 8087 | http://localhost:8087/swagger-ui.html |
| Admin Service | 8088 | http://localhost:8088/swagger-ui.html |

---

## 🔧 Development Workflow

### Option 1: Run Services Manually (Recommended for Development)

```bash
# Start only the infrastructure you need
docker-compose up -d user-db auth-db kafka redis eureka-server

# Run service with Maven (hot reload supported)
cd services/user-service
mvn spring-boot:run
```

### Option 2: Run Everything with Docker Compose

```bash
# Build all services first
mvn clean package -DskipTests

# Start everything
docker-compose up -d

# View logs
docker-compose logs -f user-service
```

---

## 📝 Common Commands

### Docker Compose

```bash
# Start specific services
docker-compose up -d user-db auth-db kafka eureka-server

# Start all infrastructure (no app services)
docker-compose up -d user-db auth-db application-db notification-db subscription-db admin-db kafka redis zookeeper kafka-ui redis-commander eureka-server

# Stop all
docker-compose down

# Stop and remove all data (reset)
docker-compose down -v

# View logs
docker-compose logs -f [service-name]

# Check status
docker-compose ps
```

### Maven

```bash
# Build entire project
mvn clean package -DskipTests

# Run a specific service
cd services/user-service
mvn spring-boot:run

# Build specific service
mvn clean package -pl services/user-service -am -DskipTests
```

### Database Access

```bash
# Connect to user-db
docker exec -it ja-user-db psql -U postgres -d user_db

# Connect to auth-db
docker exec -it ja-auth-db psql -U postgres -d auth_db

# List tables
\dt

# Exit
\q
```

---

## ✅ How to Check Services & Infrastructure

### Check Docker Containers Status

```bash
# Check all containers
docker-compose ps

# Check specific container
docker ps | grep ja-user-db

# Check container logs
docker-compose logs -f user-db
docker-compose logs -f eureka-server
docker-compose logs -f kafka
```

**Expected Output:**
```
NAME               STATUS
ja-user-db         Up (healthy)
ja-auth-db         Up (healthy)
ja-eureka-server   Up (healthy)
ja-kafka           Up (healthy)
ja-redis           Up (healthy)
```

---

### Check Databases

#### 1. Check Database Container Status

```bash
# Check if database is running
docker-compose ps user-db

# Check database logs
docker-compose logs user-db | tail -20
```

#### 2. Connect to Database

```bash
# Connect to user-db
docker exec -it ja-user-db psql -U postgres -d user_db

# Once connected, check tables
\dt

# Check specific table
SELECT * FROM users LIMIT 5;

# Exit
\q
```

#### 3. Test Database Connection from Host

```bash
# Test connection to user-db (port 5433)
psql -h localhost -p 5433 -U postgres -d user_db

# Test connection to auth-db (port 5434)
psql -h localhost -p 5434 -U postgres -d auth_db
```

#### 4. Check Database Tables Created

```bash
# For user-db
docker exec -it ja-user-db psql -U postgres -d user_db -c "\dt"

# For auth-db
docker exec -it ja-auth-db psql -U postgres -d auth_db -c "\dt"
```

**Expected Tables (user-db):**
- countries
- skills
- users
- user_education
- user_work_experience
- user_skills

**Expected Tables (auth-db):**
- credentials

---

### Check Eureka Service Discovery

#### 1. Check Eureka Dashboard

```bash
# Open in browser
open http://localhost:8761

# Or check via curl
curl http://localhost:8761
```

#### 2. Check Eureka Health

```bash
curl http://localhost:8761/actuator/health
```

**Expected Response:**
```json
{"status":"UP","components":{"discoveryComposite":{"status":"UP"}}}
```

#### 3. Check Registered Services

```bash
# Get all registered services
curl http://localhost:8761/eureka/apps

# Get specific service (e.g., user-service)
curl http://localhost:8761/eureka/apps/USER-SERVICE

# Pretty print JSON
curl -s http://localhost:8761/eureka/apps | python3 -m json.tool
```

#### 4. Check Service Registration in Eureka

When a service starts, it should appear in Eureka within 30 seconds. Check the dashboard:
- Go to http://localhost:8761
- Look for your service in the "Instances currently registered with Eureka" section
- Status should be "UP" (green)

---

### Check Kafka

#### 1. Check Kafka Container Status

```bash
# Check if Kafka is running
docker-compose ps kafka

# Check Kafka logs
docker-compose logs kafka | tail -30
```

#### 2. Check Kafka via Kafka UI

```bash
# Open Kafka UI in browser
open http://localhost:8090
```

In Kafka UI:
- Check **Topics** tab - should see topics like `user-registered`
- Check **Brokers** - should show 1 broker
- Check **Consumers** - should show consumer groups

#### 3. Check Kafka Topics via Command Line

```bash
# List topics
docker exec -it ja-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describe a topic
docker exec -it ja-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic user-registered

# Check consumer groups
docker exec -it ja-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

#### 4. Test Kafka Producer/Consumer

```bash
# Produce a test message
docker exec -it ja-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic test-topic

# Consume messages (in another terminal)
docker exec -it ja-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --from-beginning
```

---

### Check Redis

#### 1. Check Redis Container Status

```bash
# Check if Redis is running
docker-compose ps redis

# Check Redis logs
docker-compose logs redis | tail -20
```

#### 2. Check Redis via Redis Commander

```bash
# Open Redis Commander in browser
open http://localhost:8091
```

#### 3. Test Redis Connection

```bash
# Connect to Redis CLI
docker exec -it ja-redis redis-cli

# Test ping
PING
# Should return: PONG

# Set a test key
SET test_key "Hello Redis"

# Get the key
GET test_key

# List all keys
KEYS *

# Exit
exit
```

#### 4. Check Redis from Host

```bash
# If redis-cli is installed locally
redis-cli -h localhost -p 6379 ping
```

---

### Check Application Services

#### 1. Check Service Health Endpoints

```bash
# User Service
curl http://localhost:8085/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

#### 2. Check Service Info Endpoint

```bash
# Get service information
curl http://localhost:8085/actuator/info

# Get all actuator endpoints
curl http://localhost:8085/actuator
```

#### 3. Check Swagger UI

```bash
# Open Swagger UI in browser
open http://localhost:8085/swagger-ui.html
open http://localhost:8081/swagger-ui.html
open http://localhost:8080/swagger-ui.html
```

#### 4. Check Service Logs

```bash
# If running via Maven
# Logs appear in terminal where you ran: mvn spring-boot:run

# If running via Docker
docker-compose logs -f user-service
docker-compose logs -f auth-service
```

#### 5. Test Service Endpoints

```bash
# Test user-service GET all users
curl http://localhost:8085/api/v1/users

# Test auth-service health
curl http://localhost:8081/api/v1/auth/health

# Test via API Gateway
curl http://localhost:8080/api/v1/users
```

---

### Check Service-to-Service Communication

#### 1. Check Eureka Registration

```bash
# Verify service is registered in Eureka
curl -s http://localhost:8761/eureka/apps | grep -i "user-service"

# Check service instances
curl http://localhost:8761/eureka/apps/USER-SERVICE
```

#### 2. Test API Gateway Routing

```bash
# Test routing to user-service via gateway
curl http://localhost:8080/api/v1/users

# Test routing to auth-service via gateway
curl http://localhost:8080/api/v1/auth/health
```

#### 3. Check Kafka Event Flow

```bash
# 1. Register a user via auth-service
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!","firstName":"Test","lastName":"User"}'

# 2. Check Kafka UI for message in user-registered topic
open http://localhost:8090

# 3. Verify user was created in user-service
curl http://localhost:8085/api/v1/users
```

---

### Quick Health Check Script

Create a file `check-services.sh`:

```bash
#!/bin/bash

echo "🔍 Checking Infrastructure..."
echo ""

echo "📦 Docker Containers:"
docker-compose ps
echo ""

echo "🗄️  Databases:"
echo "  User DB:"
docker exec -it ja-user-db psql -U postgres -d user_db -c "SELECT COUNT(*) FROM users;" 2>/dev/null || echo "  ❌ Not accessible"
echo "  Auth DB:"
docker exec -it ja-auth-db psql -U postgres -d auth_db -c "SELECT COUNT(*) FROM credentials;" 2>/dev/null || echo "  ❌ Not accessible"
echo ""

echo "🔍 Eureka:"
curl -s http://localhost:8761/actuator/health | grep -q "UP" && echo "  ✅ Eureka is UP" || echo "  ❌ Eureka is DOWN"
echo ""

echo "📨 Kafka:"
docker exec -it ja-kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null | head -5 || echo "  ❌ Kafka not accessible"
echo ""

echo "💾 Redis:"
docker exec -it ja-redis redis-cli ping 2>/dev/null | grep -q "PONG" && echo "  ✅ Redis is UP" || echo "  ❌ Redis is DOWN"
echo ""

echo "🚀 Services:"
curl -s http://localhost:8085/actuator/health 2>/dev/null | grep -q "UP" && echo "  ✅ User Service" || echo "  ❌ User Service"
curl -s http://localhost:8081/actuator/health 2>/dev/null | grep -q "UP" && echo "  ✅ Auth Service" || echo "  ❌ Auth Service"
curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -q "UP" && echo "  ✅ API Gateway" || echo "  ❌ API Gateway"
echo ""
```

Make it executable:
```bash
chmod +x check-services.sh
./check-services.sh
```

---

## 🔄 Daily Workflow

### Starting Your Day

```bash
# 1. Start infrastructure for your service
docker-compose up -d user-db kafka eureka-server

# 2. Wait for services (~30 seconds)
docker-compose logs -f user-db
# Press Ctrl+C when ready

# 3. Run your service
cd services/user-service
mvn spring-boot:run
```

### Ending Your Day

```bash
# Stop your service (Ctrl+C)

# Optional: Stop Docker containers
docker-compose down
```

---

## 🗂️ Project Structure

```
JobApplicant_BE/
├── common/                          # Shared code (DTOs, exceptions, utils)
├── services/
│   ├── eureka-server/              # Service Discovery
│   ├── api-gateway/                # API Gateway (port 8080)
│   ├── auth-service/               # Authentication (port 8081)
│   ├── user-service/               # User Management (port 8085)
│   ├── application-service/        # Job Applications (port 8083)
│   ├── notification-service/       # Notifications (port 8086)
│   ├── subscription-service/       # Subscriptions (port 8087)
│   └── admin-service/              # Admin Panel (port 8088)
├── docker-compose.yml              # Docker configuration
├── pom.xml                         # Parent Maven POM
└── SETUP.md                        # This file
```

---

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Find what's using the port
lsof -i :5433

# Kill the process or change the port in docker-compose.yml
```

### Database Connection Failed

```bash
# Check if container is running
docker-compose ps

# Check logs
docker-compose logs user-db

# Restart container
docker-compose restart user-db
```

### Kafka Connection Issues

```bash
# Ensure Zookeeper started first
docker-compose up -d zookeeper
# Wait 10 seconds
docker-compose up -d kafka

# Check Kafka logs
docker-compose logs -f kafka
```

### Eureka Service Not Registering

```bash
# Check Eureka is running
curl http://localhost:8761/actuator/health

# Check service logs for Eureka errors
docker-compose logs auth-service | grep -i eureka
```

### Reset Everything

```bash
# Nuclear option: Remove all containers and data
docker-compose down -v
docker system prune -f

# Start fresh
docker-compose up -d
```

---

## 📊 Data Persistence

| Command | Data |
|---------|------|
| `docker-compose up -d` | ✅ Preserved |
| `docker-compose down` | ✅ Preserved |
| `docker-compose restart` | ✅ Preserved |
| `docker-compose down -v` | ❌ **DELETED** |

---

## 🔗 Useful Links

| Resource | URL |
|----------|-----|
| Eureka Dashboard | http://localhost:8761 |
| Kafka UI | http://localhost:8090 |
| Redis Commander | http://localhost:8091 |
| API Gateway | http://localhost:8080 |

---

## ✅ Setup Checklist

### Prerequisites
- [ ] Java 21 installed (`java -version`)
- [ ] Maven 3.9+ installed (`mvn -version`)
- [ ] Docker & Docker Compose installed (`docker --version`)
- [ ] Repository cloned

### Infrastructure
- [ ] `docker-compose up -d` runs without errors
- [ ] All database containers are healthy (`docker-compose ps`)
- [ ] Eureka accessible at http://localhost:8761
- [ ] Kafka UI accessible at http://localhost:8090
- [ ] Redis Commander accessible at http://localhost:8091

### Database Verification
- [ ] Can connect to user-db (`docker exec -it ja-user-db psql -U postgres -d user_db`)
- [ ] Tables created in user-db (`\dt` shows tables)
- [ ] Can connect to auth-db (`docker exec -it ja-auth-db psql -U postgres -d auth_db`)

### Service Verification
- [ ] At least one service runs successfully (`mvn spring-boot:run`)
- [ ] Service health endpoint returns UP (`curl http://localhost:8085/actuator/health`)
- [ ] Service registered in Eureka (check http://localhost:8761)
- [ ] Swagger UI accessible (http://localhost:8085/swagger-ui.html)

### Integration Verification
- [ ] Kafka topics visible in Kafka UI
- [ ] Service can communicate via API Gateway
- [ ] Events flow through Kafka (register user → check Kafka UI)

---

**Happy Coding! 🚀**
