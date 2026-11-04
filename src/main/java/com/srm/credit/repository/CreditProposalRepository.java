package com.srm.credit.repository;

import com.srm.credit.entity.CreditProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditProposalRepository extends JpaRepository<CreditProposalEntity, Long> {
    // O JpaRepository já nos fornece métodos prontos como save, findById, findAll, delete, etc.
}