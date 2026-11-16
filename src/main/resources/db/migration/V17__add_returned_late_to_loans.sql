ALTER TABLE loans
    ADD COLUMN returned_late BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE loans
SET returned_late = returned_at > scheduled_return_at
WHERE returned_at IS NOT NULL;
