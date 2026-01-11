# Critik - Art Review & Discussion Platform

A production-ready social media backend application for reviewing and discussing art. Users can post artworks, credit artists, share locations, and engage in discussions through comments and reactions.

## Features

### Authentication & Security
- **JWT-based authentication** with stateless sessions
- **BCrypt password hashing** (strength 12) for secure password storage
- **Strong password requirements**: minimum 8 characters with uppercase, lowercase, digit, and special character
- **Email validation** on registration
- **Username normalization** (case-insensitive, trimmed)
- **Protected endpoints** - only authenticated users can create/modify content
- **Ownership verification** - users can only edit/delete their own content
- **Rate limiting** - Protects against brute force and DoS attacks

### Artwork Management
- **Create artworks** with image upload, title, artist name, location, interpretation, and tags
- **Update artworks** (owner only) - modify any field except the image
- **Delete artworks** (owner only) - also removes the associated image file
- **View artworks** - public access to feed and individual artworks
- **File validation** - only allows image files (jpg, jpeg, png, gif, webp)

### Feed System
- **Public feed** (`/artworks/feed`) - accessible without authentication
- **Personalized feed** (`/artworks/feed/{userId}`) - shows artworks from followed users
- **Fallback to recent** - if following no one, shows recent artworks

### Comments & Discussions
- **Top-level comments** on artworks
- **Nested replies** - reply to comments and replies (unlimited depth)
- **Delete comments** (owner only) - cascades to all replies
- **Tree structure** - API returns comments with nested replies

### Follow System
- **Follow/unfollow users** - authenticated users only
- **Secure endpoints** - uses authenticated user, prevents manipulation
- **View followers/following** for any user

### Reactions
- **AGREE/DISAGREE** reactions on artworks
- **One reaction per user** per artwork (can change or remove)
- **Reaction counts** - public access

### Search
- **Search users** by username or display name
- **Search artworks** by title, location, or tags
- **Public access** to search endpoints

### Profile Management
- **View profiles** - public access with artwork list and follow status
- **Edit own profile** - display name, email, bio (owner only)
- **Secure updates** - verified against authenticated user

## API Endpoints

### Authentication
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | Login, returns JWT | No |

### Artworks
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/artworks/feed` | Public feed | No |
| GET | `/artworks/feed/{userId}` | Personalized feed | Yes |
| GET | `/artworks/my` | Current user's artworks | Yes |
| GET | `/artworks/{id}` | Single artwork | No |
| POST | `/artworks` | Create artwork (multipart) | Yes |
| PUT | `/artworks/{id}` | Update artwork | Yes (owner) |
| DELETE | `/artworks/{id}` | Delete artwork | Yes (owner) |

### Comments
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/artworks/{id}/comments` | Get comments tree | No |
| POST | `/artworks/{id}/comments` | Add comment | Yes |
| POST | `/artworks/{id}/comments/{commentId}/replies` | Reply to comment | Yes |
| DELETE | `/artworks/{id}/comments/{commentId}` | Delete comment | Yes (owner) |

### Reactions
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/artworks/{id}/reactions` | Get reaction counts | No |
| GET | `/artworks/{id}/reactions/me` | Get user's reaction | Yes |
| POST | `/artworks/{id}/reactions?type=AGREE` | Set reaction | Yes |
| DELETE | `/artworks/{id}/reactions` | Remove reaction | Yes |

### Follow
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/follow/{userId}` | Follow user | Yes |
| DELETE | `/follow/{userId}` | Unfollow user | Yes |
| GET | `/follow/{userId}/followers` | Get followers | Yes |
| GET | `/follow/{userId}/following` | Get following | Yes |

### Profile
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/users/{username}` | Get profile | No |
| PUT | `/users/{id}/edit` | Update profile | Yes (owner) |

### Search
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/search/users?q={query}` | Search users | No |
| GET | `/search/artworks?title=&location=&tags=` | Search artworks | No |

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **PostgreSQL 18** (primary database)
- **Bucket4j** for rate limiting
- **Caffeine** for high-performance caching
- **Spring Boot Actuator** for monitoring
- **Lombok** for boilerplate reduction
- **Flyway** for database migrations (optional)
- **BCrypt** for password hashing

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 18

### Database Setup

```bash
# Connect to PostgreSQL and create the database
psql -U postgres

CREATE DATABASE critik;
CREATE USER critik WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE critik TO critik;
\q
```

### Environment Variables

Create a `.env` file in the project root:

