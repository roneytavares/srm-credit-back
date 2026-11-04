package com.srm.credit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_currency_rate", indexes = {
    @Index(name = "idx_currency_valid_from", columnList = "currency_code, valid_from")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode; // Ex: USD

    @Column(name = "rate_to_brl", nullable = false, precision = 18, scale = 6)
    private BigDecimal rateToBrl; // Ex: 5.432100

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom; // Data e hora de vigência da taxa
}