package com.srm.credit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_credit_proposal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditProposalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "requested_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "approved_limit", precision = 18, scale = 2)
    private BigDecimal approvedLimit;

    @Column(name = "status")
    private String status;
}