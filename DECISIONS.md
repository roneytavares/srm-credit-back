# DECISIONS.md — Registro de Decisões Arquiteturais e Priorização

## 1. Escolhas de Stack e Arquitetura
- **Java 17 + Spring Boot 3.2.5:** Escolhidos por fornecerem suporte LTS estável, alta performance, forte tipagem e ecossistema robusto para o mercado financeiro[cite: 1, 11].
- **Contract-First com OpenAPI 3.0.3:** Definido para garantir desacoplamento estrito entre frontend/clientes e o backend, assegurando conformidade de dados antes da implementação dos endpoints[cite: 11].

## 2. Decisões de Modelagem e Persistência
- **Uso Estrito de `BigDecimal` (`NUMERIC(18,2)` e `NUMERIC(18,6)`):** Proibição total de tipos de ponto flutuante binário (`float`/`double`) nas entidades JPA, regras de cálculo e banco de dados, eliminando erros acumulativos de arredondamento[cite: 3, 5, 8].
- **Modelagem Dedicada de Câmbio (`CurrencyRateEntity`):** Criada para persistir histórico e vigência de cotações em tabela relacional, desacoplando o motor de cálculo de dependências externas voláteis e garantindo auditoria temporal imutável[cite: 5, 11].
- **Registro Imutável de Liquidação (`SettlementEntity`):** Mapeamento desacoplado para registrar transações consolidadas com timestamp e taxa de câmbio efetivamente aplicada, respeitando o princípio de não alteração de histórico financeiro[cite: 6, 11].
- **Convenção de Nomenclatura no PostgreSQL:** Adotada a estratégia `CamelCaseToUnderscoresNamingStrategy` para garantir mapeamento em *snake_case* nas tabelas (`tb_credit_proposal`, `tb_currency_rate`, `tb_settlement`)[cite: 11].

## 3. Resiliência e Tolerância a Falhas
- **Circuit Breaker com Resilience4j (`FxService`):** Implementado no serviço de câmbio para isolar o motor de crédito de lentidões ou indisponibilidades em APIs de cotação externas[cite: 9]. O mecanismo define *fallback* automático mantendo a aplicação funcional e responsiva sob falhas[cite: 9].

## 4. Priorização de Escopo e Trade-offs
- **Priorização do Core de Precificação:** Foco total na precisão decimal com `BigDecimal` e `HALF_EVEN` para aprovação imediata nos Golden Cases[cite: 3, 7, 11].
- **Evolução de Camadas:** Isolamento rigoroso entre DTOs da API, Regras de Negócio no Service e Entidades JPA para prevenir vazamento de regras de domínio[cite: 8, 11].

## 5. Estratégia de Testes Automatizados e Validação Matemática

### Escolha de Ferramentas: JUnit 5 + Mockito
- **Decisão:** Adoção estrita de **JUnit 5** e **Mockito** para testes unitários focados na camada de serviço (`CreditProposalServiceTest`), descartando frameworks BDD de alto nível (ex: Cucumber)[cite: 7, 11].
- **Justificativa:** O foco primário do desafio e da operação financeira é a **corretude decimal**, o rigor do cálculo matemático e a isolabilidade das regras de negócio[cite: 3, 11]. O uso de Mocks via Mockito isola os repositórios JPA de forma rápida e determinística[cite: 7, 11].

### Aferição dos Golden Cases (C1, C2 e C3)
- **Implementação de Teste Dedicado:** Criação de testes unitários específicos para validar com exatidão ao centavo as regras dos Golden Cases definidos pela mesa de operações[cite: 7, 11]:
  - **C1 (Duplicata Mercantil BRL - 3 meses):** Validação do Valor Presente esperado de R$ 92.859,94 (Spread de 1,5% a.m.)[cite: 7, 11].
  - **C2 (Cheque Pré-datado BRL - 2 meses):** Validação do Valor Presente esperado de R$ 23.337,77 (Spread de 2,5% a.m.)[cite: 7, 11].
  - **C3 (Cross-Currency USD - 3 meses):** Validação da conversão BRL -> USD para US$ 17.094,67 com a cotação de 5,4321[cite: 7, 11].
- **Rigor Financeiro:** Garantia de que a divisão intermediária utilize precisão mínima de 10 casas decimais e que a escala final de 2 casas seja consolidada estritamente via **Banker's Rounding (`RoundingMode.HALF_EVEN`)**[cite: 3, 7, 11].

## 6. Performance, Concorrência e Uso de Tabelas Especializadas (`CurrencyRateEntity`)

- **Eliminação de Latência de Rede via Cotação Local:**
  - **Decisão:** Persistir taxas de câmbio e moedas em tabela dedicada (`CurrencyRateEntity`) em vez de realizar chamadas síncronas a APIs de terceiros no fluxo crítico de cálculo[cite: 5, 11].
  - **Por que garante velocidade e concorrência:** Consultar taxas diretamente no PostgreSQL (com índices B-Tree otimizados) reduz a latência de execução de centenas de milissegundos (via rede externa) para frações de milissegundo[cite: 5, 11]. Isso impede o esgotamento de *threads* do servidor Tomcat/Spring Boot e mantém o motor de crédito apto a escalar sob alta concorrência de requisições[cite: 11].

- **Isolamento de Escrita e Imutabilidade (`SettlementEntity`):**
  - **Decisão:** Estruturar o registro de liquidações e propostas em tabelas relacionais normalizadas com padrão *append-only* (sem *updates* destrutivos em histórico)[cite: 6, 11].
  - **Por que garante velocidade e concorrência:** Evita *table locks* prolongados e concorrência de conciliação de dados, permitindo que múltiplas propostas sejam gravadas simultaneamente no PostgreSQL com isolamento de transação otimizado (`READ COMMITTED`)[cite: 11].

- **Gestão de Conexões com HikariCP:**
  - **Decisão:** Utilizar o pool de conexões padrão do Spring Boot 3 (HikariCP) ajustado para o PostgreSQL local[cite: 11].
  - **Por que garante velocidade:** Elimina o alto custo computacional de abrir e fechar conexões TCP/IP com o banco de dados a cada proposta avaliada, reutilizando conexões ativas de forma extremamente eficiente[cite: 11].