# SchoolCompetitionWeb Project Guidelines

## Project Overview
SchoolCompetitionWeb is a web-based platform for managing school competitions, featuring a Vue 3 frontend and a Spring Boot backend.

## Tech Stack
### Frontend
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite
- **State Management**: Pinia (with `pinia-plugin-persistedstate`)
- **UI Component Library**: Element Plus
- **Icons**: Iconify (via `unplugin-icons`)
- **HTTP Client**: Axios
- **Utilities**: ECharts, Lodash, XLSX (Excel handling)
- **Router**: Vue Router

### Backend
- **Framework**: Spring Boot 4.x (using `spring-boot-starter-parent` 4.0.1)
- **Data Access**: Spring Data JPA
- **Database**: MySQL (`school_competition_db`)
- **API**: Spring WebMVC
- **Utilities**: Lombok, EasyExcel (Excel handling)
- **Build Tool**: Maven

## Core Mandates & Conventions

### 1. API Communication
- All frontend API calls **MUST** use the centralized axios instance in `src/utils/request.js`.
- The `baseURL` is configured to `http://localhost:8080/api`.
- Request/Response interceptors handle authentication tokens and standardized error messaging.

### 2. Backend Architecture
- **Entities**: Located in `backend/SchoolCompetition/src/main/java/com/tfgkk/schoolcompetition/entity/`. Use JPA annotations and Lombok.
- **Repositories**: Located in `backend/SchoolCompetition/src/main/java/com/tfgkk/schoolcompetition/repository/`.
- **Controllers**: Located in `backend/SchoolCompetition/src/main/java/com/tfgkk/schoolcompetition/controller/`. API endpoints should start with `/api`.
- **CORS**: Global CORS configuration is handled in `CorsConfig.java`.

### 3. Frontend Architecture
- **Views**: Located in `src/view/` (AdminView, StudentView, LoginView).
- **Stores**: Located in `src/stores/`.
- **Router**: Routes are defined in `src/router/index.js`.
- **API Modules**: Domain-specific API functions should be kept in `src/api/` (e.g., `user.js`, `competition.js`).

### 4. Database & Persistence
- The database is MySQL, name: `school_competition_db`.
- Hibernate `ddl-auto` is set to `update` for development.
- Database credentials and connection details are in `backend/SchoolCompetition/src/main/resources/application.yml`.

### 5. Security & Credentials
- **NEVER** commit or log sensitive information (e.g., the MySQL password found in `application.yml`).
- Use the standard Authorization header format: `Bearer <token>`.

### 6. Development Workflow
- **Frontend**: `npm run dev` to start the Vite development server.
- **Backend**: Run the Spring Boot application via Maven or an IDE. Ensure the MySQL server is running and the database exists.
- **Excel Handling**: Use `xlsx` on the frontend and `EasyExcel` on the backend for consistent data import/export.

## Coding Standards
- **Surgical Updates**: Make targeted changes. Avoid large-scale refactoring unless explicitly requested.
- **Idiomatic Code**: Follow Vue 3 and Spring Boot best practices. Use explicit types and maintain structural integrity.
- **No Hacks**: Do not suppress warnings or bypass type systems.
- **Validation**: Every change must be verified. Add tests to `backend/SchoolCompetition/src/test/` or create reproduction scripts for frontend changes.
