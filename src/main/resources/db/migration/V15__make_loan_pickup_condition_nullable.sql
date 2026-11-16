ALTER TABLE loan_items
    ALTER COLUMN pickup_condition DROP NOT NULL;

UPDATE loan_items
SET pickup_condition = NULL
WHERE loan_id IN (SELECT id FROM loans WHERE status IN ('PENDING_PICKUP', 'CANCELLED'));
