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

    // Taxa base fixa conforme os critérios do desafio: 1,00% a.m. (0.01)[cite: 9, 13]
    private static final BigDecimal BASE_RATE = new BigDecimal("0.01");

    /**
     * Cria e precifica uma nova proposta de crédito com spread dinâmico e suporte a cross-currency.
     */
    @Transactional
    public CreditProposalResponse createProposal(CreditProposalRequest request) {
        // Spread dinâmico: Cheque Pré-datado = 2.5% (0.025), Duplicata Mercantil = 1.5% (0.015)
        String assetType = request.getAssetType() != null ? request.getAssetType() : "DUPLICATA";
        BigDecimal spread = "CHEQUE".equalsIgnoreCase(assetType)
                ? new BigDecimal("0.025")
                : new BigDecimal("0.015");

        BigDecimal faceValue = BigDecimal.valueOf(request.getRequestedAmount());
        int termMonths = request.getTermMonths();

        // Cálculo da taxa total composta: (1 + Taxa Base + Spread)[cite: 9, 13]
        BigDecimal totalRate = BigDecimal.ONE.add(BASE_RATE).add(spread);

        // Aplicação da fórmula de Valor Presente com alta escala intermediária (10 casas)[cite: 13]
        BigDecimal denominator = totalRate.pow(termMonths);
        BigDecimal presentValue = faceValue.divide(denominator, 10, RoundingMode.HALF_EVEN);

        // Arredondamento final: Banker's Rounding (HALF_EVEN) em 2 casas decimais[cite: 9, 13]
        BigDecimal approvedLimit = presentValue.setScale(2, RoundingMode.HALF_EVEN);

        // Tratamento de Cross-Currency (Caso C3: conversão para USD usando a taxa de câmbio informada)[cite: 9]
        String currency = request.getTargetCurrency() != null ? request.getTargetCurrency() : "BRL";
        if ("USD".equalsIgnoreCase(currency) && request.getExchangeRate() != null) {
            BigDecimal exchangeRate = BigDecimal.valueOf(request.getExchangeRate());
            approvedLimit = approvedLimit.divide(exchangeRate, 2, RoundingMode.HALF_EVEN);
        }

        // Definição do status da proposta
        String status = approvedLimit.compareTo(BigDecimal.ZERO) > 0 ? "APPROVED" : "REJECTED";

        // Montagem da entidade para persistência ACID[cite: 13]
        CreditProposalEntity entity = CreditProposalEntity.builder()
                .clientName(request.getClientName())
                .requestedAmount(faceValue)
                .termMonths(request.getTermMonths())
                .approvedLimit(approvedLimit)
                .status(status)
                .build();

        CreditProposalEntity savedEntity = repository.save(entity);

        return mapToResponse(savedEntity);
    }

    /**
     * Consulta uma proposta de crédito cadastrada pelo ID.[cite: 13]
     */
    @Transactional(readOnly = true)
    public CreditProposalResponse findById(Long id) {
        CreditProposalEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada com ID: " + id));
        return mapToResponse(entity);
    }

    /**
     * Mapeia a Entidade JPA para o DTO de Resposta gerado pelo OpenAPI.[cite: 13]
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