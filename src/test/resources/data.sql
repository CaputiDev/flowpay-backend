DELETE FROM ticket;
DELETE FROM agent;
DELETE FROM queue;

INSERT INTO queue (id, team, max_capacity, version) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'CREDIT_CARDS', 3, 0),
('550e8400-e29b-41d4-a716-446655440002', 'LOANS', 3, 0),
('550e8400-e29b-41d4-a716-446655440003', 'OTHERS', 3, 0);

INSERT INTO agent (id, name, team, current_load, version, max_capacity) VALUES
('550e8400-e29b-41d4-a716-446655440101', 'Ana (Cartões)', 'CREDIT_CARDS', 0, 0, 3),
('550e8400-e29b-41d4-a716-446655440102', 'Carlos (Cartões)', 'CREDIT_CARDS', 0, 0, 3),
('550e8400-e29b-41d4-a716-446655440103', 'Beatriz (Cartões)', 'CREDIT_CARDS', 0, 0, 3),
('550e8400-e29b-41d4-a716-446655440104', 'Daniel (Empréstimos)', 'LOANS', 0, 0, 3),
('550e8400-e29b-41d4-a716-446655440105', 'Eduarda (Empréstimos)', 'LOANS', 0, 0, 3),
('550e8400-e29b-41d4-a716-446655440106', 'Felipe (Empréstimos)', 'LOANS', 0, 0, 3),
('550e8400-e29b-41d4-a716-446655440107', 'Gabriela (Outros)', 'OTHERS', 0, 0, 3),
('550e8400-e29b-41d4-a716-446655440108', 'Henrique (Outros)', 'OTHERS', 0, 0, 3),
('550e8400-e29b-41d4-a716-446655440109', 'Isabela (Outros)', 'OTHERS', 0, 0, 3);
