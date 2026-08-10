-- Enables UUID generation natively in PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Queue Table (Filas)
CREATE TABLE IF NOT EXISTS queue (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team VARCHAR(50) NOT NULL,
    max_capacity INT NOT NULL,
    version BIGINT DEFAULT 0
    );

-- Agent Table (Atendentes)
CREATE TABLE IF NOT EXISTS agent (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    team VARCHAR(50) NOT NULL,
    current_load INT DEFAULT 0,
    version BIGINT DEFAULT 0,
    max_capacity INT NOT NULL DEFAULT 3
    );

-- Ticket Table (Solicitações/Chamados)
CREATE TABLE IF NOT EXISTS ticket (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_number BIGSERIAL UNIQUE,
    chat_ref VARCHAR(255),
    subject VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    error_msg VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    queue_id UUID REFERENCES queue(id),
    agent_id UUID REFERENCES agent(id),
    version BIGINT DEFAULT 0
    );