package br.com.ubots.flowpay.model;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Table("agent")
public class Agent {
    @Id
    private UUID id;
    private String name;
    private TeamEnum team;
    private Integer currentLoad;
    private Integer maxCapacity;
    @Version
    private Long version;

    public void incrementLoad() {
        if (this.currentLoad == null) {
            this.currentLoad = 0; // Proteção contra NullPointer caso o banco traga nulo
        }
        this.currentLoad++;
    }
}