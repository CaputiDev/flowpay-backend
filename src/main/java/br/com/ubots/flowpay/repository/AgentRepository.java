package br.com.ubots.flowpay.repository;

import br.com.ubots.flowpay.model.Agent;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRepository extends CrudRepository<Agent, UUID> {

    /**
     * Busca o primeiro atendente do time que possui menos de 3 chamados ativos,
     * priorizando quem tem a MENOR carga de trabalho atual (distribuição equilibrada).
     */
    @Query("SELECT * FROM agent WHERE team = :team AND current_load < 3 ORDER BY current_load ASC LIMIT 1")
    Optional<Agent> findAvailableAgentByTeam(String team);
}