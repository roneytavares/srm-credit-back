package com.srm.credit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_settlement", uniqueConstraints = {
    @UniqueConstraint(name = "uk_settlement_proposal", columnNames = {"proposal_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proposal_id", nullable = false)
    private Long proposalId; // ID da proposta de crédito liquidada

    @Column(name = "settled_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal settledAmount; // Valor final transacionado

    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // Moeda de pagamento (ex: BRL, USD)

    @Column(name = "exchange_rate", precision = 18, scale = 6)
    private BigDecimal exchangeRate; // Taxa de câmbio aplicada (se cross-currency)

    @Column(name = "settled_at", nullable = false)
    private LocalDateTime settledAt; // Timestamp imutável da liquidação
}