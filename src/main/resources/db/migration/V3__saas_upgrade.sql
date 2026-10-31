-- 1. Drop existing unique constraints on refresh_tokens to allow many-to-one (multiple devices/sessions)
DO $$
DECLARE
    r record;
BEGIN
    FOR r IN (
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'refresh_tokens'::regclass AND contype = 'u'
    ) LOOP
        EXECUTE 'ALTER TABLE refresh_tokens DROP CONSTRAINT ' || r.conname;
    END LOOP;
END $$;

-- 2. Drop unique index if exists
DROP INDEX IF EXISTS idx_rt_user;

-- 3. Add columns to refresh_tokens for device/session tracking
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS ip_address VARCHAR(100);
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS device_details VARCHAR(255);
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 4. Re-create non-unique index for user_id
CREATE INDEX IF NOT EXISTS idx_rt_user ON refresh_tokens(user_id);

-- 5. Add clinic_id columns for tenant isolation scoping
ALTER TABLE bills ADD COLUMN IF NOT EXISTS clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL;
ALTER TABLE treatment_plans ADD COLUMN IF NOT EXISTS clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL;
ALTER TABLE treatment_records ADD COLUMN IF NOT EXISTS clinic_id BIGINT REFERENCES clinics(id) ON DELETE SET NULL;

-- 6. Link existing data to the first clinic (if any exist) for consistency
UPDATE bills SET clinic_id = (SELECT id FROM clinics LIMIT 1) WHERE clinic_id IS NULL AND (SELECT COUNT(*) FROM clinics) > 0;
UPDATE prescriptions SET clinic_id = (SELECT id FROM clinics LIMIT 1) WHERE clinic_id IS NULL AND (SELECT COUNT(*) FROM clinics) > 0;
UPDATE treatment_plans SET clinic_id = (SELECT id FROM clinics LIMIT 1) WHERE clinic_id IS NULL AND (SELECT COUNT(*) FROM clinics) > 0;
UPDATE treatment_records SET clinic_id = (SELECT id FROM clinics LIMIT 1) WHERE clinic_id IS NULL AND (SELECT COUNT(*) FROM clinics) > 0;
