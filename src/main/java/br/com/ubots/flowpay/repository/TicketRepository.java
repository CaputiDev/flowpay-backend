package br.com.ubots.flowpay.repository;

import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends CrudRepository<Ticket, UUID> {

    /**
     * Busca o ticket pendente mais antigo de uma determinada fila.
     * Aplica o critério de desempate duplo (created_at + ticket_number) para garantir FIFO estrito.
     */
    @Query("SELECT * FROM ticket WHERE queue_id = :queueId AND status = :status ORDER BY created_at ASC, ticket_number ASC LIMIT 1")
    Optional<Ticket> findOldestTicketInQueue(UUID queueId, String status);

    /**
     * Conta quantos chamados estão aguardando em uma fila específica.
     * Usado para validar se a fila atingiu a capacidade máxima (ex: 3).
     */
    int countByQueueIdAndStatus(UUID queueId, StatusEnum status);

    boolean existsByChatRefAndStatus(String chatRef, StatusEnum status);
}