# AI_USAGE.MD — Relatório de Engenharia e Colaboração com IA

## 1. Prompts Estratégicos e Auxílio
- Utilizado auxílio de IA para aceleração no scaffolding do projeto Spring Boot, sugestão de mapeamentos JPA com Lombok e geração inicial de contratos OpenAPI 3.0.3.

## 2. Detecção e Correção de Falhas Geradas pela IA
1. **Configuração de Dependências no Build (POM):** A IA não gerou corretamente a estrutura do `pom.xml` com os diretórios adequados para a estratégia *Contract-First*, exigindo ajustes finos manuais nas dependências do OpenAPI Generator e nos caminhos de saída dos DTOs e APIs.
2. **Estrutura de Pacotes de Infraestrutura:** As classes e pastas de configuração do Spring Boot foram geradas incorretamente no pacote raiz pela IA; foi necessária a reorganização no padrão correto dentro do pacote `com.srm.credit.infrastructure`.
3. **Injeção do Padrão Naming Strategy:** A IA sugeriu mapeamentos genéricos sem tratar a estratégia de *snake_case* exigida para PostgreSQL, sendo corrigida manualmente via configurações de `hibernate.physical_naming_strategy` no `application-local.yml`.

## 3. Decisões Não Delegadas
- A validação de precisão numérico-financeira (`BigDecimal` com escala explícita e *banker's rounding*) e o design de idempotência das liquidações foram definidos e revisados integralmente por engenharia humana, prevenindo falhas clássicas de ponto flutuante.

## 4. Nem todas as classes foram funcionais para montar o Docker e a IA confundiu portas e startes tem que corrigir na mão


