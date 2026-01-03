-- ========================================
-- EyeO Platform - Stream Processing Database
-- Event Detection and Pattern Matching Schema
-- Blue Flow (Intelligence) - CaCTUs Architecture
-- ========================================

CREATE DATABASE IF NOT EXISTS teraapi_stream;
USE teraapi_stream;

-- Drop existing tables if they exist (development only)
DROP TABLE IF EXISTS pattern_matches;
DROP TABLE IF EXISTS event_patterns;
DROP TABLE IF EXISTS detection_events;
DROP TABLE IF EXISTS event_sources;
DROP TABLE IF EXISTS license_audit;
DROP TABLE IF EXISTS stream_events;

-- ========================================
-- 1. EVENT_SOURCES (Edge Devices/Cameras)
-- ========================================

CREATE TABLE event_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL,
    device_name VARCHAR(255),
    location VARCHAR(500),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    last_heartbeat TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_sources_device_id (device_id),
    INDEX idx_sources_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 2. DETECTION_EVENTS (Blue Flow Metadata)
-- ========================================

CREATE TABLE detection_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    source_id BIGINT,
    event_type VARCHAR(100) NOT NULL,
    confidence DECIMAL(5,4),
    storage_ref VARCHAR(500),
    storage_type VARCHAR(50),
    timestamp TIMESTAMP NOT NULL,
    duration_seconds INT,
    bbox_x INT,
    bbox_y INT,
    bbox_width INT,
    bbox_height INT,
    metadata JSON,
    status VARCHAR(50) DEFAULT 'PENDING',
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES event_sources(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_events_event_id (event_id),
    INDEX idx_events_source (source_id),
    INDEX idx_events_type (event_type),
    INDEX idx_events_timestamp (timestamp),
    INDEX idx_events_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 3. EVENT_PATTERNS (Pattern Matching)
-- ========================================

CREATE TABLE event_patterns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_name VARCHAR(255) NOT NULL,
    pattern_type VARCHAR(100) NOT NULL,
    event_sequence JSON,
    time_window_seconds INT,
    threshold_count INT,
    detected_count INT DEFAULT 0,
    last_detected_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    severity VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patterns_type (pattern_type),
    INDEX idx_patterns_active (is_active),
    INDEX idx_patterns_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 4. PATTERN_MATCHES
-- ========================================

CREATE TABLE pattern_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_id BIGINT NOT NULL,
    event_ids JSON NOT NULL,
    match_timestamp TIMESTAMP NOT NULL,
    confidence DECIMAL(5,4),
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pattern_id) REFERENCES event_patterns(id) ON DELETE CASCADE,
    INDEX idx_matches_pattern (pattern_id),
    INDEX idx_matches_timestamp (match_timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 5. LEGACY TABLES (Compatibility)
-- ========================================

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

-- ========================================
-- 6. SEED DATA
-- ========================================

INSERT INTO event_patterns (pattern_name, pattern_type, event_sequence, time_window_seconds, threshold_count, severity) VALUES 
    ('Repeated Person Detection', 'FREQUENCY', '["PERSON_DETECTED"]', 300, 5, 'MEDIUM'),
    ('Weapon Detected', 'SEQUENCE', '["WEAPON_DETECTED"]', 1, 1, 'CRITICAL');

