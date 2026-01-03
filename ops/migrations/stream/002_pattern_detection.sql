-- ========================================
-- EyeO Platform - Stream Database
-- Migration: 002 - Pattern Detection
-- AI pattern matching and threat detection
-- ========================================

USE teraapi_stream;

-- Event patterns configuration
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

-- Pattern matches log
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
