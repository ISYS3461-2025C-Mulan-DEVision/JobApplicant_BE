# Job Matching Notification - Testing Guide

## Quick Test Scenarios

### Scenario 1: Happy Path - Premium User Gets Matched Job Notification

#### Prerequisites
```bash
# 1. Premium user exists with active subscription
User {
  id: "user-123",
  email: "john@example.com",
  isPremium: true
}

# 2. User has search profile
UserSearchProfile {
  userId: "user-123",
  countryAbbreviation: "VN",
  salaryMin: 50000,
  salaryMax: 100000,
  skills: ["Java", "Spring Boot", "Docker"],
  employmentTypes: ["FULL_TIME"],
  jobTitles: ["Software Engineer", "Backend Developer"]
}
```

#### Test Steps

**Step 1: Job Manager publishes job to Kafka**
```bash
# Topic: jobpost.published
{
  "jobPostId": "job-456",
  "title": "Senior Backend Developer",
  "countryCode": "VN",
  "city": "Ho Chi Minh City",
  "employmentTypes": ["FULL_TIME"],
  "salaryMin": 60000,
  "salaryMax": 90000,
  "requiredSkillIds": ["java-uuid", "spring-boot-uuid", "react-uuid"],
  "fresher": false,
  "publishedAt": "2026-01-13T10:00:00"
}
```

**Step 2: Verify User-Service Processing**
```bash
# Check user-service logs
docker logs user-service 2>&1 | grep "job-456"

# Expected logs:
# "Received job posting event: jobId=job-456, title=Senior Backend Developer"
# "Job job-456 matched with profile for premium user user-123, sent to notification-service"
```

**Step 3: Verify Notification-Service Processing**
```bash
# Check notification-service logs
docker logs notification-service 2>&1 | grep "JobMatchedEvent"

# Expected logs:
# "Received JobMatchedEvent for premium user: user-123 and job post: job-456 (Senior Backend Developer)"
# "Successfully created job match notification for premium user: user-123 for job post: job-456"
```

**Step 4: Query Notification via API**
```bash
curl -X GET "http://localhost:8084/api/v1/notifications/user/user-123?type=JOB_MATCH" \
  -H "Authorization: Bearer {token}"
```

**Expected Response:**
```json
{
  "status": "SUCCESS",
  "message": "Notifications retrieved successfully",
  "data": {
    "content": [
      {
        "id": "notif-uuid",
        "userId": "user-123",
        "notificationType": "JOB_MATCH",
        "title": "🎯 New Job Match!",
        "message": "Great news! A new job posting matches your search profile: Senior Backend Developer in Ho Chi Minh City, VN. Click to view details and apply now!",
        "jobPostId": "job-456",
        "isRead": false,
        "createdAt": "2026-01-13T10:00:05"
      }
    ],
    "totalElements": 1
  }
}
```

**✅ Test Pass Criteria:**
- User-service publishes JobMatchedEvent to job-matched topic
- Notification-service creates notification
- Notification is retrievable via API
- No errors in logs

---

### Scenario 2: Free User Should NOT Get Notification

#### Prerequisites
```bash
User {
  id: "user-456",
  email: "jane@example.com",
  isPremium: false  # FREE USER
}

UserSearchProfile {
  userId: "user-456",
  countryAbbreviation: "VN",
  # ... matches job criteria
}
```

#### Test Steps

**Step 1: Job Manager publishes matching job**
```bash
# Same job as Scenario 1
```

**Step 2: Verify User-Service Skips Free User**
```bash
# Check user-service logs
docker logs user-service 2>&1 | grep "user-456"

# Expected log:
# "Skipping job match for user user-456 - user not found or not premium"
```

**Step 3: Verify NO Notification Created**
```bash
curl -X GET "http://localhost:8084/api/v1/notifications/user/user-456?type=JOB_MATCH" \
  -H "Authorization: Bearer {token}"

# Expected: Empty list
{
  "data": {
    "content": [],
    "totalElements": 0
  }
}
```

**✅ Test Pass Criteria:**
- User-service skips free user
- No JobMatchedEvent published for this user
- No notification created

---

### Scenario 3: Job Does NOT Match - No Notification

#### Prerequisites
```bash
User {
  id: "user-789",
  isPremium: true
}

UserSearchProfile {
  userId: "user-789",
  countryAbbreviation: "SG",  # SINGAPORE
  salaryMin: 100000,
  # ...
}
```

