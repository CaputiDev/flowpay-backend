package br.com.ubots.flowpay.repository;

import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QueueRepository extends CrudRepository<Queue, UUID> {

    /**
     * Busca a fila associada a um time específico.
     */
    @Query("SELECT * FROM queue WHERE team = :team")
    Optional<Queue> findByTeam(TeamEnum team);
}