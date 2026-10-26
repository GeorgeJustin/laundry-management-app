# Laundry Management Application

A full-stack Laundry Management Application built as a hands-on learning project using Java, Spring Boot, PostgreSQL, React, and Vite.

The goal is to develop a practical laundry service management system while following clean project structure, REST API development, database integration, and frontend-backend communication.

## Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Data JPA
- REST APIs
- Maven
- PostgreSQL

### Frontend
- React
- Vite
- JavaScript
- HTML and CSS

## Project Structure

```text
LaundryManagementApp/
├── Backend/
│   └── demo/
│       ├── src/
│       ├── pom.xml
│       └── mvnw.cmd
├── Frontend/
│   ├── src/
│   ├── package.json
│   └── package-lock.json
├── .gitignore
└── README.md
```

The backend and frontend are maintained in separate folders so that each application can be developed and run independently.

## Current Features

### Backend
- Laundry service CRUD operations.
- REST API endpoints for creating, retrieving, updating, and deleting services.
- PostgreSQL database integration and data persistence.
- Pagination and sorting for laundry services.
- Customer management APIs.
- Input validation and API error handling.

### Frontend
- React-based laundry management interface.
- Laundry service listing.
- Search and sorting of services.
- Add, edit, and delete service operations.
- Integration with backend REST APIs.
- Loading indicators, success messages, and error feedback.

## Prerequisites

Install the following before running the application:

- Java 21
- PostgreSQL
- Node.js and npm
- Git

## Database Configuration

1. Start PostgreSQL.
2. Create a database named `laundry_db`.
3. Configure your database credentials using environment variables.

The backend uses these environment variables:

- `DB_USERNAME` — your PostgreSQL username
- `DB_PASSWORD` — your PostgreSQL password

Never commit real database passwords or other secrets to GitHub.

## Run the Backend

Open PowerShell and navigate to the backend project:

```powershell
cd D:\LaundryManagementApp\Backend\demo
```

Set your database credentials for the current PowerShell session:

```powershell
$env:DB_USERNAME = "your_postgres_username"
$env:DB_PASSWORD = "your_postgres_password"
```

Replace the example values with your local PostgreSQL credentials.

Start the Spring Boot application:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend is configured to run at:

`http://localhost:8081`

Test the laundry services endpoint:

`http://localhost:8081/api/services`

## Run the Frontend

Open a separate terminal:

```powershell
cd D:\LaundryManagementApp\Frontend
```

Install the frontend dependencies:

```powershell
npm install
```

Start the Vite development server:

```powershell
npm run dev
```

Open the local frontend URL shown in the terminal. The default Vite URL is:

`http://localhost:5173`

Keep both the backend and frontend running while testing the application.

## Development Goals

This project is being developed incrementally, with a focus on practical full-stack development.

Planned improvements include:

- Customer-facing laundry service selection.
- Adding multiple services to a cart.
- Pickup and delivery scheduling.
- Creating a single order ID for each order.
- Order tracking and status management.
- Improving validation, testing, and application security.

These features will be added as development progresses.

## Feedback and Contributions

This is a learning project, and constructive feedback is welcome.

Suggestions for improving code quality, application architecture, REST API design, database design, testing, security, and user experience are appreciated.

## Author

GeorgeJustin

GitHub: https://github.com/GeorgeJustin