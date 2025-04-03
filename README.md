# Scala Play 2 Auth Service

This is a **fully Dockerized authentication service** built with **Scala 2.13** using **Play Framework 2**. It efficiently handles user session management and token issuance with **JWT** and **Redis**, alongside a PostgreSQL database for user persistence.

## Features

-   **Session Management & Authentication:** Handles user sessions efficiently with **JWT** and **Redis**. Users can log in and perform authenticated actions. JWT tokens are **valid for 15 minutes** and are **renewed the Redis session is still valid upon expiration**.
-   **API Endpoints**: Basic authentication endpoints are implemented. **Swagger documentation** is **coming soon** for better API documentation.
-   **Environment Configuration**: Using a HOCON file, it supports environment-based configurations for sensitive keys and URLs.
-   **Dockerized**: Runs smoothly with **Docker Compose**, leveraging **Redis** and **PostgreSQL** for optimal performance.

## Technologies Used

-   **Scala 2.13**
-   **Play Framework 2**
-   **JWT** for token-based authentication
-   **Redis** for session management
-   **PostgreSQL** for database storage
-   **Docker & Docker Compose** for containerization
-   **Slick** for database interaction

## Setup

### Prerequisites

-   Docker and Docker Compose installed.
-   Redis and PostgreSQL running through Docker Compose.

### Installation

1.  Clone the repository:

    ```bash
    git clone [https://github.com/SalimOfShadow/scala-backend/](https://github.com/SalimOfShadow/scala-backend/)
    cd scala-backend
    ```

2.  Set environment variables:

    Before running Docker Compose, ensure you have set the necessary environment variables. You can do this by creating a `.env` file in the project root or by exporting them directly in your terminal. Example `.env` file content:

    ```env
    APPLICATION_SECRET=your_application_secret
    JWT_SECRET=your_jwt_secret
    REDIS_SECRET=your_redis_secret
    DB_URL=jdbc:postgresql://postgres:5432/your_database_name?user=your_db_user&password=your_db_password
    APPLICATION_ENV=development / production
    ```

    Replace the placeholder values with your actual secrets and database credentials.

3.  Build and run the application with Docker Compose:

    ```bash
    docker-compose up --build
    ```

4.  This will start the application, Redis, and PostgreSQL services as defined in the `docker-compose.yml` file.

### Configuration

-   **CORS Configuration**:
    CORS is enabled to allow requests from `http://localhost:3000`. This can be adjusted via the `application.conf` file.

-   **Session Management**:
    -   JWT tokens **expire after 15 minutes**.
    -   The JWT token is **renewed if the Redis session is still valid** to ensure smooth user experience.
    -   Session duration in Redis is set to **one week**.

-   **Database**: The application uses **PostgreSQL** for storing user data, with database credentials configured in `application.conf`.

-   **Redis**: Redis handles session data, storing session information for up to **one week**.

-   **Environment Variables**:
    -   `APPLICATION_SECRET`, `DB_URL`, `JWT_SECRET`, `REDIS_SECRET`, and `APPLICATION_ENV` can be set as environment variables to configure the application.

## API Endpoints

The service currently supports user register,login and authentication features. **Swagger documentation is coming soon** to provide detailed information on the available endpoints.

### JWT Authentication

-   Tokens are issued upon successful user login.
-   Tokens are stored in Redis and are valid for **15 minutes**.
-   A new token will be issued upon request if the session is valid in Redis.

## Future Improvements

-   **SMTP Support**: Email functionality for features like password recovery will be added in future versions.
-   **Swagger Documentation**: API documentation will be integrated with Swagger to make it easier for developers to interact with the service.
