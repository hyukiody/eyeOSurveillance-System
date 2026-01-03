-- ========================================
-- EyeO Platform - Sentinel Database Schema
-- Zero-Trust Event Storage
-- ========================================

-- Extensões para JSON e criptografia
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Tabela de Eventos de Detecção (Blind Storage)
CREATE TABLE IF NOT EXISTS detection_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    camera_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    detected_class VARCHAR(50) NOT NULL,
    confidence DECIMAL(5,4) NOT NULL,
    
    -- Referência ao blob criptografado (chave no storage)
    storage_ref_key VARCHAR(255) NOT NULL,
    
    -- Metadados encriptados (coordenadas, etc)
    encrypted_metadata TEXT,
    
    -- Índices de performance
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    INDEX idx_camera_timestamp (camera_id, timestamp DESC),
    INDEX idx_detected_class (detected_class),
    INDEX idx_storage_ref (storage_ref_key)
);

-- Tabela de Logs de Acesso (Auditoria)
CREATE TABLE IF NOT EXISTS access_logs (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID REFERENCES detection_events(id),
    action VARCHAR(50) NOT NULL, -- 'VIEW', 'DOWNLOAD', 'DECRYPT'
    client_ip VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    INDEX idx_event_access (event_id, timestamp DESC)
);

-- Tabela de Sessões de Stream (Tracking ativo)
CREATE TABLE IF NOT EXISTS stream_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    camera_id VARCHAR(100) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_chunk_at TIMESTAMPTZ,
    total_bytes BIGINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- 'ACTIVE', 'PAUSED', 'TERMINATED'
    
    INDEX idx_camera_status (camera_id, status)
);

-- View para Analytics (sem expor dados sensíveis)
CREATE OR REPLACE VIEW analytics_summary AS
SELECT 
    DATE_TRUNC('hour', timestamp) as hour,
    camera_id,
    detected_class,
    COUNT(*) as detection_count,
    AVG(confidence) as avg_confidence
FROM detection_events
GROUP BY DATE_TRUNC('hour', timestamp), camera_id, detected_class
ORDER BY hour DESC;

-- Função para limpar eventos antigos (GDPR compliance)
CREATE OR REPLACE FUNCTION cleanup_old_events(retention_days INT DEFAULT 90)
RETURNS INT AS $$
DECLARE
    deleted_count INT;
BEGIN
    DELETE FROM detection_events 
    WHERE created_at < NOW() - INTERVAL '1 day' * retention_days;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Grant de permissões mínimas (apenas operações necessárias)
GRANT SELECT, INSERT ON detection_events TO sentinel_user;
GRANT SELECT, INSERT ON access_logs TO sentinel_user;
GRANT SELECT, INSERT, UPDATE ON stream_sessions TO sentinel_user;
GRANT SELECT ON analytics_summary TO sentinel_user;

-- Revoke permissões perigosas
REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

-- Comentários de documentação
COMMENT ON TABLE detection_events IS 'Armazena eventos de detecção de IA com referências a blobs criptografados';
COMMENT ON COLUMN detection_events.storage_ref_key IS 'Chave única do blob de vídeo criptografado no storage';
COMMENT ON COLUMN detection_events.encrypted_metadata IS 'Metadados sensíveis criptografados (coordenadas GPS, etc)';
