-- Replace admin password with a pre-computed BCrypt hash for 'Admin@123'
-- Hash generated with BCryptPasswordEncoder(strength=12), prefix $2a$
UPDATE users
SET password_hash = '$2a$12$SxSRnsVLEsB9biGaHTDOoORFI5OJZ1tK9fGUy1JQfGus5x1ILMB6C'
WHERE username = 'admin';
