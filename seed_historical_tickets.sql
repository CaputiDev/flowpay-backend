-- ==============================================================================
-- FLOWPAY MVP - SEED DE DADOS HISTÓRICOS DE ATENDIMENTO (12 MESES)
-- ==============================================================================
-- Este script popula a tabela 'ticket' com um histórico realista de chamados
-- distribuídos ao longo do último ano (Agosto/2025 até Agosto/2026).
--
-- Como executar:
-- 1. Via Docker:
--    docker exec -i flowpay-postgres psql -U postgres -d flowpay_db < seed_historical_tickets.sql
-- 2. Via psql direto:
--    psql -h localhost -p 5432 -U postgres -d flowpay_db -f seed_historical_tickets.sql
-- 3. Ou copiando e colando no DBeaver / pgAdmin / DataGrip conectado ao banco 'flowpay_db'.
-- ==============================================================================

BEGIN;

-- Garante que as filas e atendentes existam
INSERT INTO queue (id, team, max_capacity, version)
SELECT gen_random_uuid(), 'CREDIT_CARDS', 3, 0
WHERE NOT EXISTS (SELECT 1 FROM queue WHERE team = 'CREDIT_CARDS');

INSERT INTO queue (id, team, max_capacity, version)
SELECT gen_random_uuid(), 'LOANS', 3, 0
WHERE NOT EXISTS (SELECT 1 FROM queue WHERE team = 'LOANS');

INSERT INTO queue (id, team, max_capacity, version)
SELECT gen_random_uuid(), 'OTHERS', 3, 0
WHERE NOT EXISTS (SELECT 1 FROM queue WHERE team = 'OTHERS');

