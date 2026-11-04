package com.srm.credit.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class FxService {

    private static final String FX_SERVICE = "fxService";

    @CircuitBreaker(name = FX_SERVICE, fallbackMethod = "fallbackExchangeRate")
    public Double getExchangeRate(String baseCurrency, String targetCurrency) {
        // Lógica de chamada externa de câmbio (ex: RestTemplate ou WebClient)
        // Se o serviço externo falhar ou demorar, o Circuit Breaker atua.
        return callExternalFxApi(baseCurrency, targetCurrency);
    }

    private Double fallbackExchangeRate(String baseCurrency, String targetCurrency, Throwable t) {
        // Valor de fallback/segurança caso o serviço caia
        return 1.0; 
    }

    private Double callExternalFxApi(String base, String target) {
        // Simulação de chamada instável
        throw new RuntimeException("Serviço de câmbio indisponível");
    }
}