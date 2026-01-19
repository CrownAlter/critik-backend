# Deploying Critik to Render

This guide outlines the steps to deploy the Critik backend and database to [Render.com](https://render.com) using the Infrastructure as Code (Blueprints) configuration included in this repository.

## Prerequisites

1.  A [Render](https://render.com) account.
2.  Code pushed to a GitHub or GitLab repository connected to your Render account.

## Deployment Method: Blueprints (Recommended)

The easiest way to deploy is using the `render.yaml` Blueprint file. This automatically provisions the database and web service and links them together.

1.  **Log in to Render Dashboard**.
2.  Click **New +** and select **Blueprint**.
3.  Connect your `critik-backend` repository.
4.  Render will automatically detect the `render.yaml` file.
5.  **Review the Service Group Name** (defaults to repo name).
6.  Click **Apply**.

Render will now:
-   Create a **PostgreSQL Database** (`critik-db`).
-   Build the Docker image for `critik-backend` using the `Dockerfile`.
-   Automatically inject the `DB_URL` and generate a `JWT_SECRET`.
-   Deploy the service.

## Configuration Details

### Services Created
-   **Critik Backend**: The Spring Boot application running in a Docker container (Java 21).
-   **Critik DB**: A fully managed PostgreSQL 16 database.

### Environment Variables
These are automatically configured by `render.yaml`:

| Variable | Value | Description |
| :--- | :--- | :--- |
| `jwt.secret` | (Generated) | Secure random key for signing JWTs. |
| `spring.datasource.url` | (Auto-linked) | Connection string to the internal Postgres DB. |
| `server.port` | `8080` | Port the app listens on. |
| `SPRING_PROFILES_ACTIVE`| `production` | Activates production settings. |

## Manual Deployment (Alternative)

If you prefer to deploy manually without Blueprints:

1.  **Create Database**:
    -   New -> PostgreSQL.
    -   Name: `critik-db`.
    -   Copy the **Internal Connection String**.

2.  **Create Web Service**:
    -   New -> Web Service.
    -   Connect repo.
    -   Runtime: **Docker**.
    -   Environment Variables:
        -   `DB_URL`: Paste the Internal Connection String from step 1.
        -   `DB_USERNAME`: `critik` (or your chosen user).
        -   `DB_PASSWORD`: (db password).
        -   `JWT_SECRET`: (Generate a secure random string).
        -   `PORT`: `8080`.

## Troubleshooting

-   **Build Failures**: Check the logs in the Render dashboard. Ensure the `Dockerfile` build stage passes.
-   **Database Connection**: Ensure the `DB_URL` environment variable is correctly set if deploying manually. Blueprints handle this automatically.
