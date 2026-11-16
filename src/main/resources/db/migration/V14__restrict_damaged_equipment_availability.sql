UPDATE equipments
SET availability = 'IN_MAINTENANCE',
    updated_at   = now()
WHERE equipment_condition = 'DAMAGED'
  AND availability = 'AVAILABLE';

ALTER TABLE equipments
    ADD CONSTRAINT ck_equipments_damaged_availability
        CHECK (equipment_condition <> 'DAMAGED' OR availability IN ('IN_MAINTENANCE', 'RETIRED'));
