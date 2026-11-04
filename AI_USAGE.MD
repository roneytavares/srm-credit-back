# AI_USAGE.md — Relatório de Engenharia e Colaboração com IA (Backend)

> **Projeto:** SRM Credit Engine — Backend (Spring Boot & Java)
>
> **Objetivo:** Documentar a assistência da Inteligência Artificial no desenvolvimento da engine de crédito, modelagem de dados financeiros e containerização.

## 1. Resumo Executivo da Colaboração com IA

A inteligência artificial atuou como arquiteta de software e consultora técnica no desenvolvimento do backend (`srm-credit-back`). O foco principal foi garantir a precisão matemática rigorosa utilizando `BigDecimal` com arredondamento `HALF_EVEN`, estruturar a API REST em conformidade com o contrato OpenAPI e configurar o empacotamento em container Docker otimizado para o ecossistema Java.

## 2. Análise de Erros, Refatoração e Acertos

### 🧮 2.1 Precisão Financeira, Regras de Negócio e API

* **Falhas e Desafios Iniciais:**
  * **Risco de Arredondamento Impreciso:** Uso incorreto de tipos de ponto flutuante primitivos (`double`/`float`) em cálculos de taxas de juros, deságio e spread, o que violaria o padrão financeiro e falharia nos *Golden Cases* (C1, C2 e C3).
  * **Validação de Entrada:** Necessidade de garantir restrições estritas nos parâmetros de simulação de recebíveis e operações *cross-currency*.

* **Processo de Refatoração:**
  * A IA recomendou e implementou o uso estrito de `BigDecimal` combinando o modo de arredondamento `RoundingMode.HALF_EVEN` (arredondamento bancário) em todas as fórmulas de deságio e prazos.
  * Estruturação de DTOs claros e validações robustas baseadas em Bean Validation (`@NotNull`, `@Min`, `@DecimalMin`).

* **Principais Acertos:**
  * Paridade total com os resultados esperados nos Golden Cases de duplicatas, cheques e operações em USD.
  * Organização limpa dos serviços de domínio e controladores REST.

### 🐳 2.2 Infraestrutura e Dockerfile (Backend)

* **Falhas e Desafios Iniciais:**
  * **Tamanho e Performance da Imagem:** O uso de imagens base pesadas de JDK completo para execução final gerava artefatos lentos para transporte e subida.

* **Processo de Refatoração:**
  * Criação de um *Multi-stage Dockerfile* em Java, utilizando Maven na primeira fase para compilar o arquivo `.jar` executável e uma imagem leve de JRE (ou Eclipse Temurin Alpine/JRE slim) na fase final de runtime.
  * Otimização do mapeamento de portas (`8080`) e comunicação via rede Docker com o frontend.

* **Principais Acertos:**
  * Garantia de inicialização estável e sem vazamentos de memória ou dependências ausentes no container.

## 3. Decisões Humanas (O que não foi delegado)

1. **Definição das Regras Fiscais e de Deságio:** A validação final das fórmulas matemáticas aplicadas aos cálculos de crédito e deságio de recebíveis da SRM.
2. **Homologação e Testes de Integração:** A execução e verificação final da API através de testes locais e simulações reais no ambiente de desenvolvimento.