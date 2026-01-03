-- ========================================
-- EyeO Platform - Identity Database
-- Migration: 003 - Seed Data
-- Default roles and admin user
-- ========================================

USE teraapi_identity;

-- Insert fixed roles (CaCTUs specification)
INSERT INTO roles (name, description) VALUES 
    ('ROLE_ADMIN', 'System administrators with full access'),
    ('ROLE_USER', 'Regular users with client-side decryption capability'),
    ('ROLE_DEVICE', 'Edge devices (cameras and processing nodes)');

-- Default admin user
-- Password: admin123 (CHANGE IN PRODUCTION!)
INSERT INTO users (username, email, password_hash, is_active) VALUES 
    ('admin', 'admin@eyeo-platform.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', TRUE);

-- Assign ROLE_ADMIN to admin user
INSERT INTO users_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';
