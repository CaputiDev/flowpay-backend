-- Clear tables to guarantee a clean state on initialization
DELETE FROM ticket;
DELETE FROM agent;
DELETE FROM queue;

-- Populate Queues (3 teams with max capacity of 3)
INSERT INTO queue (id, team, max_capacity, version) VALUES
                                                        (gen_random_uuid(), 'CREDIT_CARDS', 3, 0),
                                                        (gen_random_uuid(), 'LOANS', 3, 0),
                                                        (gen_random_uuid(), 'OTHERS', 3, 0);

-- Populate Agents (3 agents per team with initial current_load = 0)
INSERT INTO agent (id, name, team, current_load, version) VALUES
                                                              (gen_random_uuid(), 'Ana (Cartões)', 'CREDIT_CARDS', 0, 0),
                                                              (gen_random_uuid(), 'Carlos (Cartões)', 'CREDIT_CARDS', 0, 0),
                                                              (gen_random_uuid(), 'Beatriz (Cartões)', 'CREDIT_CARDS', 0, 0),
                                                              (gen_random_uuid(), 'Daniel (Empréstimos)', 'LOANS', 0, 0),
                                                              (gen_random_uuid(), 'Eduarda (Empréstimos)', 'LOANS', 0, 0),
                                                              (gen_random_uuid(), 'Felipe (Empréstimos)', 'LOANS', 0, 0),
                                                              (gen_random_uuid(), 'Gabriela (Outros)', 'OTHERS', 0, 0),
                                                              (gen_random_uuid(), 'Henrique (Outros)', 'OTHERS', 0, 0),
                                                              (gen_random_uuid(), 'Isabela (Outros)', 'OTHERS', 0, 0);