package br.com.ubots.flowpay.model;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table("agent")
public class Agent {

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("team")
    private TeamEnum team;

    @Column("current_load")
    private Integer currentLoad;

    @Column("max_capacity")
    private Integer maxCapacity;

    @Column("version")
    @Version
    private Long version;

    /**
     * Incrementa a carga de trabalho atual do atendente.
     */
    public void incrementLoad() {
        if (this.currentLoad == null) {
            this.currentLoad = 0;
        }
        this.currentLoad++;
    }

    /**
     * Decrementa a carga de trabalho atual do atendente.
     */
    public void decrementLoad() {
        if (this.currentLoad != null && this.currentLoad > 0) {
            this.currentLoad--;
        }
    }

    /**
     * Verifica se o atendente possui capacidade disponível para novos atendimentos.
     */
    public boolean hasAvailableCapacity() {
        if (this.currentLoad == null) {
            return true;
        }
        int capacity = (this.maxCapacity != null) ? this.maxCapacity : 3;
        return this.currentLoad < capacity;
    }
}