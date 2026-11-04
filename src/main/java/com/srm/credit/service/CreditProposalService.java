package com.srm.credit.service;

import com.srm.credit.dto.CreditProposalRequest;
import com.srm.credit.dto.CreditProposalResponse;
import com.srm.credit.entity.CreditProposalEntity;
import com.srm.credit.repository.CreditProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CreditProposalService {

    private final CreditProposalRepository repository;

    // Taxa base fixa conforme os critérios do desafio: 1,00% a.m. (0.01)
    private static final BigDecimal BASE_RATE = new BigDecimal("0.01");

    /**
     * Cria e precifica uma nova proposta de crédito utilizando os critérios do desafio:
     * Fórmula: Valor Presente = Valor de Face / (1 + Taxa Base + Spread)^Prazo
     * Arredondamento: Banker's Rounding (HALF_EVEN) em 2 casas decimais apenas no resultado final.
     */
    @Transactional
    public CreditProposalResponse createProposal(CreditProposalRequest request) {
        // Exemplo de spread aplicado para Duplicata Mercantil (1,5% a.m.)[cite: 3]
        BigDecimal spread = new BigDecimal("0.015");

        BigDecimal faceValue = BigDecimal.valueOf(request.getRequestedAmount());
        int termMonths = request.getTermMonths();

        // Cálculo da taxa total composta: (1 + Taxa Base + Spread)
        BigDecimal totalRate = BigDecimal.ONE.add(BASE_RATE).add(spread);

        // Aplicação da fórmula de Valor Presente com alta escala intermediária (10 casas)
        BigDecimal denominator = totalRate.pow(termMonths);
        BigDecimal presentValue = faceValue.divide(denominator, 10, RoundingMode.HALF_EVEN);

        // Arredondamento final: Banker's Rounding (HALF_EVEN) com 2 casas decimais[cite: 3]
        BigDecimal approvedLimit = presentValue.setScale(2, RoundingMode.HALF_EVEN);

        // Definição do status da proposta
        String status = approvedLimit.compareTo(BigDecimal.ZERO) > 0 ? "APPROVED" : "REJECTED";

        // Montagem da entidade para persistência ACID utilizando BigDecimal puro[cite: 3]
        CreditProposalEntity entity = CreditProposalEntity.builder()
                .clientName(request.getClientName())
                .requestedAmount(faceValue)
                .termMonths(request.getTermMonths())
                .approvedLimit(approvedLimit) // Mantém BigDecimal na entidade e no banco NUMERIC(18,2)[cite: 3]
                .status(status)
                .build();

        CreditProposalEntity savedEntity = repository.save(entity);

        return mapToResponse(savedEntity);
    }

    /**
     * Consulta uma proposta de crédito cadastrada pelo ID.
     */
    @Transactional(readOnly = true)
    public CreditProposalResponse findById(Long id) {
        CreditProposalEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada com ID: " + id));
        return mapToResponse(entity);
    }

    /**
     * Mapeia a Entidade JPA para o DTO de Resposta gerado pelo OpenAPI.
     * (Converte para Double apenas na fronteira do DTO de saída, se exigido pelo contrato gerado,
     * preservando a precisão interna no banco e nos cálculos).
     */
    private CreditProposalResponse mapToResponse(CreditProposalEntity entity) {
        CreditProposalResponse response = new CreditProposalResponse();
        response.setId(entity.getId());
        response.setClientName(entity.getClientName());
        response.setRequestedAmount(entity.getRequestedAmount() != null ? entity.getRequestedAmount().doubleValue() : null);
        response.setTermMonths(entity.getTermMonths());
        response.setApprovedLimit(entity.getApprovedLimit() != null ? entity.getApprovedLimit().doubleValue() : null);
        response.setStatus(entity.getStatus());
        return response;
    }
}