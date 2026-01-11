# Swagger API Documentation

## Overview

The Critik API now includes comprehensive Swagger/OpenAPI documentation for easy API exploration and testing.

## Accessing Swagger UI

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

Or simply:

```
http://localhost:8080/swagger-ui.html
```

## Accessing OpenAPI Specification

The OpenAPI 3.0 specification (JSON format) is available at:

```
http://localhost:8080/v3/api-docs
```

For YAML format:

```
http://localhost:8080/v3/api-docs.yaml
```

## Features

### 1. **Interactive API Testing**
   - Test all API endpoints directly from the browser
   - No need for external tools like Postman
   - See request/response examples in real-time

### 2. **JWT Authentication Support**
   - Use the "Authorize" button at the top right
   - Enter your JWT token in the format: `Bearer <your-token>`
   - All authenticated endpoints will automatically include the token

### 3. **Comprehensive Documentation**
   - All endpoints organized by tags (Authentication, Artworks, Comments, etc.)
   - Request/response schemas with examples
   - Parameter descriptions and validation rules
   - HTTP status codes and error responses

### 4. **API Tags/Groups**
   - **Authentication**: User registration, login, token refresh, logout
   - **Artworks**: Upload, view, update, delete artworks and feeds
   - **Comments**: Add comments, replies, and manage discussions
   - **Reactions**: AGREE/DISAGREE reactions on artworks
   - **Follow**: Follow/unfollow users, view followers/following
   - **Profile**: View and update user profiles
   - **Search**: Search for users and artworks

## How to Use

### Step 1: Start the Application
```bash
mvn spring-boot:run
```

### Step 2: Open Swagger UI
Navigate to `http://localhost:8080/swagger-ui/index.html` in your browser

### Step 3: Authenticate (for protected endpoints)

1. First, register or login using the **Authentication** endpoints
2. Copy the `accessToken` from the response
3. Click the **"Authorize"** button at the top of the page
4. Enter: `Bearer <paste-your-token-here>`
5. Click **"Authorize"** and then **"Close"**

### Step 4: Test Endpoints

1. Click on any endpoint to expand it
2. Click **"Try it out"** button
3. Fill in the required parameters
4. Click **"Execute"**
5. View the response below

## Example: Testing the Complete Flow

### 1. Register a User
```
POST /auth/register
Body:
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "SecurePass123!"
}
```

### 2. Login
```
POST /auth/login
Body:
{
  "username": "testuser",
  "password": "SecurePass123!"
}
```

### 3. Authorize with Token
- Copy the `accessToken` from login response
- Click "Authorize" and enter: `Bearer <token>`

### 4. Upload Artwork
```
POST /artworks
(multipart/form-data)
- file: [select an image]
- title: "My Artwork"
- interpretation: "This represents..."
- tags: "abstract,modern"
```

### 5. View Public Feed
```
GET /artworks/feed
(No authentication required)
```

## Security Configuration

The following endpoints are **publicly accessible** (no authentication):
- `/auth/**` - Authentication endpoints
- `/swagger-ui/**` - Swagger UI
- `/v3/api-docs/**` - OpenAPI specification
- `GET /artworks/feed` - Public artwork feed
- `GET /artworks/{id}` - View artwork details
- `GET /artworks/{id}/comments` - View comments
- `GET /artworks/{id}/reactions` - View reactions
- `GET /search/**` - Search users and artworks
- `GET /users/{username}` - View user profiles

All other endpoints require JWT authentication.

## API Models/Schemas

All DTOs and entities include detailed schema information:
- **AuthRequest/AuthResponse**: Login credentials and tokens
- **UserDto**: User information
- **Artwork**: Artwork details with location and tags
- **Comment**: Comments with nested reply support
- **ProfileResponse**: User profile with artworks

## Production Deployment

When deploying to production:

1. **Update Server URL**: Modify `OpenApiConfig.java` to include your production URL
   ```java
   new Server()
       .url("https://api.yourproduction.com")
       .description("Production Server")
   ```

2. **Security Considerations**: 
   - In production, consider restricting Swagger UI access
   - Or remove it entirely and only expose the OpenAPI spec
   - You can disable Swagger UI with: `springdoc.swagger-ui.enabled=false`

3. **Configure via application.properties**:
   ```properties
   # Customize API documentation
   springdoc.api-docs.path=/api-docs
   springdoc.swagger-ui.path=/swagger-ui.html
   
   # Disable in production if needed
   springdoc.swagger-ui.enabled=true
   ```

## Customization

The Swagger configuration is located in:
```
src/main/java/com/application/critik/config/OpenApiConfig.java
```

You can customize:
- API title and description
- Contact information
- License
- Server URLs
- Security schemes

## Additional Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)

## Troubleshooting

### Swagger UI not loading?
- Ensure the application is running on port 8080
- Check that SecurityConfig allows `/swagger-ui/**` endpoints
- Clear browser cache

### Authentication not working?
- Make sure to include "Bearer " prefix before the token
- Check that the token hasn't expired (15 minutes for access tokens)
- Use `/auth/refresh` to get a new access token

### Endpoints not showing up?
- Check that controllers have `@Tag` annotations
- Ensure methods have `@Operation` annotations
- Restart the application after adding new endpoints

---

**Happy API Testing!** 🎨🚀
