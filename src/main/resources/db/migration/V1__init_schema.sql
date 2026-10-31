-- Initial Schema Definition for OralCare Dental Clinic System

CREATE TABLE IF NOT EXISTS clinics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    contact VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    website VARCHAR(255),
    license_number VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    tenant_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone_number VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    contact VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    gender VARCHAR(50),
    blood_group VARCHAR(20),
    medical_history TEXT,
    allergies TEXT,
    emergency_contact VARCHAR(255),
    address TEXT,
    clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    dentist_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    treatment_type VARCHAR(255) NOT NULL,
    date_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    duration_minutes INT DEFAULT 30,
    room_number VARCHAR(50),
    booking_time TIMESTAMP,
    queue_position INT,
    estimated_wait_minutes INT,
    clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL,
    check_in_time TIMESTAMP,
    treatment_start_time TIMESTAMP,
    treatment_end_time TIMESTAMP,
    reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_24h_sent BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_1h_sent BOOLEAN NOT NULL DEFAULT FALSE,
    appointment_type VARCHAR(50) DEFAULT 'SCHEDULED',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_apt_patient ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_apt_dentist ON appointments(dentist_id);
CREATE INDEX IF NOT EXISTS idx_apt_datetime ON appointments(date_time);
CREATE INDEX IF NOT EXISTS idx_apt_booking ON appointments(booking_time);

CREATE TABLE IF NOT EXISTS treatment_plans (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    dentist_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL,
    plan_name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    estimated_cost DOUBLE PRECISION,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    total_sessions INT,
    completed_sessions INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS treatment_sessions (
    plan_id BIGINT NOT NULL REFERENCES treatment_plans(id) ON DELETE CASCADE,
    title VARCHAR(255),
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    completed_by_name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    treatment_id BIGINT REFERENCES treatment_plans(id) ON DELETE SET NULL,
    prescribed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL,
    medicine_list TEXT,
    dosage_instructions TEXT,
    duration_days INT,
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bills (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    treatment_id BIGINT REFERENCES treatment_plans(id) ON DELETE SET NULL,
    appointment_id BIGINT REFERENCES appointments(id) ON DELETE SET NULL,
    clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL,
    amount DOUBLE PRECISION NOT NULL,
    discount DOUBLE PRECISION DEFAULT 0.0,
    tax DOUBLE PRECISION DEFAULT 0.0,
    total_amount DOUBLE PRECISION,
    status VARCHAR(50) NOT NULL DEFAULT 'UNPAID',
    due_date TIMESTAMP,
    paid_at TIMESTAMP,
    payment_method VARCHAR(50),
    notes TEXT,
    invoice_number VARCHAR(100) UNIQUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    title VARCHAR(255),
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    reference_id BIGINT,
    reference_type VARCHAR(255),
    created_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notif_user_read ON notifications(user_id, is_read);

CREATE TABLE IF NOT EXISTS doctor_notes (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    treatment_id BIGINT REFERENCES treatment_plans(id) ON DELETE SET NULL,
    doctor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    note_text TEXT NOT NULL,
    category VARCHAR(255),
    is_flagged BOOLEAN DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    file_type VARCHAR(100),
    file_size BIGINT,
    report_text TEXT,
    report_type VARCHAR(100),
    description TEXT,
    uploaded_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    created_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_sender ON chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_receiver ON chat_messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_chat_created ON chat_messages(created_at);

CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    uploaded_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    file_type VARCHAR(100) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_doc_patient ON documents(patient_id);
CREATE INDEX IF NOT EXISTS idx_doc_file_type ON documents(file_type);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    ip_address VARCHAR(100),
    extra_info TEXT,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    otp VARCHAR(50),
    expiry TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    token_type VARCHAR(50) DEFAULT 'EMAIL_LINK',
    ip_address VARCHAR(100),
    attempts INT DEFAULT 0,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(100),
    device_details VARCHAR(255),
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rt_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_rt_user ON refresh_tokens(user_id);

CREATE TABLE IF NOT EXISTS otp_verification (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(50) NOT NULL,
    expiry TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    otp_type VARCHAR(50) NOT NULL DEFAULT 'REGISTRATION',
    created_at TIMESTAMP
);
