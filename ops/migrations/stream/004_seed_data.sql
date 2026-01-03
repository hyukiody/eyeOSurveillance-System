-- ========================================
-- EyeO Platform - Stream Database
-- Migration: 004 - Seed Data
-- Default patterns and configurations
-- ========================================

USE teraapi_stream;

-- Default event patterns
INSERT INTO event_patterns (
    pattern_name, 
    pattern_type, 
    event_sequence, 
    time_window_seconds, 
    threshold_count, 
    severity
) VALUES 
    (
        'Repeated Person Detection',
        'FREQUENCY',
        '["PERSON_DETECTED"]',
        300,
        5,
        'MEDIUM'
    ),
    (
        'Weapon Detected',
        'SEQUENCE',
        '["WEAPON_DETECTED"]',
        1,
        1,
        'CRITICAL'
    ),
    (
        'Suspicious Loitering',
        'FREQUENCY',
        '["PERSON_DETECTED"]',
        600,
        10,
        'LOW'
    );
