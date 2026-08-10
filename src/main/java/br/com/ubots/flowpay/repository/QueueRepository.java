package br.com.ubots.flowpay.repository;

import br.com.ubots.flowpay.model.Queue;
import br.com.ubots.flowpay.model.enums.TeamEnum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QueueRepository extends CrudRepository<Queue, UUID> {

    /**
     * Busca a fila associada a um time específico.
     * O Spring Data JDBC traduz o nome do método para:
     * SELECT * FROM queue WHERE team = :team
     */
    Optional<Queue> findByTeam(TeamEnum team);
}