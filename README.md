# srm-credit-back
# SRM Credit Back - Sistema de Precificação e Propostas de Crédito

Backend desenvolvido em **Spring Boot** para o processamento, precificação e liquidação de propostas de crédito, com suporte a operações *cross-currency* (múltiplas moedas) e foco absoluto em **precisão financeira** e **resiliência**.

---

## 🚀 Tecnologias e Stack

* **Java 17** (OpenJDK)
* **Spring Boot 3.2.5**
* **Spring Data JPA / Hibernate**
* **PostgreSQL / H2 Database** (para testes isolados)
* **Resilience4j** (Circuit Breaker para serviços de câmbio)
* **Maven** (Gestão de dependências e build)
* **JUnit 5 & Mockito** (Suíte de testes automatizados)

---

## 📐 Decisões Arquiteturais e Precisão Matemática

O projeto foi concebido sob rigorosos padrões de engenharia financeira e arquitetura de software, documentados em detalhes no ficheiro [DECISIONS.md](./DECISIONS.md):

1. **Eliminação de Erros de Ponto Flutuante:**
    * Proibição estricta de tipos primitivos/wrappers de ponto flutuante binário (`double` / `float`).
    * Utilização exclusiva de **`BigDecimal`** com precisão configurada via mapeamento JPA (`NUMERIC(18,2)` para valores monetários e `NUMERIC(18,6)` para taxas de câmbio).
2. **Arredondamento Financeiro Oficial:**
    * Aplicação do algoritmo **Banker's Rounding** (`RoundingMode.HALF_EVEN`) em todas as etapas de cálculo de valor presente e conversão cambial para evitar viéses de arredondamento.
3. **Resiliência Externa (`FxService`):**
    * Integração de câmbio protegida por **Resilience4j Circuit Breaker**, garantindo fallback automático (`1.0`) e tolerância a falhas de rede ou indisponibilidade de provedores externos.

---

## 🏆 Validação de Negócio (Golden Cases)

O motor de cálculo valida matematicamente os seguintes cenários padrão de mercado:

* **C1 (Duplicata Mercantil em BRL):** Validação de valor presente com taxa base e spread de 1.5% a.m. (Prazo: 3 meses).
* **C2 (Cheque Pré-datado em BRL):** Precificação com spread de 2.5% a.m. (Prazo: 2 meses).
* **C3 (Cross-Currency - Conversão USD):** Conversão de ativos nacionais para moeda estrangeira utilizando cotações históricas persistidas de forma imutável.

---

## 🛠️ Como Executar o Projeto e os Testes

### Pré-requisitos
* Java 17 instalado e configurado no PATH.
* Maven 3.8+ instalado.

### 1. Clonar o repositório e navegar para a pasta
```bash
git clone [https://github.com/roneytavares/srm-credit-back.git](https://github.com/roneytavares/srm-credit-back.git)
cd srm-credit-back
