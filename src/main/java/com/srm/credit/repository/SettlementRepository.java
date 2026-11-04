package com.srm.credit.repository;

import com.srm.credit.entity.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {

    // Permite verificar se uma proposta já foi liquidada (auxilia na idempotência)
    Optional<SettlementEntity> findByProposalId(Long proposalId);
}