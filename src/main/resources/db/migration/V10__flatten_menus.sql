WITH ordered AS (
    SELECT mi.id,
           ROW_NUMBER() OVER (ORDER BY m.position, mi.position, mi.id) AS new_position
    FROM menu_items mi
    JOIN menus m ON m.id = mi.menu_id
)
UPDATE menu_items mi
SET position = o.new_position
FROM ordered o
WHERE mi.id = o.id;

DROP INDEX idx_menu_items_menu_position;
ALTER TABLE menu_items DROP COLUMN menu_id;
DROP TABLE menus;

ALTER TABLE menu_items RENAME TO menus;
CREATE INDEX idx_menus_position ON menus (position);

CREATE TABLE menu_roles (
    menu_id BIGINT NOT NULL REFERENCES menus (id),
    role_id BIGINT NOT NULL REFERENCES roles (id),
    PRIMARY KEY (menu_id, role_id)
);

INSERT INTO menu_roles (menu_id, role_id)
SELECT menu_item_id, role_id
FROM menu_item_roles
WHERE deleted_at IS NULL;

DROP TABLE menu_item_roles;
