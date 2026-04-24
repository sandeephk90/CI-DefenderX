-- Convert all PostgreSQL custom enum columns to VARCHAR so Hibernate can write them natively.
-- Hibernate 6 + PostgreSQL: @Enumerated(EnumType.STRING) sends VARCHAR, not the custom enum type.
ALTER TABLE users     ALTER COLUMN role     TYPE VARCHAR(64) USING role::text;
ALTER TABLE endpoints ALTER COLUMN os_type  TYPE VARCHAR(64) USING os_type::text;
ALTER TABLE endpoints ALTER COLUMN status   TYPE VARCHAR(64) USING status::text;
ALTER TABLE events    ALTER COLUMN severity TYPE VARCHAR(64) USING severity::text;
ALTER TABLE threats   ALTER COLUMN severity TYPE VARCHAR(64) USING severity::text;
ALTER TABLE threats   ALTER COLUMN status   TYPE VARCHAR(64) USING status::text;
ALTER TABLE alerts    ALTER COLUMN channel  TYPE VARCHAR(64) USING channel::text;
