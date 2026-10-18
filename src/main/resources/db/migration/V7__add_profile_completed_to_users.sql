ALTER TABLE users ADD COLUMN profile_completed BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users u
SET profile_completed = TRUE
WHERE EXISTS (
    SELECT 1
    FROM people p
    WHERE p.user_id = u.id
      AND p.deleted_at IS NULL
);
