package com.srm.credit.repository;

import com.srm.credit.entity.CurrencyRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRateEntity, Long> {

    // Busca a taxa de câmbio mais recente válida para a moeda até uma determinada data/hora
    Optional<CurrencyRateEntity> findTopByCurrencyCodeAndValidFromLessThanEqualOrderByValidFromDesc(
        String currencyCode, LocalDateTime dateTime
    );
}