-- Inserção de tickets históricos estruturados por mês
INSERT INTO ticket (
    id, chat_ref, subject, status, error_msg, created_at, started_at, finished_at,
    is_finished, waiting_time_seconds, service_time_seconds, total_time_seconds,
    queue_id, agent_id, version
) VALUES
-- ==============================================================================
-- AGOSTO / 2025
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822001', 'Preciso desbloquear meu cartão novo', 'RESOLVED', NULL,
    '2025-08-04 09:14:00', '2025-08-04 09:14:25', '2025-08-04 09:17:10',
    TRUE, 25, 165, 190,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822002', 'Simulação de empréstimo consignado CLT', 'RESOLVED', NULL,
    '2025-08-07 11:20:00', '2025-08-07 11:21:10', '2025-08-07 11:25:30',
    TRUE, 70, 260, 330,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Daniel (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'whatsapp_551198822003', 'Dúvida sobre alteração cadastral e endereço', 'RESOLVED', NULL,
    '2025-08-11 14:05:00', '2025-08-11 14:05:40', '2025-08-11 14:08:20',
    TRUE, 40, 160, 200,
    (SELECT id FROM queue WHERE team = 'OTHERS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Gabriela (Outros)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822004', 'Contestação de compra não reconhecida na fatura', 'RESOLVED', NULL,
    '2025-08-15 16:30:00', '2025-08-15 16:30:50', '2025-08-15 16:34:40',
    TRUE, 50, 230, 280,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822005', 'Antecipação do saque aniversário FGTS', 'RESOLVED', NULL,
    '2025-08-20 10:45:00', '2025-08-20 10:46:15', '2025-08-20 10:50:00',
    TRUE, 75, 225, 300,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Eduarda (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'whatsapp_551198822006', 'Fila cheia no pico de fechamento de cartões', 'REJECTED', 'A fila atingiu a capacidade máxima. Solicitação recusada.',
    '2025-08-25 17:40:00', NULL, '2025-08-25 17:40:00',
    TRUE, 0, 0, 0,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1), NULL, 0
),

-- ==============================================================================
-- SETEMBRO / 2025
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822010', 'Aumento de limite de cartão de crédito', 'RESOLVED', NULL,
    '2025-09-02 10:10:00', '2025-09-02 10:10:35', '2025-09-02 10:13:50',
    TRUE, 35, 195, 230,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822011', 'Renegociação de dívida e quitação de parcelas', 'RESOLVED', NULL,
    '2025-09-09 15:20:00', '2025-09-09 15:21:30', '2025-09-09 15:26:00',
    TRUE, 90, 270, 360,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Felipe (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'whatsapp_551198822012', 'Como funciona o programa de cashback e pontos', 'RESOLVED', NULL,
    '2025-09-16 11:00:00', '2025-09-16 11:00:20', '2025-09-16 11:03:00',
    TRUE, 20, 160, 180,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822013', 'Dúvida sobre horário de atendimento na agência', 'RESOLVED', NULL,
    '2025-09-22 09:30:00', '2025-09-22 09:30:15', '2025-09-22 09:32:00',
    TRUE, 15, 105, 120,
    (SELECT id FROM queue WHERE team = 'OTHERS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Henrique (Outros)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822014', 'Taxa CET e IOF de financiamento de veículo', 'RESOLVED', NULL,
    '2025-09-27 14:40:00', '2025-09-27 14:41:00', '2025-09-27 14:45:10',
    TRUE, 60, 250, 310,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Daniel (Empréstimos)' LIMIT 1), 0
),

-- ==============================================================================
-- OUTUBRO / 2025
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822020', 'Segunda via da fatura do cartão Elo', 'RESOLVED', NULL,
    '2025-10-03 08:50:00', '2025-10-03 08:50:18', '2025-10-03 08:52:40',
    TRUE, 18, 142, 160,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822021', 'Análise de crédito para abertura de contrato', 'RESOLVED', NULL,
    '2025-10-10 13:15:00', '2025-10-10 13:16:00', '2025-10-10 13:20:10',
    TRUE, 60, 250, 310,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Eduarda (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822022', 'Bloqueio preventivo por perda de cartão Mastercard', 'RESOLVED', NULL,
    '2025-10-17 19:10:00', '2025-10-17 19:10:20', '2025-10-17 19:13:00',
    TRUE, 20, 160, 180,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'whatsapp_551198822023', 'Empréstimo com garantia de imóvel', 'RESOLVED', NULL,
    '2025-10-24 10:00:00', '2025-10-24 10:01:20', '2025-10-24 10:06:00',
    TRUE, 80, 280, 360,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Felipe (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822024', 'Problema de login e autenticação em dois fatores', 'RESOLVED', NULL,
    '2025-10-29 16:20:00', '2025-10-29 16:20:45', '2025-10-29 16:23:15',
    TRUE, 45, 150, 195,
    (SELECT id FROM queue WHERE team = 'OTHERS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Isabela (Outros)' LIMIT 1), 0
),

-- ==============================================================================
-- NOVEMBRO / 2025 (Black Friday - Alto Volume)
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822030', 'Aumento de limite emergencial Black Friday', 'RESOLVED', NULL,
    '2025-11-05 11:30:00', '2025-11-05 11:30:40', '2025-11-05 11:33:40',
    TRUE, 40, 180, 220,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822031', 'Cartão adicional para dependente titular', 'RESOLVED', NULL,
    '2025-11-12 14:10:00', '2025-11-12 14:10:50', '2025-11-12 14:14:00',
    TRUE, 50, 190, 240,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822032', 'Simulação de crédito pessoal pré-aprovado', 'RESOLVED', NULL,
    '2025-11-19 16:45:00', '2025-11-19 16:46:10', '2025-11-19 16:50:30',
    TRUE, 70, 260, 330,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Daniel (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'whatsapp_551198822033', 'Solicitação recusada no pico da Black Friday', 'REJECTED', 'A fila atingiu a capacidade máxima. Solicitação recusada.',
    '2025-11-28 18:00:00', NULL, '2025-11-28 18:00:00',
    TRUE, 0, 0, 0,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1), NULL, 0
),
(
    gen_random_uuid(), 'web_551198822034', 'Cartão contactless aproximação não funciona', 'RESOLVED', NULL,
    '2025-11-28 20:15:00', '2025-11-28 20:15:30', '2025-11-28 20:18:20',
    TRUE, 30, 170, 200,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),

-- ==============================================================================
-- DEZEMBRO / 2025 (Fim de Ano - Volume Intenso)
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822040', 'Contestação de compra duplicada no Natal', 'RESOLVED', NULL,
    '2025-12-04 10:20:00', '2025-12-04 10:20:45', '2025-12-04 12:24:15',
    TRUE, 45, 210, 255,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822041', 'Refinanciamento de dívidas para 2026', 'RESOLVED', NULL,
    '2025-12-10 15:30:00', '2025-12-10 15:31:40', '2025-12-10 15:36:20',
    TRUE, 100, 280, 380,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Eduarda (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822042', 'Acesso à Sala VIP com cartão Visa Infinite', 'RESOLVED', NULL,
    '2025-12-17 18:40:00', '2025-12-17 18:40:20', '2025-12-17 18:43:00',
    TRUE, 20, 160, 180,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822043', 'Quitação integral de carnê e desconto', 'RESOLVED', NULL,
    '2025-12-22 11:15:00', '2025-12-22 11:16:00', '2025-12-22 11:19:40',
    TRUE, 60, 220, 280,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Felipe (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'whatsapp_551198822044', 'Solicitação de informe de rendimentos', 'RESOLVED', NULL,
    '2025-12-29 09:00:00', '2025-12-29 09:00:30', '2025-12-29 09:02:40',
    TRUE, 30, 130, 160,
    (SELECT id FROM queue WHERE team = 'OTHERS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Gabriela (Outros)' LIMIT 1), 0
),

-- ==============================================================================
-- JANEIRO / 2026 (Impostos, IPVA, Renegociações)
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822050', 'Parcelamento da fatura pós festas', 'RESOLVED', NULL,
    '2026-01-05 09:30:00', '2026-01-05 09:30:35', '2026-01-05 09:34:00',
    TRUE, 35, 205, 240,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822051', 'Empréstimo pessoal para pagamento de IPVA e IPTU', 'RESOLVED', NULL,
    '2026-01-12 14:00:00', '2026-01-12 14:01:10', '2026-01-12 14:05:30',
    TRUE, 70, 260, 330,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Daniel (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822052', 'Renegociação de inadimplência e acordo Serasa', 'RESOLVED', NULL,
    '2026-01-19 16:15:00', '2026-01-19 16:16:30', '2026-01-19 16:21:00',
    TRUE, 90, 270, 360,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Eduarda (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822053', 'Solicitar segunda via de boleto bancário', 'RESOLVED', NULL,
    '2026-01-26 10:45:00', '2026-01-26 10:45:20', '2026-01-26 10:47:30',
    TRUE, 20, 130, 150,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),

-- ==============================================================================
-- FEVEREIRO / 2026 (Carnaval)
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822060', 'Bloqueio de urgência por roubo de celular e cartão', 'RESOLVED', NULL,
    '2026-02-03 21:10:00', '2026-02-03 21:10:15', '2026-02-03 21:13:00',
    TRUE, 15, 165, 180,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822061', 'Contestação de transações por aproximação', 'RESOLVED', NULL,
    '2026-02-10 11:30:00', '2026-02-10 11:31:00', '2026-02-10 11:34:40',
    TRUE, 60, 220, 280,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822062', 'Amortização extraordinária de saldo devedor', 'RESOLVED', NULL,
    '2026-02-18 15:40:00', '2026-02-18 15:41:15', '2026-02-18 15:45:00',
    TRUE, 75, 225, 300,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Felipe (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822063', 'Informações sobre seguro viagem do cartão Visa', 'RESOLVED', NULL,
    '2026-02-24 16:50:00', '2026-02-24 16:50:30', '2026-02-24 16:53:10',
    TRUE, 30, 160, 190,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),

-- ==============================================================================
-- MARÇO / 2026
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822070', 'Como consultar a pontuação do Score no SPC', 'RESOLVED', NULL,
    '2026-03-03 10:15:00', '2026-03-03 10:16:00', '2026-03-03 10:19:30',
    TRUE, 60, 210, 270,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Daniel (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822071', 'Troca de senha do chip do cartão titular', 'RESOLVED', NULL,
    '2026-03-10 14:20:00', '2026-03-10 14:20:25', '2026-03-10 14:22:45',
    TRUE, 25, 140, 165,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822072', 'Crédito consignado para aposentado e pensionista', 'RESOLVED', NULL,
    '2026-03-17 11:00:00', '2026-03-17 11:01:10', '2026-03-17 11:05:40',
    TRUE, 70, 270, 340,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Eduarda (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822073', 'Isenção de taxa de anuidade por gastos mensais', 'RESOLVED', NULL,
    '2026-03-24 16:30:00', '2026-03-24 16:30:40', '2026-03-24 16:33:50',
    TRUE, 40, 190, 230,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),

-- ==============================================================================
-- ABRIL / 2026
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822080', 'Informações de informe de rendimentos para IRPF', 'RESOLVED', NULL,
    '2026-04-06 09:40:00', '2026-04-06 09:40:20', '2026-04-06 09:42:30',
    TRUE, 20, 130, 150,
    (SELECT id FROM queue WHERE team = 'OTHERS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Henrique (Outros)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822081', 'Simulação de refinanciamento de empréstimo', 'RESOLVED', NULL,
    '2026-04-14 15:10:00', '2026-04-14 15:11:15', '2026-04-14 15:16:00',
    TRUE, 75, 285, 360,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Felipe (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822082', 'Alteração de melhor dia e vencimento da fatura', 'RESOLVED', NULL,
    '2026-04-20 11:25:00', '2026-04-20 11:25:35', '2026-04-20 11:28:10',
    TRUE, 35, 155, 190,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822083', 'Fila cheia em período de fechamento de folha', 'REJECTED', 'A fila atingiu a capacidade máxima. Solicitação recusada.',
    '2026-04-28 17:30:00', NULL, '2026-04-28 17:30:00',
    TRUE, 0, 0, 0,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1), NULL, 0
),

-- ==============================================================================
-- MAIO / 2026
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822090', 'Pedido de cartão virtual para compras online', 'RESOLVED', NULL,
    '2026-05-04 10:50:00', '2026-05-04 10:50:20', '2026-05-04 10:52:40',
    TRUE, 20, 140, 160,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822091', 'Simulação de taxa de juros CET de empréstimo', 'RESOLVED', NULL,
    '2026-05-11 14:15:00', '2026-05-11 14:16:00', '2026-05-11 14:20:30',
    TRUE, 60, 270, 330,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Daniel (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822092', 'Dúvida sobre transferência Pix e limite diário', 'RESOLVED', NULL,
    '2026-05-18 16:00:00', '2026-05-18 16:00:25', '2026-05-18 16:02:45',
    TRUE, 25, 140, 165,
    (SELECT id FROM queue WHERE team = 'OTHERS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Isabela (Outros)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822093', 'Transferência de pontos para programa de milhas', 'RESOLVED', NULL,
    '2026-05-25 11:30:00', '2026-05-25 11:31:00', '2026-05-25 11:34:20',
    TRUE, 60, 200, 260,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),

-- ==============================================================================
-- JUNHO / 2026
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822100', 'Desbloqueio de cartão após troca de senha', 'RESOLVED', NULL,
    '2026-06-02 09:15:00', '2026-06-02 09:15:20', '2026-06-02 09:17:35',
    TRUE, 20, 135, 155,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822101', 'Quitação antecipada de empréstimo consignado', 'RESOLVED', NULL,
    '2026-06-09 13:40:00', '2026-06-09 13:41:10', '2026-06-09 13:45:50',
    TRUE, 70, 280, 350,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Eduarda (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822102', 'Código de barras para pagamento da fatura vencida', 'RESOLVED', NULL,
    '2026-06-16 17:10:00', '2026-06-16 17:10:30', '2026-06-16 17:13:00',
    TRUE, 30, 150, 180,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822103', 'Renegociar parcelamento em atraso', 'RESOLVED', NULL,
    '2026-06-23 10:20:00', '2026-06-23 10:21:20', '2026-06-23 10:26:00',
    TRUE, 80, 280, 360,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Felipe (Empréstimos)' LIMIT 1), 0
),

-- ==============================================================================
-- JULHO / 2026
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822110', 'Aumento de limite para viagem de férias', 'RESOLVED', NULL,
    '2026-07-06 10:30:00', '2026-07-06 10:30:40', '2026-07-06 10:33:50',
    TRUE, 40, 190, 230,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822111', 'Antecipação do saque aniversário FGTS 2026', 'RESOLVED', NULL,
    '2026-07-13 15:00:00', '2026-07-13 15:01:05', '2026-07-13 15:05:30',
    TRUE, 65, 265, 330,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Daniel (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822112', 'Benefícios da bandeira Mastercard Black', 'RESOLVED', NULL,
    '2026-07-20 18:20:00', '2026-07-20 18:20:25', '2026-07-20 18:23:00',
    TRUE, 25, 155, 180,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Ana (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822113', 'Atualização de comprovante de renda', 'RESOLVED', NULL,
    '2026-07-27 11:10:00', '2026-07-27 11:10:35', '2026-07-27 11:13:00',
    TRUE, 35, 145, 180,
    (SELECT id FROM queue WHERE team = 'OTHERS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Gabriela (Outros)' LIMIT 1), 0
),

-- ==============================================================================
-- AGOSTO / 2026 (Mês Vigente)
-- ==============================================================================
(
    gen_random_uuid(), 'whatsapp_551198822120', 'Contestação de compra duplicada no cartão', 'RESOLVED', NULL,
    '2026-08-03 09:00:00', '2026-08-03 09:00:25', '2026-08-03 09:03:30',
    TRUE, 25, 185, 210,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Carlos (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'telegram_551198822121', 'Simulação de empréstimo pessoal online', 'RESOLVED', NULL,
    '2026-08-10 11:15:00', '2026-08-10 11:16:10', '2026-08-10 11:20:40',
    TRUE, 70, 270, 340,
    (SELECT id FROM queue WHERE team = 'LOANS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Eduarda (Empréstimos)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'app_551198822122', 'Segunda via da fatura do mês atual', 'RESOLVED', NULL,
    '2026-08-17 14:30:00', '2026-08-17 14:30:20', '2026-08-17 14:32:50',
    TRUE, 20, 150, 170,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1),
    (SELECT id FROM agent WHERE name = 'Beatriz (Cartões)' LIMIT 1), 0
),
(
    gen_random_uuid(), 'web_551198822123', 'Excesso de chamados simultâneos na fila', 'REJECTED', 'A fila atingiu a capacidade máxima. Solicitação recusada.',
    '2026-08-21 08:30:00', NULL, '2026-08-21 08:30:00',
    TRUE, 0, 0, 0,
    (SELECT id FROM queue WHERE team = 'CREDIT_CARDS' LIMIT 1), NULL, 0
);

COMMIT;
