-- Cria um índice composto otimizado para a busca do ticket mais antigo (FIFO)
CREATE INDEX IF NOT EXISTS idx_ticket_queue_status_date
    ON ticket (queue_id, status, created_at);

-- Bônus: Índices em Foreign Keys são sempre recomendados para acelerar JOINs no futuro
CREATE INDEX IF NOT EXISTS idx_ticket_agent_id ON ticket (agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_team ON agent (team);