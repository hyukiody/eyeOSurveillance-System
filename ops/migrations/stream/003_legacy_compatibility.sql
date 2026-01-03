-- ========================================
-- EyeO Platform - Stream Database
-- Migration: 003 - Legacy Compatibility
-- Maintain backward compatibility
-- ========================================

USE teraapi_stream;

-- Legacy stream events table
CREATE TABLE stream_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL,
    correlation_id VARCHAR(64) NOT NULL,
    payload JSON NOT NULL,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stream_events_correlation (correlation_id),
    INDEX idx_stream_events_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- License audit table
CREATE TABLE license_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    license_key VARCHAR(128) NOT NULL,
    request_path VARCHAR(120) NOT NULL,
    request_hash CHAR(44) NOT NULL,
    verdict ENUM('ALLOWED','THROTTLED','BLOCKED') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_license_key (license_key),
    INDEX idx_license_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
