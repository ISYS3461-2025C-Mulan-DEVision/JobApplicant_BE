# SeaweedFS Deployment & S3 Configuration Guide

## Overview
SeaweedFS is a distributed file storage system with S3-compatible API. This guide covers deployment, S3 configuration, and bucket/user setup.

## Architecture
- **Master Server** (port 9333): Manages volumes and metadata
- **Volume Server** (port 8080): Stores actual file data
- **S3 API Gateway** (port 8333): S3-compatible interface for applications
- **Admin UI** (port 16625): Web interface for management
- **Filer Server** (port 8888): File directory service

## Deployment

### 1. Docker Compose Setup
```yaml
# docker-compose.yml
seaweedfs:
  image: chrislusf/seaweedfs:latest
  ports:
    - "127.0.0.1:9333:9333"   # Master (localhost only)
    - "127.0.0.1:8444:8444"   # Volume (localhost only)
    - "127.0.0.1:16625:16625" # Admin UI (localhost only)
  volumes:
    - seaweedfs_data:/data
    - ./seaweedfs-entrypoint.sh:/seaweedfs-entrypoint.sh
    - ./config.json:/etc/seaweedfs/config.json
  entrypoint: /seaweedfs-entrypoint.sh
  environment:
    - SEAWEED_S3_ACCESS_KEY=minioadmin
    - SEAWEED_S3_SECRET_KEY=minioadmin
```

### 2. Entrypoint Script
File: `seaweedfs-entrypoint.sh`

```bash
#!/bin/sh
# Create data directory
mkdir -p /data
chown -R 1000:1000 /data

# Start weed server (master, volume, filer, S3 all-in-one)
weed server \
  -dir=/data \
  -master.volumeSizeLimitMB=1024 \
  -s3 \
  -s3.config=/etc/seaweedfs/config.json &
SERVER_PID=$!

sleep 2

# Start admin UI
weed admin -port=16625 -masters=localhost:9333 -adminUser=admin -adminPassword=admin

wait $SERVER_PID
```

### 3. Data Persistence
- **Named Volume**: `seaweedfs_data:/data` persists master metadata and volumes
- **Master Data**: Stored in `/data` directory
- **Recovery**: Metadata automatically restored on container restart

## S3 Configuration

### 1. IAM Config File
File: `config.json`

```json
{
  "identities": [
    {
      "name": "application-service",
      "credentials": [
        {
          "accessKey": "minioadmin",
          "secretKey": "minioadmin"
        }
      ],
      "actions": ["Read", "Write"],
      "buckets": ["job-applications"]
    },
    {
      "name": "admin",
      "credentials": [
        {
          "accessKey": "admin",
          "secretKey": "admin"
        }
      ],
      "actions": ["Read", "Write", "Admin"]
    }
  ]
}
```

**Actions:**
- `Read` - Get/list objects
- `Write` - Put/delete objects
- `Admin` - Create/delete buckets, manage policies
- `buckets` (optional) - Restrict user to specific buckets

### 2. User Roles

| Role | Permissions | Use Case |
|------|-------------|----------|
| `Read/Write` | Upload, download files | Application service |
| `Admin` | Full bucket management | Manual setup, admin operations |
| `Read Only` | View, download only | Public access, reports |

## Bucket Setup

### Option A: Pre-Create Buckets (Recommended)

Create during initialization using S3 client:
```bash
# Using AWS CLI
aws s3api create-bucket --bucket job-applications --endpoint-url http://localhost:8333
```

Or in application startup:
```java
// Spring Boot initialization
AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
    .withEndpointConfiguration(...)
    .build();
s3Client.createBucket("job-applications");
```

### Option B: Auto-Create via Application

Application requires `Admin` role:
```json
{
  "name": "application-service",
  "actions": ["Read", "Write", "Admin"],  // ⚠️ Less secure
  "buckets": ["job-applications"]
}
```

## Application Integration

### Spring Boot Configuration
```properties
# application.properties
aws.s3.endpoint=http://seaweedfs:8333
aws.s3.accessKey=minioadmin
aws.s3.secretKey=minioadmin
aws.s3.bucket=job-applications
aws.s3.region=us-east-1
```

### S3 Client Code
```java
@Configuration
public class S3Config {
    @Bean
    public S3Client s3Client(@Value("${aws.s3.endpoint}") String endpoint,
                            @Value("${aws.s3.accessKey}") String accessKey,
                            @Value("${aws.s3.secretKey}") String secretKey) {
        return S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
    }
}
```

## Admin Interface

**Access:** http://localhost:16625

**Login Credentials:**
- Username: `admin`
- Password: `admin`

**Available Operations:**
- View master/volume status
- Monitor bucket storage
- View file metadata
- Manage replication settings

## Security Best Practices

### 1. Change Default Credentials
Replace weak defaults in `config.json` and entrypoint script:
```bash
# Generate strong credentials
openssl rand -base64 32
```

### 2. Port Binding
All ports should be `localhost-only` for internal Docker network:
```yaml
ports:
  - "127.0.0.1:9333:9333"   # ✅ Internal only
  - "0.0.0.0:8333:8333"     # ❌ Exposed (only if needed)
```

### 3. IAM Policy
- Application service: `Read/Write` to specific buckets only
- Admin credentials: Separate from application
- No public S3 URLs: Use backend proxy for downloads

## Troubleshooting

### Master Not Starting
**Error:** `flag provided but not defined: -mdir`

**Solution:** Use correct flag `-master.dir` or omit (defaults to `-dir`):
```bash
weed server -dir=/data  # Metadata stored in /data
```

### Volume Not Found After Restart
**Error:** "volume X not found"

**Cause:** Master metadata not persisted

**Solution:** Ensure Docker volume is mounted:
```yaml
volumes:
  - seaweedfs_data:/data  # Named volume for persistence
```

### S3 Bucket Access Denied
**Cause:** User credentials not in IAM config or bucket not in allowed list

**Solution:** Check `config.json` for correct credentials and bucket names

### Admin UI Not Accessible
**Cause:** Port not exposed or credentials wrong

**Solution:** 
```bash
# Check if admin is running
docker logs ja-seaweedfs | grep "Admin"

# Verify port binding
docker port ja-seaweedfs | grep 16625
```

## Deployment Checklist

- [ ] Docker Compose configured with persistent volume
- [ ] `seaweedfs-entrypoint.sh` created and executable
- [ ] `config.json` with IAM users configured
- [ ] Buckets pre-created or auto-created on app startup
- [ ] Application credentials match IAM config
- [ ] S3 endpoint configured correctly in app
- [ ] Ports bound to localhost only (security)
- [ ] Admin UI accessible for monitoring
- [ ] Default credentials changed (production)

## References

- [SeaweedFS Documentation](https://github.com/chrislusf/seaweedfs)
- [S3 API Compatibility](https://seaweedfs.readthedocs.io/en/latest/s3/)
- [IAM Configuration](https://seaweedfs.readthedocs.io/en/latest/s3/#iam-api-configuration)
