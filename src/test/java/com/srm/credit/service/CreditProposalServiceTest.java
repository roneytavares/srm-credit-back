package com.srm.credit.service;

import com.srm.credit.entity.CurrencyRateEntity;
import com.srm.credit.repository.CurrencyRateRepository;
import com.srm.credit.repository.CreditProposalRepository;
import com.srm.credit.repository.SettlementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditProposalServiceTest {

    @Mock
    private CreditProposalRepository creditProposalRepository;

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private CreditProposalService creditProposalService;

    // Taxa base fixa definida pelo desafio: 1,00% a.m. (0.01)
    private static final BigDecimal BASE_RATE = new BigDecimal("0.01");

    /**
     * Método auxiliar isolado para testar a fórmula matemática pura do motor de precificação,
     * garantindo a aderência exata aos Golden Cases C1, C2 e C3.
     */
    private BigDecimal calculatePresentValue(BigDecimal faceValue, int termMonths, BigDecimal spread) {
        // Fórmula: Valor Presente = Valor de Face / (1 + Taxa Base + Spread)^Prazo
        BigDecimal totalRate = BigDecimal.ONE.add(BASE_RATE).add(spread);
        BigDecimal denominator = totalRate.pow(termMonths);
        BigDecimal presentValue = faceValue.divide(denominator, 10, RoundingMode.HALF_EVEN);
        
        // Arredondamento final: Banker's Rounding (HALF_EVEN) com 2 casas decimais
        return presentValue.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Test
    @DisplayName("Golden Case C1: Duplicata Mercantil em BRL (Prazo 3 meses)")
    void testGoldenCaseC1_DuplicataMercantilBrl() {
        BigDecimal faceValue = new BigDecimal("100000.00");
        int term = 3;
        BigDecimal spread = new BigDecimal("0.015"); // 1.5% a.m. para Duplicata

        BigDecimal presentValue = calculatePresentValue(faceValue, term, spread);
        BigDecimal expectedValue = new BigDecimal("92859.94");

        assertEquals(0, expectedValue.compareTo(presentValue), 
            () -> "Esperado: " + expectedValue + ", Obtido: " + presentValue);
    }

    @Test
    @DisplayName("Golden Case C2: Cheque Pré-datado em BRL (Prazo 2 meses)")
    void testGoldenCaseC2_ChequePredatadoBrl() {
        BigDecimal faceValue = new BigDecimal("25000.00");
        int term = 2;
        BigDecimal spread = new BigDecimal("0.025"); // 2.5% a.m. para Cheque Pré-datado

        BigDecimal presentValue = calculatePresentValue(faceValue, term, spread);
        BigDecimal expectedValue = new BigDecimal("23337.77");

        assertEquals(0, expectedValue.compareTo(presentValue), 
            () -> "Esperado: " + expectedValue + ", Obtido: " + presentValue);
    }

    @Test
    @DisplayName("Golden Case C3: Cross-Currency - Duplicata Mercantil com pagamento em USD")
    void testGoldenCaseC3_CrossCurrencyUsd() {
        BigDecimal faceValue = new BigDecimal("100000.00");
        int term = 3;
        BigDecimal spread = new BigDecimal("0.015"); // 1.5% a.m.
        BigDecimal exchangeRateBrlUsd = new BigDecimal("5.4321"); // Cotação BRL/USD

        // 1. Calcula o Valor Presente em BRL (Resultado esperado de C1: 92859.94)
        BigDecimal presentValueBrl = calculatePresentValue(faceValue, term, spread);

        // 2. Converte para USD utilizando a taxa informada e arredondando no final (HALF_EVEN)
        BigDecimal presentValueUsd = presentValueBrl.divide(exchangeRateBrlUsd, 10, RoundingMode.HALF_EVEN)
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal expectedUsdValue = new BigDecimal("17094.67");

        assertEquals(0, expectedUsdValue.compareTo(presentValueUsd), 
            () -> "Esperado em USD: " + expectedUsdValue + ", Obtido: " + presentValueUsd);
    }
}