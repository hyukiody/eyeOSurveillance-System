-- TeraAPI Database Initialization
-- Copyright (c) 2026 YiStudIo Software Inc. All rights reserved.

-- Users table
CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(36) PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  token VARCHAR(512) NOT NULL UNIQUE,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Request logs table
CREATE TABLE IF NOT EXISTS request_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  client_id VARCHAR(255) NOT NULL,
  method VARCHAR(10) NOT NULL,
  path VARCHAR(255) NOT NULL,
  duration_ms LONG,
  status_code INT,
  error_message TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_client (client_id),
  INDEX idx_created (created_at)
);

-- Create Roles
INSERT INTO roles (name, description, is_active) VALUES 
('ADMIN', 'Administrator with full access', TRUE),
('USER', 'Standard user with basic access', TRUE),
('GUEST', 'Guest user with limited access', TRUE);

-- Create Default Admin User
-- Default admin user (password is BCrypt-hashed; do not publish plaintext passwords in repo)
INSERT INTO users (username, email, password, is_active, is_locked, created_at, role_id) 
SELECT 'admin', 'admin@teraapi.local', '$2a$12$h3tLOdFvULhFZzIz/sN1n.4KU3L7g8K9m2Q1P5X8Y9Z7V3D1U2C0a', TRUE, FALSE, UNIX_TIMESTAMP()*1000, id
FROM roles WHERE name = 'ADMIN' LIMIT 1;

-- Create Default Test User
-- Default test user (password is BCrypt-hashed; do not publish plaintext passwords in repo)
INSERT INTO users (username, email, password, is_active, is_locked, created_at, role_id)
SELECT 'testuser', 'test@teraapi.local', '$2a$12$K9tLOdFvULhFZzIz/sN1n.4KU3L7g8K9m2Q1P5X8Y9Z7V3D1U2C0a', TRUE, FALSE, UNIX_TIMESTAMP()*1000, id
FROM roles WHERE name = 'USER' LIMIT 1;
