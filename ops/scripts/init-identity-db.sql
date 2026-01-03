-- ========================================
-- EyeO Platform - Identity Service Database
-- RBAC (Role-Based Access Control) Schema
-- Based on CaCTUs Architecture Specification
-- ========================================

-- Drop existing tables if they exist (development only)
DROP TABLE IF EXISTS users_roles;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- ========================================
-- 1. ROLES TABLE (Fixed Enum)
-- ========================================
-- Security constraint: Only 3 roles allowed in the system
-- ROLE_ADMIN: System administrators
-- ROLE_USER: Regular users (client-side decryption)
-- ROLE_DEVICE: Edge devices (eyeOSurveillance nodes)

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed the 3 fixed roles (as per CaCTUs specification)
INSERT INTO roles (name, description) VALUES 
    ('ROLE_ADMIN', 'System administrators with full access'),
    ('ROLE_USER', 'Regular users with client-side decryption capability'),
    ('ROLE_DEVICE', 'Edge devices (cameras and processing nodes)');

-- ========================================
-- 2. USERS TABLE (Identities: Humans + Machines)
-- ========================================
-- Supports both human users and device identities

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    device_id VARCHAR(100),  -- For ROLE_DEVICE entries
    is_active BOOLEAN DEFAULT TRUE,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_users_username (username),
    INDEX idx_users_device_id (device_id),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 3. USERS_ROLES (Many-to-Many Join Table)
-- ========================================
-- Enforces referential integrity
-- Prevents duplicate role assignments

CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_users_roles_user (user_id),
    INDEX idx_users_roles_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 4. SESSIONS TABLE (JWT Token Tracking)
-- ========================================
-- Tracks active sessions for audit and revocation

CREATE TABLE sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_sessions_user_id (user_id),
    INDEX idx_sessions_token_hash (token_hash),
    INDEX idx_sessions_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 5. SEED DATA (Default Admin User)
-- ========================================
-- Password: admin123 (CHANGE IN PRODUCTION!)
-- BCrypt hash for 'admin123'

INSERT INTO users (username, email, password_hash, is_active) VALUES 
    ('admin', 'admin@eyeo-platform.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', TRUE);

-- Assign ROLE_ADMIN to default admin user
INSERT INTO users_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- ========================================
-- 6. AUDIT TABLE (Optional - Security Log)
-- ========================================

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(255),
    ip_address VARCHAR(45),
    status VARCHAR(20),
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- VERIFICATION QUERIES
-- ========================================

-- Verify roles are inserted
SELECT * FROM roles;

-- Verify admin user exists
SELECT u.username, u.email, r.name as role
FROM users u
JOIN users_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- Show table statistics
SELECT 
    table_name,
    table_rows,
    data_length,
    index_length
FROM information_schema.tables
WHERE table_schema = DATABASE()
ORDER BY table_name;
