-- Create payments table to support partial payments, multiple transactions, and refunds
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL REFERENCES bills(id) ON DELETE CASCADE,
    amount DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(100) NOT NULL,
    transaction_reference VARCHAR(255),
    is_refund BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast bill transaction lookups
CREATE INDEX IF NOT EXISTS idx_payment_bill ON payments(bill_id);
