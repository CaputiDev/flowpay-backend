-- Migration V4: Adiciona colunas para métricas de tempo, flag de finalização e rastreamento de rejeição

ALTER TABLE ticket ADD COLUMN IF NOT EXISTS is_finished BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS started_at TIMESTAMP;
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS waiting_time_seconds BIGINT DEFAULT 0;
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS service_time_seconds BIGINT DEFAULT 0;
ALTER TABLE ticket ADD COLUMN IF NOT EXISTS total_time_seconds BIGINT DEFAULT 0;

-- Índice para consultas analíticas e relatórios mensais
CREATE INDEX IF NOT EXISTS idx_ticket_analytics ON ticket (created_at, status, is_finished);
