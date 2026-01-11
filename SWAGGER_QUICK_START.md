# 🚀 Swagger API Documentation - Quick Start Guide

## ⚡ 3-Step Setup

### Step 1: Start the Application
```bash
mvn spring-boot:run
```

### Step 2: Open Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### Step 3: Start Testing!
- Browse all available endpoints organized by category
- Try endpoints directly in your browser
- No additional tools needed!

---

## 🔐 Testing Protected Endpoints

Most endpoints require authentication. Here's how:

### 1. Register a User
- Open **Authentication** section
- Click `POST /auth/register`
- Click **"Try it out"**
- Enter:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "SecurePass123!"
}
```
- Click **"Execute"**

### 2. Login
- Click `POST /auth/login`
- Click **"Try it out"**
- Enter:
```json
{
  "username": "testuser",
  "password": "SecurePass123!"
}
```
- Click **"Execute"**
- **Copy the `accessToken` from the response**

### 3. Authorize
- Click the **"Authorize"** button (🔓 icon at top right)
- In the popup, enter: `Bearer <paste-your-token-here>`
- Click **"Authorize"**
- Click **"Close"**
- The lock icon should now be closed (🔒)

### 4. Test Protected Endpoints
Now you can test any endpoint! Try:
- Upload an artwork: `POST /artworks`
- Add a comment: `POST /artworks/{artworkId}/comments`
- Follow a user: `POST /follow/{userId}`
- Update your profile: `PUT /users/{id}/edit`

---

## 📚 API Categories

### 🔑 Authentication
- Register, Login, Refresh Token, Logout

### 🎨 Artworks
- Upload, View, Update, Delete artworks
- Public and personalized feeds

### 💬 Comments
- Add comments and nested replies
- View comment trees

### 👍 Reactions
- AGREE or DISAGREE with artworks
- View reaction counts

### 👥 Follow
- Follow/Unfollow users
- View followers and following lists

### 👤 Profile
- View public profiles
- Update profile information

### 🔍 Search
- Search users by username
- Search artworks by title, location, tags

---

## 🎯 Quick Tips

✅ **No Authentication Needed:**
- View public artwork feed
- View artwork details
- View comments and reactions
- Search users and artworks
- View user profiles

🔒 **Authentication Required:**
- Upload artworks
- Add comments
- Set reactions
- Follow users
- Update profile

💡 **Pro Tips:**
- Click any endpoint to see request/response examples
- Use "Try it out" to test immediately
- Check response codes to understand API behavior
- Access token expires in 15 minutes - use refresh token to get a new one

---

## 🌐 Access URLs

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html |
| **API Docs (JSON)** | http://localhost:8080/v3/api-docs |
| **API Docs (YAML)** | http://localhost:8080/v3/api-docs.yaml |

---

## 📖 Need More Info?

- **Detailed Guide:** See `SWAGGER_DOCUMENTATION.md`
- **Implementation Details:** See `SWAGGER_IMPLEMENTATION_SUMMARY.md`
- **API Documentation (Markdown):** See `API_DOCUMENTATION.md`

---

**Happy Testing! 🎉**
