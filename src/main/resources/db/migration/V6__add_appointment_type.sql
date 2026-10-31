-- Migration V6: Add appointment_type column to appointments table safely
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS appointment_type VARCHAR(50);

-- Backfill existing appointment rows with safe default 'SCHEDULED'
UPDATE appointments SET appointment_type = 'SCHEDULED' WHERE appointment_type IS NULL;

-- Set DEFAULT value for future row insertions
ALTER TABLE appointments ALTER COLUMN appointment_type SET DEFAULT 'SCHEDULED';

-- Enforce NOT NULL constraint
ALTER TABLE appointments ALTER COLUMN appointment_type SET NOT NULL;
