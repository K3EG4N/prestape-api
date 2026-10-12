ALTER TABLE roles ADD COLUMN external_name VARCHAR(255);

UPDATE roles SET external_name = name;

ALTER TABLE roles ALTER COLUMN external_name SET NOT NULL;