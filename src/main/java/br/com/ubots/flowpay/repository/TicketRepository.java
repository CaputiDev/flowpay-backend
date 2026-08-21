package br.com.ubots.flowpay.repository;

import br.com.ubots.flowpay.model.Ticket;
import br.com.ubots.flowpay.model.enums.StatusEnum;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends CrudRepository<Ticket, UUID> {

    /**
     * Busca o ticket pendente mais antigo de uma determinada fila.
     * Aplica o critério de desempate duplo (created_at + ticket_number) para garantir FIFO estrito.
     */
    @Query("SELECT * FROM ticket WHERE queue_id = :queueId AND status = :status ORDER BY created_at ASC, ticket_number ASC LIMIT 1")
    Optional<Ticket> findOldestTicketInQueue(@Param("queueId") UUID queueId, @Param("status") String status);

    /**
     * Conta quantos chamados estão aguardando em uma fila específica.
     * Usado para validar se a fila atingiu a capacidade máxima (ex: 3).
     */
    long countByQueueIdAndStatus(UUID queueId, StatusEnum status);

    boolean existsByChatRefAndStatus(String chatRef, StatusEnum status);

    boolean existsByChatRefAndStatusIn(String chatRef, Collection<StatusEnum> statuses);

    /**
     * Busca todas as solicitações ativas (em andamento) no sistema.
     */
    @Query("SELECT * FROM ticket WHERE status = 'IN_PROGRESS' ORDER BY created_at ASC, ticket_number ASC")
    List<Ticket> findAllActiveTickets();

    /**
     * Busca todas as solicitações em espera (pendentes) ordenadas por ordem de chegada (FIFO).
     */
    @Query("SELECT * FROM ticket WHERE status = 'PENDING' ORDER BY created_at ASC, ticket_number ASC")
    List<Ticket> findAllWaitingTickets();

    /**
     * Busca todos os tickets ordenados cronologicamente por data de criação.
     */
    @Query("SELECT * FROM ticket ORDER BY created_at ASC, ticket_number ASC")
    List<Ticket> findAllOrderByCreatedAt();
}