```env
# Required
JWT_SECRET=your-super-secret-key-at-least-32-characters-long
DB_PASSWORD=your_db_password

# Optional (defaults shown)
DB_URL=jdbc:postgresql://localhost:5432/critik
DB_USERNAME=critik
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://localhost:4200
FILE_UPLOAD_DIR=uploads
PORT=8080

# Rate Limiting (requests per minute)
RATE_LIMIT_LOGIN=5
RATE_LIMIT_REGISTER=3
RATE_LIMIT_API=60
RATE_LIMIT_SEARCH=30
```

### Generate a Secure JWT Secret

```bash
openssl rand -base64 32
```

### Run the Application

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/critik-0.0.1-SNAPSHOT.jar
```

### Run Tests

```bash
./mvnw test
```

## Rate Limiting

The application includes built-in rate limiting to protect against abuse:

| Endpoint | Default Limit | Purpose |
|----------|---------------|---------|
| `/auth/login` | 5/min per IP | Prevent brute force attacks |
| `/auth/register` | 3/min per IP | Prevent spam accounts |
| `/search/**` | 30/min per IP | Prevent scraping |
| All other API | 60/min per user | Standard usage limits |

### Rate Limit Response Headers

All responses include rate limit information:
- `X-Rate-Limit-Remaining`: Tokens remaining in the current window
- `X-Rate-Limit-Retry-After-Seconds`: Seconds until tokens are available (when limited)

When rate limited, you'll receive HTTP 429 with:
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again in 45 seconds.",
  "retryAfterSeconds": 45
}
```

## Production Deployment

### Security Checklist

- [ ] **JWT Secret**: Use a cryptographically secure random string (32+ characters)
- [ ] **HTTPS**: Always use HTTPS in production (terminate SSL at load balancer)
- [ ] **CORS**: Update `CORS_ALLOWED_ORIGINS` to your frontend domain(s)
- [ ] **Database**: Use strong credentials and SSL connections
- [ ] **Passwords**: Already enforced - 8+ chars, mixed case, digit, special char
- [ ] **File uploads**: Consider external storage (S3, CloudFront) for scalability
- [ ] **Rate limiting**: Tune limits based on expected traffic
- [ ] **Monitoring**: Enable Actuator endpoints behind authentication
- [ ] **Secrets management**: Use vault/secrets manager, not env files

### Recommended Production Settings

```env
# Database
DDL_AUTO=validate
SHOW_SQL=false
FLYWAY_ENABLED=true
DB_POOL_SIZE=20

# Logging
LOG_LEVEL=WARN
APP_LOG_LEVEL=INFO

# Security
JWT_EXPIRATION=3600000  # 1 hour with refresh tokens

# Rate limiting (adjust based on traffic)
RATE_LIMIT_LOGIN=10
RATE_LIMIT_REGISTER=5
RATE_LIMIT_API=120
RATE_LIMIT_SEARCH=60
```

### Database Migrations with Flyway

For production, enable Flyway and create migration scripts:

```
src/main/resources/db/migration/
├── V1__Initial_schema.sql
├── V2__Add_artist_name_column.sql
└── V3__Add_comment_replies.sql
```

### Docker Deployment

Example `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -S critik && adduser -S critik -G critik
USER critik

COPY target/critik-*.jar app.jar
EXPOSE 8080

# JVM tuning for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

Example `docker-compose.yml`:

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - DB_URL=jdbc:postgresql://db:5432/critik
      - DB_USERNAME=critik
      - DB_PASSWORD=${DB_PASSWORD}
      - CORS_ALLOWED_ORIGINS=https://your-frontend.com
      - DDL_AUTO=validate
      - FLYWAY_ENABLED=true
    depends_on:
      db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
  
  db:
    image: postgres:18-alpine
    environment:
      - POSTGRES_DB=critik
      - POSTGRES_USER=critik
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U critik -d critik"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

### Cloud Deployment Options

| Platform | Recommended Setup | Database |
|----------|-------------------|----------|
| **AWS** | ECS Fargate or EKS | RDS PostgreSQL 18 |
| **Google Cloud** | Cloud Run | Cloud SQL PostgreSQL |
| **Azure** | Container Apps | Azure Database for PostgreSQL |
| **DigitalOcean** | App Platform | Managed PostgreSQL |
| **Railway** | One-click deploy | Managed PostgreSQL |
| **Render** | Web Service | Managed PostgreSQL |

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: critik-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: critik-api
  template:
    metadata:
      labels:
        app: critik-api
    spec:
      containers:
      - name: critik
        image: your-registry/critik:latest
        ports:
        - containerPort: 8080
        env:
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: critik-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

### Monitoring & Observability

The application exposes Actuator endpoints for monitoring:

```bash
# Health check (public)
GET /actuator/health

# Detailed health (authenticated)
GET /actuator/health

# Metrics
GET /actuator/metrics

# Prometheus format
GET /actuator/prometheus
```

**Recommended Monitoring Stack:**

1. **Metrics**: Prometheus + Grafana
   - JVM metrics (memory, GC, threads)
   - HTTP request metrics (latency, error rates)
   - Custom business metrics

2. **Logging**: ELK Stack or Loki + Grafana
   - Structured JSON logging recommended
   - Centralized log aggregation

3. **Tracing**: Jaeger or Zipkin
   - Distributed tracing for debugging
   - Request flow visualization

4. **Alerting**: PagerDuty, OpsGenie, or Grafana Alerts
   - High error rates
   - Elevated latency
   - Resource exhaustion

### Performance Optimization

1. **Database**
   - Add indexes on frequently queried columns
   - Enable connection pooling (HikariCP configured)
   - Consider read replicas for heavy read workloads

2. **Caching**
   - Add Redis for session/rate limit storage (multi-instance)
   - Cache frequently accessed data (user profiles, popular artworks)

3. **File Storage**
   - Use CDN (CloudFront, Cloudflare) for uploaded images
   - Consider S3 or similar object storage
   - Implement image optimization/resizing

4. **API Performance**
   - Enable response compression (configured)
   - Implement pagination for list endpoints
   - Consider GraphQL for flexible queries

## Project Structure

```
src/main/java/com/application/critik/
├── CritikApplication.java          # Main application entry point
├── config/
│   ├── SecurityConfig.java         # Security configuration with JWT
│   └── WebConfig.java              # Web MVC configuration
├── controllers/
│   ├── AuthController.java         # Authentication endpoints
│   ├── ArtworkController.java      # Artwork CRUD operations
│   ├── CommentController.java      # Comment operations
│   ├── FollowController.java       # Follow/unfollow operations
│   ├── ProfileController.java      # User profile operations
│   ├── ReactionController.java     # Reaction operations
│   └── SearchController.java       # Search endpoints
├── dto/
│   ├── AuthRequest.java            # Login request DTO
│   ├── AuthResponse.java           # Login response with token
│   ├── ArtworkDto.java             # Artwork data transfer object
│   ├── ProfileResponse.java        # Profile response DTO
│   ├── UserDto.java                # User data transfer object
│   └── UserUpdateRequest.java      # Profile update request
├── entities/
│   ├── Artwork.java                # Artwork entity with artist name
│   ├── Comment.java                # Comment entity with nested replies
│   ├── Follow.java                 # Follow relationship entity
│   ├── Reaction.java               # Reaction entity
│   ├── ReactionType.java           # AGREE/DISAGREE enum
│   └── User.java                   # User entity
├── exceptions/
│   ├── GlobalExceptionHandler.java # Centralized exception handling
│   ├── DuplicateResourceException.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
├── mappers/
│   └── ArtworkMapper.java          # Entity to DTO mapping
├── repositories/
│   ├── ArtworkRepository.java
│   ├── CommentRepository.java
│   ├── FollowRepository.java
│   ├── ReactionRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtRequestFilter.java       # JWT authentication filter
│   ├── JwtUtil.java                # JWT token utilities
│   └── UserDetailsServiceImpl.java # User details service
└── services/
    ├── AuthService.java            # Authentication logic
    ├── ArtworkService.java         # Artwork business logic
    ├── CommentService.java         # Comment business logic
    ├── FollowService.java          # Follow business logic
    ├── ProfileService.java         # Profile business logic
    ├── ReactionService.java        # Reaction business logic
    └── SearchService.java          # Search business logic
```

## Security Improvements Made

1. **Password never exposed** - `@JsonIgnore` on password field
2. **Ownership verification** - Users can only modify their own content
3. **Strong password validation** - Enforced on registration
4. **Email validation** - Format and uniqueness checks
5. **Input validation** - Length limits and required field checks
6. **Secure follow system** - Uses authenticated user, not URL parameter
7. **Global exception handling** - Consistent error responses, no internal details exposed
8. **File upload validation** - Only allowed image extensions
9. **CORS configuration** - Configurable via environment variable

## Production-Grade Enhancement Suggestions

To make Critik a fully production-ready social media platform, consider implementing these additional features:

### 🔐 Security Enhancements

| Feature | Priority | Description |
|---------|----------|-------------|
| **Refresh Tokens** | High | Implement short-lived access tokens (15min) with long-lived refresh tokens (7 days) for better security |
| **Account Lockout** | High | Lock accounts after 5 failed login attempts for 15 minutes |
| **Email Verification** | High | Require email verification before activating accounts |
| **Password Reset** | High | Secure password reset flow with time-limited tokens |
| **2FA/MFA** | Medium | Add TOTP-based two-factor authentication |
| **OAuth2 Social Login** | Medium | Allow login via Google, GitHub, Apple |
| **API Key Authentication** | Low | For third-party integrations and mobile apps |
| **IP Blacklisting** | Low | Block malicious IPs at the application level |

### 📱 User Experience Features

| Feature | Priority | Description |
|---------|----------|-------------|
| **Notifications** | High | Real-time notifications for follows, comments, reactions |
| **Direct Messaging** | High | Private messaging between users |
| **User Blocking** | High | Allow users to block other users |
| **Content Reporting** | High | Report inappropriate content/users |
| **Email Notifications** | Medium | Digest emails, new follower notifications |
| **Push Notifications** | Medium | Mobile push via FCM/APNs |
| **User Mentions** | Medium | @username mentions in comments |
| **Hashtags** | Medium | Clickable hashtags for discovery |

### 📊 Content & Discovery

| Feature | Priority | Description |
|---------|----------|-------------|
| **Pagination** | High | Cursor-based pagination for feeds (more efficient than offset) |
| **Trending Feed** | High | Algorithm-based trending artworks |
| **Bookmarks/Saves** | Medium | Save artworks for later viewing |
| **Collections** | Medium | Organize saved artworks into collections |
| **Content Moderation** | Medium | AI-based NSFW detection, manual review queue |
| **Full-Text Search** | Medium | Elasticsearch for advanced search capabilities |
| **Recommendations** | Low | ML-based artwork recommendations |

### 🏗️ Infrastructure & Scalability

| Feature | Priority | Description |
|---------|----------|-------------|
| **Redis Caching** | High | Cache user sessions, rate limits, hot data |
| **CDN for Images** | High | CloudFront/Cloudflare for image delivery |
| **Object Storage** | High | S3/GCS for image storage instead of local filesystem |
| **Message Queue** | Medium | RabbitMQ/SQS for async processing (emails, notifications) |
| **Database Read Replicas** | Medium | Separate read/write for scaling |
| **Connection Pooling** | Medium | PgBouncer for PostgreSQL connection management |
| **Blue-Green Deployments** | Medium | Zero-downtime deployments |
| **Database Sharding** | Low | For extreme scale (millions of users) |

### 📈 Analytics & Monitoring

| Feature | Priority | Description |
|---------|----------|-------------|
| **User Analytics** | Medium | Track user engagement, popular content |
| **A/B Testing** | Low | Feature flagging and experimentation |
| **Business Metrics** | Medium | DAU/MAU, retention, engagement rates |
| **Custom Dashboards** | Low | Grafana dashboards for business KPIs |

### 🔧 Developer Experience

| Feature | Priority | Description |
|---------|----------|-------------|
| **API Documentation** | High | OpenAPI/Swagger documentation |
| **API Versioning** | High | URL or header-based versioning (v1, v2) |
| **Integration Tests** | High | Comprehensive test suite |
| **CI/CD Pipeline** | High | GitHub Actions/GitLab CI for automated testing and deployment |
| **GraphQL API** | Medium | Alternative to REST for flexible queries |
| **WebSocket Support** | Medium | Real-time updates for notifications |
| **SDK Generation** | Low | Auto-generated client SDKs |

### 📋 Compliance & Legal

| Feature | Priority | Description |
|---------|----------|-------------|
| **GDPR Compliance** | High | Data export, right to deletion |
| **Terms of Service** | High | Legal terms acceptance tracking |
| **Privacy Policy** | High | Cookie consent, data usage disclosure |
| **Audit Logging** | Medium | Track all admin and security events |
| **Data Retention** | Medium | Automated cleanup of old data |

### 🎨 Sample Implementation Roadmap

**Phase 1 - Core Production (2-4 weeks)**
1. Email verification and password reset
2. Pagination for all list endpoints
3. S3 image storage + CDN
4. Redis caching for rate limits
5. CI/CD pipeline with tests

**Phase 2 - User Engagement (4-6 weeks)**
1. Notifications system (in-app)
2. User blocking and content reporting
3. Trending/discover feed
4. Bookmarks feature
5. API documentation (Swagger)

**Phase 3 - Scale & Polish (6-8 weeks)**
1. Real-time notifications (WebSocket)
2. Direct messaging
3. Full-text search (Elasticsearch)
4. OAuth2 social login
5. Mobile push notifications

**Phase 4 - Growth (Ongoing)**
1. Recommendation engine
2. Analytics dashboard
3. A/B testing framework
4. GraphQL API
5. Internationalization (i18n)

### 📚 Recommended Libraries for Enhancements

```xml
<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Redis Caching -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- API Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- AWS S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.25.0</version>
</dependency>

<!-- Elasticsearch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

## License

This project is licensed under the MIT License.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
