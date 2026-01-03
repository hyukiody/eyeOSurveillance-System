-- ========================================
-- EyeO Platform - Stream Database
-- Migration: 001 - Event Sources
-- Edge device and camera registration
-- ========================================

USE teraapi_stream;

-- Event sources (edge nodes/cameras)
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

-- Detection events (Blue Flow metadata)
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
