-- 1. Alter Existing Tables
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS reminder_24h_sent BOOLEAN DEFAULT FALSE;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS reminder_1h_sent BOOLEAN DEFAULT FALSE;

ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP DEFAULT NULL;

-- 2. Create Treatment Records Table
CREATE TABLE IF NOT EXISTS treatment_records (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    appointment_id BIGINT REFERENCES appointments(id) ON DELETE SET NULL,
    treatment_plan_id BIGINT REFERENCES treatment_plans(id) ON DELETE SET NULL,
    dentist_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    notes TEXT NOT NULL,
    procedures_performed TEXT NOT NULL,
    cost DOUBLE PRECISION DEFAULT 0.0,
    dentist_remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 3. Create Prescription Items Table
CREATE TABLE IF NOT EXISTS prescription_items (
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicine_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    instructions VARCHAR(255),
    PRIMARY KEY (prescription_id, medicine_name)
);

-- 4. Create Dentist Availability Table
CREATE TABLE IF NOT EXISTS dentist_availability (
    id BIGSERIAL PRIMARY KEY,
    dentist_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL, -- 1 (Monday) to 7 (Sunday)
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT unique_dentist_day UNIQUE(dentist_id, day_of_week)
);

-- 5. Create Dentist Leaves Table
CREATE TABLE IF NOT EXISTS dentist_leaves (
    id BIGSERIAL PRIMARY KEY,
    dentist_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' -- PENDING, APPROVED, REJECTED
);

-- 6. Create Login History Table
CREATE TABLE IF NOT EXISTS login_history (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(100) NOT NULL,
    device_details VARCHAR(255),
    status VARCHAR(50) NOT NULL -- SUCCESS, FAILED_BAD_CREDENTIALS, FAILED_LOCKED
);
