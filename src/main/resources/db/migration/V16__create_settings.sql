CREATE TABLE system_settings
(
    id                SMALLINT PRIMARY KEY DEFAULT 1,
    tolerance_minutes INTEGER     NOT NULL DEFAULT 45,
    max_duration_days INTEGER     NOT NULL DEFAULT 3,
    min_advance_hours INTEGER     NOT NULL DEFAULT 1,
    auto_cancel_hours INTEGER     NOT NULL DEFAULT 2,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by_id     BIGINT REFERENCES users (id),
    CONSTRAINT ck_system_settings_single_row CHECK (id = 1),
    CONSTRAINT ck_system_settings_tolerance CHECK (tolerance_minutes BETWEEN 0 AND 240),
    CONSTRAINT ck_system_settings_duration CHECK (max_duration_days BETWEEN 1 AND 30),
    CONSTRAINT ck_system_settings_advance CHECK (min_advance_hours BETWEEN 0 AND 72),
    CONSTRAINT ck_system_settings_auto_cancel CHECK (auto_cancel_hours BETWEEN 1 AND 72)
);

INSERT INTO system_settings (id) VALUES (1);

CREATE TABLE role_loan_limits
(
    role_id          BIGINT PRIMARY KEY REFERENCES roles (id),
    max_equipments   INTEGER NOT NULL,
    max_active_loans INTEGER NOT NULL,
    CONSTRAINT ck_role_loan_limits_equipments CHECK (max_equipments BETWEEN 1 AND 10),
    CONSTRAINT ck_role_loan_limits_active_loans CHECK (max_active_loans BETWEEN 1 AND 10)
);

INSERT INTO role_loan_limits (role_id, max_equipments, max_active_loans)
SELECT id, 2, 1
FROM roles
WHERE name NOT IN ('ADMIN', 'SUPERVISOR')
  AND deleted_at IS NULL;

CREATE TABLE business_hours
(
    day_of_week SMALLINT PRIMARY KEY,
    is_open     BOOLEAN NOT NULL,
    opens_at    TIME    NOT NULL,
    closes_at   TIME    NOT NULL,
    CONSTRAINT ck_business_hours_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_business_hours_range CHECK (closes_at > opens_at)
);

INSERT INTO business_hours (day_of_week, is_open, opens_at, closes_at) VALUES
    (1, TRUE, '08:00', '20:00'),
    (2, TRUE, '08:00', '20:00'),
    (3, TRUE, '08:00', '20:00'),
    (4, TRUE, '08:00', '20:00'),
    (5, TRUE, '08:00', '20:00'),
    (6, TRUE, '08:00', '13:00'),
    (7, FALSE, '08:00', '13:00');
