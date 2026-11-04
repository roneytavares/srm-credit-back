# ANEXO B — Pós-Mortem: Incidente de Liquidações Duplicadas

**Data do Incidente:** Sexta-feira, 06 de Novembro de 2026  
**Horário:** 18h40 – 19h30 (Horário de Brasília)  
**Serviço Afetado:** `SRM Credit Engine` — Endpoint de Liquidação (`POST /settlements`)  
**Impacto de Negócio:** Três cedentes distintos receberam pagamentos em duplicidade devido a falhas de consistência transacional e concorrência no fluxo de liquidação.

---

## 1. Linha do Tempo do Incidente

* **18:35** — A mesa de operações realiza o fechamento de lotes de liquidação de final de dia. Devido a uma instabilidade momentânea na rede da API de câmbio (`fxService`) ou re-tries automáticos do frontend/gateway por timeout, requisições simultâneas ou duplicadas chegam ao endpoint `/settlements`.
* **18:40** — O operador reporta via canal de incidentes do Slack que três cedentes entraram em contato afirmando que o valor da liquidação caiu duas vezes em suas contas bancárias.
* **18:42** — O Plantão (Staff Engineer + Dev Pleno) aciona a sala de crise e inicia a investigação nos logs da aplicação e no banco de dados relacional.
* **18:50** — Identificação do vetor do problema no código em produção (Anexo A): ausência de transação ACID (`BEGIN/COMMIT`), falta de bloqueio de concorrência (`SELECT ... FOR UPDATE` ou Optimistic Locking) e o bloco `try/catch` que engolia exceções sem reverter o estado.
* **18:55** — Contenção imediata aplicada: endpoint de liquidação temporariamente colocado em modo de manutenção (`Read-Only`) via feature flag ou bloqueio de rota no API Gateway.
* **19:15** — Validação do impacto no banco de dados: foram mapeadas exatamente 3 transações duplicadas na tabela `settlements` para recebíveis que já constavam como `SETTLED`.
* **19:30** — Comunicação enviada à mesa de operações e financeiro para estorno/ajuste manual dos saldos dos cedentes afetados. Fim da contenção primária.

---

## 2. Causa Raiz Provável

Analisando o código do Anexo A, a causa raiz decorre de **quatro falhas críticas combinadas**:

1. **Falta de Atomicidade (ACID):** As operações de inserção na tabela `settlements` e a atualização do status na tabela `receivables` foram executadas em comandos SQL isolados, sem uma transação de banco de dados (`BEGIN TRANSACTION ... COMMIT / ROLLBACK`).
2. **Tratamento de Exceção Defeituoso:** O bloco `catch` continha o comentário: *`// se falhar aqui, o insert já rodou, então segue o jogo`*. Isso encobria erros e impedia o rollback automático, permitindo fluxos inconsistentes.
3. **Ausência de Idempotência:** O endpoint aceitava qualquer `receivableId` a cada requisição HTTP POST, sem verificar se o recebível já estava liquidado ou se havia uma chave de idempotência (ex. `Idempotency-Key` no header).
4. **Condição de Corrida (Race Condition):** Como não havia bloqueio de concorrência (como um `SELECT ... FOR UPDATE`), duas requisições paralelas para o mesmo `receivableId` passavam simultaneamente pela consulta inicial, calculavam o valor e realizavam inserts duplicados antes que o status fosse alterado para `SETTLED`.

### Como confirmar com evidências:
* **Logs da Aplicação:** Buscar por requisições concorrentes com o mesmo `receivableId` em um intervalo inferior a 1 segundo.
* **Banco de Dados:** Executar query para identificar múltiplos registros na tabela `settlements` agrupados pelo mesmo `receivable_id`.
* **Métricas:** Verificar picos de taxa de erro ou latência no endpoint `/settlements` correlacionados com o horário do incidente.

---

## 3. Ações Imediatas (Contenção) vs. Correção Definitiva

### Ações Imediatas (Contenção):
* **Paralisação Temporária:** Desativar temporariamente o endpoint de liquidação automática via API Gateway até que o hotfix seja aplicado.
* **Ajuste Financeiro Manual:** Acionar a equipe financeira e de operações para proceder com o estorno dos valores duplicados nas contas dos cedentes afetados.
* **Hotfix Emergencial de Código:** Envolver o bloco de inserção e atualização numa transação única de banco de dados e adicionar checagem estrita do status do recebível (`WHERE status = 'PENDING'`).

### Correção Definitiva:
1. **Transações ACID Reais:** Garantir que o repositório utilize gerenciamento explícito de transações (`EntityManager.transaction` ou transações nativas do driver SQL) para que o `INSERT` e o `UPDATE` ocorram de forma atômica.
2. **Implementação de Idempotência:** Exigir o cabeçalho `Idempotency-Key` nas requisições de escrita financeira, armazenando a chave e a resposta em cache/tabela dedicada por pelo menos 24 horas.
3. **Bloqueio Concorrente (Pessimistic/Optimistic Locking):** Utilizar `SELECT ... FOR UPDATE` no momento da leitura do recebível para bloquear a linha na base de dados durante o processamento da liquidação.

---

## 4. Prevenção Sistêmica

Para garantir que esta classe de erro nunca mais ocorra sem transformar o fluxo de desenvolvimento em burocracia excessiva, adotaremos as seguintes diretrizes:

* **Linha de Defesa no Pipeline (Linter / Static Analysis):**
  * Adicionar regras no SonarQube/ESLint que proíbam blocos `try/catch` vazios ou que engulam exceções sem repassar o erro (`rethrow`) ou disparar um `rollback`.
  * Regra mandatória: **Todo código que envolva escrita em tabelas financeiras deve passar por revisão obrigatória de dois engenheiros (Sênior ou Tech Lead)**.
* **Testes de Concorrência Automatizados:**
  * Introduzir testes de integração baseados em concorrência (ex.: disparar 10 requisições simultâneas para o mesmo ID usando ferramentas como Jest/Supertest em paralelo) para validar se o mecanismo de idempotência e locks bloqueia duplicidades.
* **Arquitetura Resiliente (Evolução para EDA):**
  * Em médio prazo, migrar o fluxo síncrono de ponta a ponta para uma **Arquitetura Orientada a Eventos (EDA)** com mensageria (ex.: Kafka ou RabbitMQ) utilizando o padrão *Outbox Pattern*, garantindo processamento exatamente-uma-vez (*exactly-once semantics* ou idempotência robusta no consumidor).