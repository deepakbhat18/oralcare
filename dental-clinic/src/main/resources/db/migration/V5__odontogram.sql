-- Create tooth_records table for the Odontogram feature
CREATE TABLE IF NOT EXISTS tooth_records (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    tooth_number INTEGER NOT NULL,
    condition VARCHAR(50) NOT NULL,
    notes TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_patient_tooth UNIQUE (patient_id, tooth_number)
);

CREATE INDEX IF NOT EXISTS idx_tooth_patient ON tooth_records(patient_id);
