package com.srm.credit.service;

import com.srm.credit.infrastructure.SrmCreditApplication;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = SrmCreditApplication.class)
@ActiveProfiles("local") // <--- Força o carregamento do application-local.yml com as configs do Circuit Breaker
class FxServiceTest {

    @Autowired
    private FxService fxService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("fxService");
        circuitBreaker.reset(); // Reseta o estado para CLOSED antes de cada teste
    }

    @Test
    @DisplayName("Deve retornar o valor de fallback quando o serviço de câmbio falhar")
    void testFallbackActivation() {
        Double rate = fxService.getExchangeRate("USD", "BRL");

        assertEquals(0, Double.compare(1.0, rate), "O valor de fallback deve ser 1.0");
    }

    @Test
    @DisplayName("Deve transitar o Circuit Breaker para o estado OPEN após múltiplas falhas")
    void testCircuitBreakerOpensAfterThreshold() {
        // Com o perfil 'local', a slidingWindowSize é 10 e o threshold é 50%.
        // Realizamos 10 chamadas com falha para preencher a janela e abrir o circuito.
        for (int i = 0; i < 10; i++) {
            try {
                fxService.getExchangeRate("USD", "BRL");
            } catch (Exception ignored) {
            }
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState(),
                "O Circuit Breaker deveria estar no estado OPEN após exceder o limite de falhas.");
    }
}