#### Test Steps

**Step 1: Job Manager publishes non-matching job**
```bash
# Topic: jobpost.published
{
  "jobPostId": "job-789",
  "title": "Junior Developer",
  "countryCode": "VN",  # VIETNAM - doesn't match SG
  "salaryMin": 30000,   # Below user's minimum
  "salaryMax": 50000,
  # ...
}
```

**Step 2: Verify User-Service Rejects Match**
```bash
# Check user-service logs
docker logs user-service 2>&1 | grep "job-789"

# Expected log:
# "Country mismatch for job job-789 and profile user-789"
# OR
# "Salary mismatch for job job-789 and profile user-789"
```

**Step 3: Verify NO Notification Created**
```bash
curl -X GET "http://localhost:8084/api/v1/notifications/user/user-789?type=JOB_MATCH"

# Expected: Empty or no notification for job-789
```

**✅ Test Pass Criteria:**
- JobMatchingService correctly rejects non-matching job
- No JobMatchedEvent published
- No notification created

---

### Scenario 4: Duplicate Prevention

#### Test Steps

**Step 1: Same job published twice**
```bash
# First time - creates notification
# Second time - should prevent duplicate

# Check notification-service logs
docker logs notification-service 2>&1 | grep "already exists"

# Expected log:
# "Job match notification already exists for user: user-123 and job: job-456"
```

**Step 2: Verify Only One Notification Exists**
```bash
curl -X GET "http://localhost:8084/api/v1/notifications/user/user-123?type=JOB_MATCH"

# Verify only 1 notification for job-456, not 2
```

**✅ Test Pass Criteria:**
- Duplicate check prevents multiple notifications
- Only one notification per user+job combination

---

## Kafka Topic Verification

### Check Topics Exist

```bash
# Access Kafka container
docker exec -it kafka bash

# List topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Expected topics:
# - jobpost.published
# - jobpost.skills.changed
# - jobpost.country.changed
# - job-matched
```

### Consume from Topics

```bash
# Consume job-matched events
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic job-matched \
  --from-beginning

# Expected output:
# {"eventId":"...","userId":"user-123","jobPostId":"job-456","jobTitle":"Senior Backend Developer",...}
```

---

## Database Verification

### Check Notifications Table

```sql
-- Connect to notification database
psql -h localhost -p 5436 -U notification_user -d notification_db

-- View all job match notifications
SELECT 
  id,
  user_id,
  notification_type,
  title,
  LEFT(message, 50) as message_preview,
  job_post_id,
  is_read,
  created_at
FROM notifications
WHERE notification_type = 'JOB_MATCH'
ORDER BY created_at DESC;

-- Count notifications per user
SELECT 
  user_id,
  COUNT(*) as notification_count,
  COUNT(CASE WHEN is_read = false THEN 1 END) as unread_count
FROM notifications
WHERE notification_type = 'JOB_MATCH'
  AND is_active = true
GROUP BY user_id;
```

### Check Search Profiles

```sql
-- Connect to user database
psql -h localhost -p 5432 -U user_user -d user_db

-- View search profiles with details
SELECT 
  usp.id,
  usp.user_id,
  u.email,
  u.is_premium,
  usp.country_abbreviation,
  usp.salary_min,
  usp.salary_max,
  COUNT(DISTINCT usps.skill_id) as skill_count,
  COUNT(DISTINCT uspe.employment_type) as employment_type_count,
  COUNT(DISTINCT uspjt.job_title) as job_title_count
FROM users_search_profiles usp
JOIN users u ON u.id = usp.user_id
LEFT JOIN user_search_profile_skills usps ON usps.user_search_profile_id = usp.id
LEFT JOIN user_search_profile_employment_statuses uspe ON uspe.user_search_profile_id = usp.id
LEFT JOIN user_search_profile_job_titles uspjt ON uspjt.user_search_profile_id = usp.id
WHERE usp.is_active = true
  AND u.is_premium = true
GROUP BY usp.id, usp.user_id, u.email, u.is_premium, usp.country_abbreviation, usp.salary_min, usp.salary_max;
```

---

## REST API Testing

### Get All Notifications

```bash
curl -X GET "http://localhost:8084/api/v1/notifications/user/{userId}" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"
```

