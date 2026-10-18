CREATE UNIQUE INDEX ux_people_code ON people (code);
CREATE UNIQUE INDEX ux_people_document_number ON people (document_number) WHERE deleted_at IS NULL;
