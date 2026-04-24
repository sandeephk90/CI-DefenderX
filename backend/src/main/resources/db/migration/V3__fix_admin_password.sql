-- Fix admin password hash to match 'Admin@123'
-- Uses pgcrypto to generate a proper bcrypt hash at migration time
CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE users
SET password_hash = crypt('Admin@123', gen_salt('bf', 12))
WHERE username = 'admin';
