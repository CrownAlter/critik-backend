# Critik API - Comprehensive Documentation

## OpenAPI / Swagger UI

This project also exposes interactive API documentation generated from the Spring controllers:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

For secured endpoints, obtain an access token via `POST /auth/login` and then authorize in Swagger UI with:

`Bearer <access_token>`


**Version:** 1.0.0  
**Last Updated:** 2026-01-11  
**Base URL:** `http://localhost:8080`

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
   - [Auth Endpoints](#auth-endpoints)
   - [Artwork Endpoints](#artwork-endpoints)
   - [Comment Endpoints](#comment-endpoints)
   - [Reaction Endpoints](#reaction-endpoints)
   - [Follow Endpoints](#follow-endpoints)
   - [Profile Endpoints](#profile-endpoints)
   - [Search Endpoints](#search-endpoints)
4. [Data Models](#data-models)
5. [Error Handling](#error-handling)
6. [Security Features](#security-features)
7. [Test Results](#test-results)
8. [Known Issues](#known-issues)

---

## Overview

Critik is an art review and discussion platform that allows users to:
- Share artwork with interpretations
- Comment and reply to discussions
- React to artworks (Agree/Disagree)
- Follow other users
- Search for users and artworks

### Technology Stack
- **Backend:** Spring Boot 3.5.4
- **Database:** PostgreSQL 18
- **Authentication:** JWT (JSON Web Tokens)
- **Java Version:** 21

---

## Authentication

### JWT Token Flow

1. **Register** - Create a new account
2. **Login** - Receive access token (15 min) + refresh token (7 days)
3. **Use Access Token** - Include in Authorization header for protected endpoints
4. **Refresh Token** - Get new access token when expired
5. **Logout** - Revoke refresh token

### Authorization Header Format
```
Authorization: Bearer <access_token>
```

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@#$%^&+=!)

---

## API Endpoints

### Auth Endpoints

#### POST /auth/register
Register a new user account.

**Authentication:** None required

**Request Body:**
```json
{
  "username": "string (3-50 chars)",
  "email": "string (valid email)",
  "password": "string (8+ chars, see requirements)"
}
```

**Response (200):**
```json
{
  "message": "User registered successfully"
}
```

**Errors:**
- `400` - Validation error (invalid email, weak password, etc.)
- `409` - Username or email already exists

---

#### POST /auth/login
Authenticate and receive tokens.

**Authentication:** None required

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response (200):**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "username": "string"
}
```

**Errors:**
- `401` - Invalid credentials

---

#### POST /auth/refresh
Refresh access token using refresh token.

**Authentication:** None required

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response (200):**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Errors:**
- `400/500` - Invalid or expired refresh token

---

#### POST /auth/logout
Revoke a specific refresh token (single device logout).

**Authentication:** None required

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response (200):**
```json
{
  "message": "Logged out successfully"
}
```

---

#### POST /auth/logout-all
Revoke all refresh tokens for the user (logout from all devices).

**Authentication:** Required

**Response (200):**
```json
{
  "message": "Logged out from all devices successfully"
}
```

**Status:** ⚠️ Known issue - may return 500 error

---

### Artwork Endpoints

#### GET /artworks/feed
Get public feed of all artworks.

**Authentication:** None required

**Response (200):**
```json
[
  {
    "id": 1,
    "title": "string",
    "artistName": "string",
    "imageUrl": "/uploads/uuid_filename.jpg",
    "locationName": "string",
    "locationLat": 0.0,
    "locationLon": 0.0,
    "interpretation": "string",
    "tags": "tag1,tag2,tag3",
    "createdAt": "2026-01-11T12:00:00",
    "updatedAt": "2026-01-11T12:00:00",
    "user": { "id": 1, "username": "string" }
  }
]
```

---

#### GET /artworks/feed/{userId}
Get personalized feed for a user (artworks from followed users).

**Authentication:** None required

**Path Parameters:**
- `userId` - User ID

**Response (200):** Array of artworks (same format as public feed)

---

#### GET /artworks/my
Get current user's artworks.

**Authentication:** Required

**Response (200):** Array of artworks

---

#### GET /artworks/{artworkId}
Get a single artwork by ID.

**Authentication:** None required

**Path Parameters:**
- `artworkId` - Artwork ID

**Response (200):** Single artwork object

**Errors:**
- `404` - Artwork not found

---

#### POST /artworks
Upload a new artwork.

**Authentication:** Required

**Content-Type:** `multipart/form-data`

**Form Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| file | File | Yes | Image file (jpg, jpeg, png, gif, webp) |
| title | String | Yes | Artwork title (max 200 chars) |
| artistName | String | No | Original artist name |
| locationName | String | No | Location name |
| lat | Double | No | Latitude coordinate |
| lon | Double | No | Longitude coordinate |
| interpretation | String | No | User's interpretation (max 5000 chars) |
| tags | String | No | Comma-separated tags |

**Response (201):** Created artwork object

**Errors:**
- `400` - Invalid file type or missing required fields

---

#### PUT /artworks/{artworkId}
Update an existing artwork.

**Authentication:** Required (owner only)

**Path Parameters:**
- `artworkId` - Artwork ID

**Request Body:**
```json
{
  "title": "string (optional)",
  "artistName": "string (optional)",
  "locationName": "string (optional)",
  "lat": 0.0,
  "lon": 0.0,
  "interpretation": "string (optional)",
  "tags": "string (optional)"
}
```

**Response (200):** Updated artwork object

**Errors:**
- `403` - Not the artwork owner
- `404` - Artwork not found

---

#### DELETE /artworks/{artworkId}
Delete an artwork (cascade deletes comments and reactions).

**Authentication:** Required (owner only)

**Path Parameters:**
- `artworkId` - Artwork ID

**Response (200):**
```json
{
  "message": "Artwork deleted successfully"
}
```

**Errors:**
- `403` - Not the artwork owner
- `404` - Artwork not found

---

### Comment Endpoints

#### GET /artworks/{artworkId}/comments
Get all comments for an artwork (tree structure with replies).

**Authentication:** None required

**Path Parameters:**
- `artworkId` - Artwork ID

**Response (200):**
```json
[
  {
    "id": 1,
    "commentText": "string",
    "createdAt": "2026-01-11T12:00:00",
    "user": { "id": 1, "username": "string" },
    "replies": [
      {
        "id": 2,
        "commentText": "string",
        "createdAt": "2026-01-11T12:01:00",
        "user": { "id": 2, "username": "string" },
        "replies": []
      }
    ]
  }
]
```

---

#### POST /artworks/{artworkId}/comments
Add a top-level comment to an artwork.

**Authentication:** Required

**Path Parameters:**
- `artworkId` - Artwork ID

**Request Body:**
```json
{
  "text": "string (max 2000 chars)"
}
```

**Response (201):** Created comment object

---

#### POST /artworks/{artworkId}/comments/{commentId}/replies
Reply to an existing comment.

**Authentication:** Required

**Path Parameters:**
- `artworkId` - Artwork ID
- `commentId` - Parent comment ID

**Request Body:**
```json
{
  "text": "string (max 2000 chars)"
}
```

**Response (201):** Created reply object

---

#### DELETE /artworks/{artworkId}/comments/{commentId}
Delete a comment (cascade deletes all replies).

**Authentication:** Required (owner only)

**Path Parameters:**
- `artworkId` - Artwork ID
- `commentId` - Comment ID

**Response (200):**
```json
{
  "message": "Comment deleted successfully"
}
```

**Errors:**
- `403` - Not the comment owner
- `404` - Comment not found

---

### Reaction Endpoints

#### GET /artworks/{artworkId}/reactions
Get reaction counts for an artwork.

**Authentication:** None required

**Path Parameters:**
- `artworkId` - Artwork ID

**Response (200):**
```json
{
  "AGREE": 5,
  "DISAGREE": 2
}
```

---

#### GET /artworks/{artworkId}/reactions/me
Get current user's reaction to an artwork.

**Authentication:** Required

**Path Parameters:**
- `artworkId` - Artwork ID

**Response (200):**
```json
{
  "hasReaction": true,
  "type": "AGREE"
}
```

---

#### POST /artworks/{artworkId}/reactions
Set or change reaction to an artwork.

**Authentication:** Required

**Path Parameters:**
- `artworkId` - Artwork ID

**Query Parameters:**
- `type` - Reaction type: `AGREE` or `DISAGREE`

**Response (200):**
```json
{
  "message": "Reaction saved",
  "type": "AGREE"
}
```

---

#### DELETE /artworks/{artworkId}/reactions
Remove reaction from an artwork.

**Authentication:** Required

**Path Parameters:**
- `artworkId` - Artwork ID

**Response (200):**
```json
{
  "message": "Reaction removed"
}
```

---

### Follow Endpoints

#### POST /follow/{userId}
Follow a user.

**Authentication:** Required

**Path Parameters:**
- `userId` - User ID to follow

**Response (200):**
```json
{
  "message": "Followed successfully"
}
```

**Errors:**
- `400` - Cannot follow yourself
- `409` - Already following this user
- `404` - User not found

---

#### DELETE /follow/{userId}
Unfollow a user.

**Authentication:** Required

**Path Parameters:**
- `userId` - User ID to unfollow

**Response (200):**
```json
{
  "message": "Unfollowed successfully"
}
```

---

#### GET /follow/{userId}/followers
Get a user's followers.

**Authentication:** Required

**Path Parameters:**
- `userId` - User ID

**Response (200):**
```json
[
  {
    "id": 1,
    "username": "string",
    "displayName": "string",
    "email": "string",
    "bio": "string"
  }
]
```

---

#### GET /follow/{userId}/following
Get users that a user follows.

**Authentication:** Required

**Path Parameters:**
- `userId` - User ID

**Response (200):** Array of user objects

---

### Profile Endpoints

#### GET /users/{username}
Get a user's public profile.

**Authentication:** None required

**Path Parameters:**
- `username` - Username

**Response (200):**
```json
{
  "user": {
    "id": 1,
    "username": "string",
    "displayName": "string",
    "email": "string",
    "bio": "string"
  },
  "artworks": [],
  "isFollowing": false
}
```

**Errors:**
- `404` - User not found

---

#### PUT /users/{id}/edit
Update user profile.

**Authentication:** Required (owner only)

**Path Parameters:**
- `id` - User ID

**Request Body:**
```json
{
  "displayName": "string (optional, max 100 chars)",
  "email": "string (optional, valid email)",
  "bio": "string (optional, max 500 chars)"
}
```

**Response (200):**
```json
{
  "id": 1,
  "username": "string",
  "displayName": "string",
  "email": "string",
  "bio": "string"
}
```

**Errors:**
- `403` - Not the profile owner
- `409` - Email already taken

---

### Search Endpoints

#### GET /search/users
Search for users by username or display name.

**Authentication:** None required

**Query Parameters:**
- `q` - Search query

**Response (200):** Array of user objects

---

#### GET /search/artworks
Search for artworks by title, location, or tags.

**Authentication:** None required

**Query Parameters (all optional):**
- `title` - Search in artwork titles
- `location` - Search in location names
- `tags` - Search in tags

**Response (200):** Array of artwork objects

---

## Data Models

### User
```json
{
  "id": "Long",
  "username": "String (3-50 chars, unique)",
  "displayName": "String (max 100 chars)",
  "email": "String (unique, valid format)",
  "bio": "String (max 500 chars)"
}
```

### Artwork
```json
{
  "id": "Long",
  "title": "String (required, max 200 chars)",
  "artistName": "String (max 200 chars)",
  "imageUrl": "String",
  "locationLat": "Double",
  "locationLon": "Double",
  "locationName": "String (max 300 chars)",
  "interpretation": "String (max 5000 chars)",
  "tags": "String (comma-separated, max 500 chars)",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime",
  "user": "User"
}
```

### Comment
```json
{
  "id": "Long",
  "commentText": "String (required, max 2000 chars)",
  "createdAt": "LocalDateTime",
  "user": "User",
  "artwork": "Artwork",
  "parentComment": "Comment (nullable)",
  "replies": "List<Comment>"
}
```

### Reaction
```json
{
  "id": "Long",
  "type": "ReactionType (AGREE | DISAGREE)",
  "createdAt": "LocalDateTime",
  "user": "User",
  "artwork": "Artwork"
}
```

### Follow
```json
{
  "id": "Long",
  "follower": "User",
  "followed": "User",
  "createdAt": "LocalDateTime"
}
```

---

## Error Handling

### Standard Error Response
```json
{
  "timestamp": "2026-01-11T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message"
}
```

### HTTP Status Codes
| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Access denied |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Duplicate resource |
| 413 | Payload Too Large - File too big |
| 500 | Internal Server Error |

---

## Security Features

### Authentication & Authorization
- ✅ JWT-based stateless authentication
- ✅ Short-lived access tokens (15 minutes)
- ✅ Long-lived refresh tokens (7 days) with rotation
- ✅ BCrypt password hashing (strength 12)
- ✅ Owner-only resource modification

### Input Validation
- ✅ Password strength requirements
- ✅ Email format validation
- ✅ Field length limits
- ✅ File type validation for uploads

### Rate Limiting
- ✅ Login: 5 attempts per minute
- ✅ Register: 3 attempts per minute
- ✅ API: 60 requests per minute
- ✅ Search: 30 requests per minute

### Other Security
- ✅ CSRF disabled (stateless JWT)
- ✅ CORS configured
- ✅ Secure file upload with UUID naming
- ✅ SQL injection prevention (JPA)

---

## Test Results

### Summary (2026-01-11)
| Metric | Value |
|--------|-------|
| Total Tests | 46 |
| Passed | 45 |
| Failed | 1 |
| Success Rate | **97.83%** |

### Results by Category
| Category | Passed | Failed |
|----------|--------|--------|
| Authentication | 8 | 1 |
| Artwork | 9 | 0 |
| Comment | 5 | 0 |
| Reaction | 6 | 0 |
| Follow | 6 | 0 |
| Profile | 4 | 0 |
| Search | 5 | 0 |
| Security | 2 | 0 |

### Endpoints Tested
| # | Method | Endpoint | Status |
|---|--------|----------|--------|
| 1 | POST | /auth/register | ✅ Pass |
| 2 | POST | /auth/login | ✅ Pass |
| 3 | POST | /auth/refresh | ✅ Pass |
| 4 | POST | /auth/logout | ✅ Pass |
| 5 | POST | /auth/logout-all | ⚠️ Issue |
| 6 | GET | /artworks/feed | ✅ Pass |
| 7 | GET | /artworks/feed/{userId} | ✅ Pass |
| 8 | GET | /artworks/my | ✅ Pass |
| 9 | GET | /artworks/{id} | ✅ Pass |
| 10 | POST | /artworks | ✅ Pass |
| 11 | PUT | /artworks/{id} | ✅ Pass |
| 12 | DELETE | /artworks/{id} | ✅ Pass |
| 13 | GET | /artworks/{id}/comments | ✅ Pass |
| 14 | POST | /artworks/{id}/comments | ✅ Pass |
| 15 | POST | /artworks/{id}/comments/{id}/replies | ✅ Pass |
| 16 | DELETE | /artworks/{id}/comments/{id} | ✅ Pass |
| 17 | GET | /artworks/{id}/reactions | ✅ Pass |
| 18 | GET | /artworks/{id}/reactions/me | ✅ Pass |
| 19 | POST | /artworks/{id}/reactions | ✅ Pass |
| 20 | DELETE | /artworks/{id}/reactions | ✅ Pass |
| 21 | POST | /follow/{userId} | ✅ Pass |
| 22 | DELETE | /follow/{userId} | ✅ Pass |
| 23 | GET | /follow/{userId}/followers | ✅ Pass |
| 24 | GET | /follow/{userId}/following | ✅ Pass |
| 25 | GET | /users/{username} | ✅ Pass |
| 26 | PUT | /users/{id}/edit | ✅ Pass |
| 27 | GET | /search/users | ✅ Pass |
| 28 | GET | /search/artworks | ✅ Pass |

---

## Known Issues

### 1. POST /auth/logout-all - Returns 500 Error
**Severity:** Medium  
**Impact:** Users cannot logout from all devices at once  
**Workaround:** Use single device logout (`POST /auth/logout`)

**Root Cause:** Transaction management issue with bulk update query on refresh tokens.

**Attempted Fixes:**
- Changed query to use user.id instead of User entity
- Added @Modifying(clearAutomatically = true)
- Added @Transactional to service method
- Changed RefreshToken entity to EAGER fetch

**Status:** Under investigation. Single logout works correctly.

---

## Rate Limiting

Rate limit headers are included in responses:
- `X-Rate-Limit-Remaining` - Requests remaining
- `X-Rate-Limit-Retry-After-Seconds` - Wait time if limit exceeded

---

## CORS Configuration

Default allowed origin: `http://localhost:4200`

Configure via environment variable:
```
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com
```

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| JWT_SECRET | Secret key for JWT signing (min 32 chars) | Required |
| JWT_EXPIRATION | Access token expiration (ms) | 900000 (15 min) |
| JWT_REFRESH_EXPIRATION | Refresh token expiration (ms) | 604800000 (7 days) |
| DB_URL | Database connection URL | jdbc:postgresql://localhost:5432/critik |
| DB_USERNAME | Database username | critik |
| DB_PASSWORD | Database password | Required |
| FILE_UPLOAD_DIR | Upload directory | uploads |
| MAX_FILE_SIZE | Max upload size | 10MB |
| CORS_ALLOWED_ORIGINS | Allowed CORS origins | http://localhost:4200 |
| PORT | Server port | 8080 |

---

## Health Check

**Endpoint:** `GET /actuator/health`

**Response:**
```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}
```

---

*Documentation generated on 2026-01-11*