### Filter by Job Match Type

```bash
curl -X GET "http://localhost:8084/api/v1/notifications/user/{userId}?type=JOB_MATCH&page=0&size=10" \
  -H "Authorization: Bearer {token}"
```

### Get Unread Count

```bash
curl -X GET "http://localhost:8084/api/v1/notifications/user/{userId}/unread-count" \
  -H "Authorization: Bearer {token}"
```

### Mark as Read

```bash
curl -X PUT "http://localhost:8084/api/v1/notifications/{notificationId}/user/{userId}/read" \
  -H "Authorization: Bearer {token}"
```

### Mark All as Read

```bash
curl -X PUT "http://localhost:8084/api/v1/notifications/user/{userId}/read-all" \
  -H "Authorization: Bearer {token}"
```

---

## Load Testing

### Simulate Multiple Job Posts

```bash
# Script to publish multiple jobs
for i in {1..100}; do
  # Publish job via Kafka or Job Manager API
  echo "Publishing job $i"
  # ... publish logic
done

# Monitor notification creation rate
watch -n 1 'docker logs notification-service 2>&1 | grep "Successfully created" | wc -l'
```

### Check Performance Metrics

```bash
# User-service processing time
docker logs user-service 2>&1 | grep "Processing job against" | tail -20

# Notification-service processing time
docker logs notification-service 2>&1 | grep "Successfully created" | tail -20
```

---

## Troubleshooting Commands

### Check Service Health

```bash
# Notification service
curl http://localhost:8084/api/v1/notifications/health

# User service
curl http://localhost:8083/api/v1/users/health
```

### Check Kafka Consumer Groups

```bash
docker exec -it kafka bash

kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group notification-service-group

# Check lag - should be 0 if processing in real-time
```

### Restart Services

```bash
# Restart notification-service
docker-compose restart notification-service

# Restart user-service
docker-compose restart user-service

# Restart Kafka
docker-compose restart kafka
```

---

## Common Issues & Solutions

### Issue 1: Notifications Not Created

**Symptoms:**
- Job posts published but no notifications appear
- Logs show "Received job posting event" but no "Successfully created notification"

**Check:**
1. User is premium: `SELECT is_premium FROM users WHERE id = 'user-id';`
2. Search profile exists: `SELECT * FROM users_search_profiles WHERE user_id = 'user-id';`
3. Kafka topics connected: Check consumer group lag
4. Job matching criteria: Review JobMatchingService logs

**Solution:**
```bash
# Check user-service logs for matching details
docker logs user-service 2>&1 | grep "mismatch" | tail -20

# Verify premium status
UPDATE users SET is_premium = true WHERE id = 'user-id';
```

### Issue 2: Duplicate Notifications

**Symptoms:**
- Multiple notifications for same job+user

**Check:**
```sql
SELECT job_post_id, user_id, COUNT(*) as count
FROM notifications
WHERE notification_type = 'JOB_MATCH'
GROUP BY job_post_id, user_id
HAVING COUNT(*) > 1;
```

**Solution:**
- Should not happen due to duplicate check in NotificationServiceImpl
- If it does, check database transaction isolation level

### Issue 3: Kafka Consumer Lag

**Symptoms:**
- Delays between job post and notification

**Check:**
```bash
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group notification-service-group
```

**Solution:**
- Scale up consumer instances
- Increase Kafka partition count
- Optimize matching algorithm

---

## Success Criteria

✅ **Complete Implementation:**
- [x] JobPostMatchConsumer listens to correct topic (JOB_MATCHED)
- [x] Kafka configuration updated with JobMatchedEvent consumer factory
- [x] Notifications created for premium users only
- [x] Duplicate prevention working
- [x] Rich notification messages with job details
- [x] REST API returns notifications correctly
- [x] No errors in service logs

✅ **Performance:**
- Notification created within 1 second of job post
- No Kafka consumer lag
- Database queries optimized

✅ **Testing:**
- Premium user receives notification
- Free user does NOT receive notification
- Non-matching jobs don't create notifications
- Duplicate prevention works

---

## Next Steps

1. **Manual Testing:** Follow Scenario 1 end-to-end
2. **Integration Testing:** Test with real Job Manager service
3. **Load Testing:** Simulate 100+ jobs published
4. **Monitor:** Check logs and metrics in production

---

**Last Updated:** January 13, 2026

