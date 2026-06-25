# 🦷 Dental Clinic Management System

A production-ready **Spring Boot 3.x** backend for managing dental clinic operations — featuring JWT authentication, role-based access control, real AI/ML predictions using Weka, PostgreSQL persistence, Swagger documentation, scheduled tasks, and file management.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Overview](#api-overview)
- [AI Prediction Modules](#ai-prediction-modules)
- [Authentication Flow](#authentication-flow)
- [Password Reset Flow](#password-reset-flow)
- [Roles & Permissions](#roles--permissions)
- [Scheduler Jobs](#scheduler-jobs)
- [Swagger UI](#swagger-ui)
- [Database Schema](#database-schema)
- [Multi-Tenant Support](#multi-tenant-support)

---

## ✅ Features

| Feature | Details |
|---|---|
| **JWT Authentication** | Login, register, refresh token, email verification |
| **Role-Based Access** | ADMIN, DENTIST, RECEPTIONIST, PATIENT |
| **Password Reset** | Email link, OTP via email, OTP via SMS (placeholder) |
| **Patient Management** | CRUD, search, medical history |
| **Appointments** | Book, reschedule, track status with AI predictions |
| **Treatment Plans** | Multi-session plans with progress tracking |
| **Billing** | Invoice generation, payment tracking, overdue detection |
| **Prescriptions** | JSON medicine list, dosage instructions |
| **Doctor Notes** | Manual + AI-suggested clinical notes |
| **Reports** | File upload/download (PDF, images) |
| **Notifications** | In-app notification system with read/unread tracking |
| **AI Predictions** | 5 real ML models using Weka (LinearRegression, RandomForest, NaiveBayes) |
| **Swagger / OpenAPI 3** | Full API documentation at `/swagger-ui.html` |
| **Scheduler** | Appointment reminders, overdue bill detection, token cleanup, follow-up reminders |
| **Multi-Tenant** | Clinic isolation via `tenantId` |
| **Global Error Handling** | Standardized `ApiResponse<T>` for all endpoints |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Database | PostgreSQL + Spring Data JPA + Hibernate |
| AI / ML | Weka 3.8.6 (LinearRegression, RandomForest, NaiveBayes) |
| API Docs | Springdoc OpenAPI 3 (Swagger UI) |
| Email | Spring Boot Mail (JavaMailSender) |
| Scheduling | Spring `@Scheduled` |
| Build Tool | Maven |
| Java Version | Java 17 |
| Utilities | Lombok, Jackson, Commons IO |

---

## 📁 Project Structure

```
dental-clinic/
├── src/main/java/com/dental/clinic/
│   ├── DentalClinicApplication.java        # Entry point
│   ├── config/
│   │   ├── SecurityConfig.java             # JWT filter, CORS, RBAC
│   │   ├── OpenApiConfig.java              # Swagger / OpenAPI config
│   │   └── AppConfig.java                  # ObjectMapper, resource handlers
│   ├── controller/
│   │   ├── AuthController.java             # Login, register, forgot/reset password
│   │   ├── PatientController.java
│   │   ├── AppointmentController.java
│   │   ├── TreatmentPlanController.java
│   │   ├── BillController.java
│   │   ├── PrescriptionController.java
│   │   ├── DoctorNoteController.java
│   │   ├── ReportController.java
│   │   ├── NotificationController.java
│   │   ├── UserController.java
│   │   ├── ClinicController.java
│   │   └── AIPredictionController.java     # All AI prediction endpoints
│   ├── dto/
│   │   ├── request/                        # LoginRequest, RegisterRequest, etc.
│   │   └── response/                       # ApiResponse<T>, AuthResponse, etc.
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   ├── ConflictException.java
│   │   └── AIModelException.java
│   ├── model/                              # JPA Entities
│   │   ├── User.java
│   │   ├── Clinic.java
│   │   ├── Patient.java
│   │   ├── Appointment.java
│   │   ├── TreatmentPlan.java
│   │   ├── Report.java
│   │   ├── Notification.java
│   │   ├── Bill.java
│   │   ├── Prescription.java
│   │   ├── DoctorNote.java
│   │   └── PasswordResetToken.java
│   ├── repository/                         # Spring Data JPA Repositories
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── PatientService.java
│   │   ├── AppointmentService.java
│   │   ├── TreatmentPlanService.java
│   │   ├── BillService.java
│   │   ├── PrescriptionService.java
│   │   ├── DoctorNoteService.java
│   │   ├── ReportService.java
│   │   ├── NotificationService.java
│   │   ├── EmailService.java
│   │   ├── impl/
│   │   │   └── UserDetailsServiceImpl.java
│   │   └── ai/
│   │       └── AIModelService.java         # All 5 Weka ML models
│   ├── scheduler/
│   │   └── ClinicScheduler.java            # Scheduled background jobs
│   └── util/
│       └── JwtUtil.java
├── src/main/resources/
│   └── application.yml
├── training-data/                          # CSV training data for AI models
│   ├── queue_time_training.csv
│   ├── treatment_duration_training.csv
│   └── risk_assessment_training.csv
├── ai-models/                              # Serialized models (auto-generated at first startup)
│   ├── queue_time.model
│   ├── treatment_duration.model
│   ├── risk_assessment.model
│   ├── followup_prediction.model
│   └── doctor_note.model
├── uploads/                                # Patient report file storage
└── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### 1. Clone and configure

```bash
git clone https://github.com/your-org/dental-clinic.git
cd dental-clinic
```

### 2. Create PostgreSQL database

```sql
CREATE DATABASE dental_clinic;
CREATE USER dental_user WITH PASSWORD 'dental_pass';
GRANT ALL PRIVILEGES ON DATABASE dental_clinic TO dental_user;
```

### 3. Set environment variables

```bash
export DB_USERNAME=dental_user
export DB_PASSWORD=dental_pass
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

Or edit `src/main/resources/application.yml` directly.

### 4. Build and run

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

The application will start on **http://localhost:8080/api**

**On first startup**, the AI service will automatically:
1. Generate synthetic training data (500–600 samples per model)
2. Train all 5 Weka ML models
3. Serialize them to `./ai-models/`

Subsequent startups load models from disk (much faster).

---

## 🔐 Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | — | Email sender address |
| `MAIL_PASSWORD` | — | Email app password |
| `JWT_SECRET` | (built-in) | 256-bit Base64 secret for JWT signing |
| `FILE_UPLOAD_DIR` | `./uploads` | Directory for report file storage |
| `AI_MODELS_DIR` | `./ai-models` | Directory for serialized Weka models |
| `AI_TRAINING_DIR` | `./training-data` | Directory for CSV training data |
| `NOTIFICATION_FROM` | `noreply@dentalclinic.com` | Sender address for emails |

---

## 🌐 API Overview

All endpoints are prefixed with `/api`. Authentication required unless marked **public**.

### Authentication `/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Login, returns JWT |
| POST | `/auth/register` | Public | Register new user |
| GET | `/auth/verify-email?token=` | Public | Verify email |
| POST | `/auth/forgot-password` | Public | Request password reset |
| POST | `/auth/reset-password` | Public | Reset password |
| POST | `/auth/refresh-token` | Public | Refresh JWT |

### Patients `/patients`

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/patients` | ADMIN, DENTIST, RECEPTIONIST | All patients |
| GET | `/patients/{id}` | All | Patient by ID |
| GET | `/patients/search?name=` | ADMIN, DENTIST, RECEPTIONIST | Search |
| POST | `/patients/register` | Public | Self-registration |
| POST | `/patients` | ADMIN, RECEPTIONIST | Create |
| PUT | `/patients/{id}` | ADMIN, RECEPTIONIST, DENTIST | Update |
| DELETE | `/patients/{id}` | ADMIN | Delete |

### Appointments `/appointments`

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/appointments` | ADMIN, DENTIST, RECEPTIONIST | All |
| GET | `/appointments/upcoming` | ADMIN, DENTIST, RECEPTIONIST | Upcoming |
| GET | `/appointments/date-range` | ADMIN, DENTIST, RECEPTIONIST | By date range |
| POST | `/appointments/book` | ADMIN, RECEPTIONIST, PATIENT | Book (with AI predictions) |
| PATCH | `/appointments/{id}/status` | ADMIN, DENTIST, RECEPTIONIST | Update status |

### AI Predictions `/ai`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/ai/predict/queue-time` | Queue wait time (LinearRegression) |
| POST | `/ai/predict/treatment-duration` | Treatment duration (RandomForest) |
| POST | `/ai/predict/risk` | Patient risk level (NaiveBayes) |
| POST | `/ai/predict/follow-up` | Follow-up probability (RandomForest) |
| POST | `/ai/suggest/doctor-note` | AI clinical note suggestion (NaiveBayes) |
| GET | `/ai/status` | Model readiness status |

### Bills, Prescriptions, Reports, Notifications, Doctor Notes

Full CRUD available. See Swagger UI for complete documentation.

---

## 🤖 AI Prediction Modules

All models are implemented using the **Weka 3.8.6** library. No external AI services or APIs — fully self-contained Java ML.

### Model Architecture

| Prediction | Algorithm | Training Samples | Features |
|---|---|---|---|
| Queue Time | LinearRegression | 500 | hour, queue length, day, age, visits |
| Treatment Duration | RandomForest (50 trees) | 500 | treatment type, severity, age, comorbidities |
| Patient Risk | NaiveBayes | 600 | age, severity, diabetes, hypertension, visit gap |
| Follow-Up Need | RandomForest (50 trees) | 500 | severity, gap, age, diabetes |
| Doctor Note | NaiveBayes | 600 | age, severity, comorbidities, treatment type |

### Lifecycle

```
Application Startup
      │
      ▼
Check ./ai-models/ for serialized .model files
      │
  ┌───┴───────────┐
  │               │
Not Found       Found
  │               │
  ▼               ▼
Generate       Load from
synthetic      disk
training       (fast)
data
  │
  ▼
Train Weka
classifiers
  │
  ▼
Serialize to
./ai-models/
  │
  ▼
Ready for
inference
```

### Sample Request

```json
POST /api/ai/predict/risk
{
  "patientAge": 65,
  "severityScore": 8,
  "hasDiabetes": true,
  "hasHypertension": true,
  "lastVisitGapDays": 400,
  "previousVisits": 1
}
```

### Sample Response

```json
{
  "success": true,
  "message": "Risk assessment complete",
  "data": {
    "predictionType": "PATIENT_RISK",
    "predictedValue": 2.0,
    "unit": "risk_level",
    "riskLevel": "HIGH",
    "interpretation": "HIGH RISK: Immediate attention required. Consider priority scheduling and specialist referral.",
    "confidenceScore": 0.91,
    "modelUsed": "NaiveBayes",
    "generatedAt": "2024-11-15T10:30:00"
  }
}
```

---

## 🔒 Authentication Flow

```
Client                          Server
  │                               │
  │─── POST /auth/login ──────────▶
  │     { email, password }       │
  │                         Validate credentials
  │                         Generate JWT (24h) + Refresh (7d)
  │◀─── 200 OK ────────────────── │
  │     { accessToken,            │
  │       refreshToken,           │
  │       user }                  │
  │                               │
  │─── GET /patients ─────────────▶
  │     Authorization: Bearer ... │
  │                         Validate JWT via JwtAuthFilter
  │◀─── 200 OK ────────────────── │
```

---

## 🔑 Password Reset Flow

### Option A — Email Link

```
1. POST /auth/forgot-password { email, method: "EMAIL" }
2. Server generates UUID token, stores in PasswordResetToken table (15 min TTL)
3. Sends email with https://dentalclinic.com/reset-password?token=<UUID>
4. POST /auth/reset-password { token, newPassword }
5. Password updated, token marked as used
```

### Option B — OTP Email

```
1. POST /auth/forgot-password { email, method: "OTP_EMAIL" }
2. Server generates 6-digit OTP (10 min TTL), sends to email
3. POST /auth/reset-password { email, otp, newPassword }
4. Password updated, token marked as used
```

### Option C — OTP SMS (Placeholder)

```
1. POST /auth/forgot-password { email, method: "OTP_SMS" }
2. Server generates 6-digit OTP (10 min TTL)
3. Integrate Twilio/AWS SNS to send SMS (implementation placeholder)
4. POST /auth/reset-password { email, otp, newPassword }
```

---

## 👥 Roles & Permissions

| Resource | ADMIN | DENTIST | RECEPTIONIST | PATIENT |
|---|---|---|---|---|
| Users | Full | View | View | — |
| Clinics | Full | View | — | — |
| Patients | Full | Read/Update | Full | Self only |
| Appointments | Full | Full | Full | Book/View |
| Treatment Plans | Full | Full | View | View |
| Bills | Full | View | Full | View self |
| Prescriptions | Full | Full | View | View self |
| Doctor Notes | Full | Full | — | — |
| Reports | Full | Full | Upload/View | View self |
| Notifications | Full | View own | Send/View | View own |
| AI Predictions | Full | Full | View | — |

---

## ⏰ Scheduler Jobs

| Job | Schedule | Description |
|---|---|---|
| Appointment Reminders | Every hour | Sends email + in-app notification 24h before appointment |
| Mark Overdue Bills | Daily midnight | Marks UNPAID bills as OVERDUE if past due date |
| Clean Expired Tokens | Daily 2 AM | Deletes expired password reset tokens |
| Follow-Up Reminders | Every Monday 9 AM | Notifies patients who haven't visited in 6+ months |

---

## 📖 Swagger UI

Access the interactive API documentation at:

```
http://localhost:8080/api/swagger-ui.html
```

- All endpoints documented with request/response examples
- JWT Bearer auth integrated — click **Authorize** and paste your token
- OpenAPI JSON at `/api/v3/api-docs`

---

## 🗄 Database Schema

Key entity relationships:

```
Clinic ──< Patient ──< Appointment >── User (dentist)
                  ──< TreatmentPlan >── User (dentist)
                  ──< Report
                  ──< Bill >── TreatmentPlan
                  ──< Prescription >── TreatmentPlan >── User
                  ──< DoctorNote >── TreatmentPlan >── User (doctor)

User ──< Notification
PasswordResetToken (standalone, linked by email)
```

All entities use `@CreatedDate` / `@LastModifiedDate` via Spring Data JPA Auditing.

---

## 🏢 Multi-Tenant Support

Each `Clinic` record has a `tenantId` (UUID-based, auto-generated).

- Patients, appointments, and staff are scoped to a clinic via `clinicId`
- Extend with a `TenantContext` thread-local + Hibernate filter for full row-level isolation
- The current implementation provides the data model foundation; request routing per tenant can be added via request headers (`X-Tenant-Id`)

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

Test classes go in `src/test/java/com/dental/clinic/`.

---

## 🐳 Docker (Optional)

```dockerfile
FROM eclipse-temurin:17-jre
COPY target/dental-clinic-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t dental-clinic .
docker run -p 8080:8080 \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e MAIL_USERNAME=your@email.com \
  -e MAIL_PASSWORD=yourpassword \
  dental-clinic
```

---

## 📜 License

MIT License — free to use and modify.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request
