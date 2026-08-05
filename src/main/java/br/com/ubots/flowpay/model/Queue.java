package br.com.ubots.flowpay.model;

import br.com.ubots.flowpay.model.enums.TeamEnum;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Table("queue")
public class Queue {
    @Id
    @Column("id")
    private UUID id;
    @Column("team")
    private TeamEnum team;
    @Column("max_capacity")
    private Integer maxCapacity;
    @Column("version")
    @Version
    private Long version;
}