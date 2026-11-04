# SPEC.md — SRM Credit Engine (Backend)

## 1. Visão Geral e Arquitetura

Optou-se por estruturar o repositório `srm-credit-back` utilizando a **Arquitetura em Camadas Tradicional (Layered Architecture / N-Tier Architecture)** do Spring Boot, garantindo clareza, alta manutenibilidade e alinhamento com a padronização corporativa adotada na empresa.

### Organização de Pacotes (`com.srm.credit`)
* `controller/`: Camada REST/HTTP (`@RestController`), responsável pela validação de entrada (`@Valid`), roteamento de chamadas e mapeamento de respostas HTTP.
* `service/`: Camada de Regras de Negócio (`@Service`). Centraliza a inteligência de precificação, tratamentos de câmbio, regras financeiras e orquestração de transações (`@Transactional`).
* `repository/`: Camada de Persistência (`JpaRepository`). Interfaces de acesso ao banco de dados PostgreSQL.
* `entity/`: Camada de Mapeamento de Dados (`@Entity`). Representação relacional da base com tipos numéricos rigorosos.
* `infrastructure/`: Camada Transversal (Cross-Cutting). Configurações do Springdoc/OpenAPI, tratamento global de exceções (`@ControllerAdvice`) e políticas de CORS.

---

## 2. Premissas de Negócio e Ambiguidades Resolvidas

- **Prazo e Juros:** O prazo informado na API de precificação é interpretado estritamente em **meses inteiros**, utilizando juros compostos mensais.
- **Taxa Base:** Fixada em **1,00% a.m.** (0.01), conforme especificação dos golden cases de aferição.
- **Spreads por Tipo de Ativo:**
  - Duplicata Mercantil: **1,50% a.m.** (0.015)
  - Cheque Pré-datado: **2,50% a.m.** (0.025)
- **Operações Multimoedas e Histórico de Câmbio (Cross-Currency):**
  - O sistema armazena o histórico de taxas de câmbio com carimbo de data/hora (`valid_from`) na entidade `CurrencyRateEntity`.
  - Garantia de que a simulação ou liquidação utilize exatamente a taxa vigente no momento da transação (atendendo ao Golden Case C3) para assegurar a reprodutibilidade em auditorias futuras.
- **Política de Arredondamento:** O arredondamento é executado **exclusivamente no resultado final** do Valor Presente e do Deságio, adotando **Banker's Rounding (`HALF_EVEN`)** a 2 casas decimais.

---

## 3. Precisão Numérica e Tipagem

- **Prevenção contra Ponto Flutuante Binário:** É estritamente proibido o uso de `float` ou `double` para manipulação de valores monetários ou taxas.
- **Backend (Java):** Utilização integral de `java.math.BigDecimal` em todos os cálculos, DTOs e entidades.
- **Banco de Dados (PostgreSQL):**
  - Valores monetários (valor de face, valor presente, deságio, valor liquidado): `NUMERIC(18, 2)`
  - Taxas e cotações de câmbio: `NUMERIC(18, 6)`

---

## 4. Critérios de Aceite e Qualidade

- **Aferição por Golden Cases:** Validação automatizada via testes unitários (`PricingServiceTest`) cobrindo com exatidão ao centavo os cenários C1, C2 e C3.
- **Idempotência e Imutabilidade (ACID):**
  - A liquidação gera um registro imutável em `SettlementEntity`.
  - Blindagem via restrição única (`proposal_id`) contra execuções duplicadas derivadas de *retries* de rede ou múltiplos cliques.
- **Documentação de Contrato:** Interface REST integralmente documentada via Swagger UI / OpenAPI 3.0.