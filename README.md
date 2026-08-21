# 💳 FlowPay — Engine Inteligente de Roteamento de Atendimento (MVP)

> **Elevator Pitch:** O **FlowPay** é um motor de roteamento inteligente de chamados de clientes projetado para alta disponibilidade e resiliência. Ele protege a saúde mental dos atendentes e garante a satisfação do cliente através de distribuição inteligente por times e filas sequenciais FIFO sem perda de dados.

---

## 🏛️ Decisões de Arquitetura & Design (ADR - Architecture Decision Records)

Para garantir máxima performance, manutenibilidade e resiliência sob concorrência intensa, adotamos as seguintes decisões técnicas:

1. **Lock Otimista (`@Version`) + Spring Retry (`@Retryable`)**:
   - **Motivo:** Em vez de travar o banco de dados com *Locks Pessimistas* (que geram gargalos de IO e deadlocks sob alta carga), utilizamos *Lock Otimista* com `@Version` no Spring Data JDBC acoplado ao Spring Retry (`@Retryable`). Se duas solicitações tentarem atribuir o mesmo atendente simultaneamente, o sistema tenta novamente automaticamente em milissegundos com backoff, garantindo consistência sem onerar a infraestrutura de dados.
2. **Strategy Pattern para Classificação de Assuntos (`SubjectClassifier`)**:
   - **Motivo:** Permite adicionar novos classificadores de temas (ex: *Fraudes*, *Pix*) simplesmente criando uma nova classe anotada com `@Component`, sem alterar o código do serviço principal (*Open/Closed Principle* do SOLID).
3. **Padrão DTO de Saída (`TicketResponse`)**:
   - **Motivo:** Evita o anti-pattern de *Leaking Database Entity*, blindando o modelo interno do banco (`Ticket`) de vazamentos de versão de lock, chaves internas ou dados sensíveis para o cliente HTTP.
4. **Clean OpenAPI Documentation via Interfaces (`TicketControllerOpenApi`)**:
   - **Motivo:** Toda a documentação Swagger está contida em uma interface separada de contrato, mantendo os Controllers 100% limpos e focados exclusivamente na camada Web.

---

## ⚡ Quick Start / Developer Experience (DX)

Todos os comandos abaixo são **Copy & Paste Friendly**. Você pode rodar a aplicação em menos de 1 minuto!

### Opção 1: Execução Completa via Docker Compose (PostgreSQL + App)

```bash
docker-compose up -d --build
```

> **Acesse no navegador:**
> - **Swagger UI (Documentação Interativa):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
> - **OpenAPI v3 JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

### Opção 2: Execução Rápida para Desenvolvimento (H2 em Memória)

Se você já possui o Java 21 instalado e quer testar a aplicação imediatamente em ambiente isolado:

#### Windows (PowerShell / CMD)
```cmd
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=test"
```

#### Linux / macOS
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

---

## 🧪 Suíte de Testes Automatizados & Cobertura

O projeto possui **45+ testes automatizados** divididos em uma estrutura hierárquica clara (`unit/`, `integration/`, `e2e/`).

### Rodar Todos os Testes
```bash
.\mvnw.cmd test
```

### Relatório de Cobertura de Código (JaCoCo)
Após rodar os testes, o relatório de cobertura em formato HTML é gerado automaticamente em:
```text
target/site/jacoco/index.html
```

---

## 📋 Regras de Negócio Implementadas

| Regra | Descrição |
| :--- | :--- |
| **RN01 - Capacidade Máxima** | Cada atendente lida com no máximo **3 solicitações ativas** simultâneas. |
| **RN02 - Exclusividade de Time** | Cada atendente pertence a apenas um time (*Cartões*, *Empréstimos* ou *Outros Assuntos*). |
| **RN03 - Fila FIFO** | Chamados excedentes aguardam em fila respeitando a ordem cronológica de chegada (*First In, First Out*). |
| **RN04 - Quadro Estático** | 9 atendentes fixos (3 para Cartões, 3 para Empréstimos e 3 para Outros Assuntos). |
| **RN05 - Limite da Fila** | Capacidade máxima inflexível de **3 solicitações aguardando na fila por time**. O 4º excedente (13º do time) é recusado com HTTP 422. |

---

## 🔌 Principais Endpoints da API REST

### 1. Criar e Rotear Solicitação
`POST /v1/tickets`

**Payload de Requisição:**
```json
{
  "chatRef": "whatsapp_555199999911",
  "subject": "Preciso de ajuda com limite do meu cartão de crédito"
}
```

**Respostas Possíveis:**
- `201 Created`: Atribuído diretamente a um atendente livre (`status: IN_PROGRESS`).
- `202 Accepted`: Atendentes lotados; enviado para a fila de espera (`status: PENDING`).
- `400 Bad Request`: JSON malformado ou campos vazios.
- `409 Conflict`: Solicitação ativa já existente para o mesmo `chatRef` ou conflito de concorrência.
- `422 Unprocessable Entity`: Capacidade máxima da fila atingida (13ª solicitação do time).

---

### 2. Finalizar Atendimento
`PATCH /v1/tickets/{id}/finish`

**Respostas Possíveis:**
- `200 OK`: Solicitação encerrada (`status: RESOLVED`). Atendente é liberado e a solicitação pendente mais antiga da fila (FIFO) é automaticamente reatribuída.
- `404 Not Found`: ID da solicitação não encontrado.
- `422 Unprocessable Entity`: Solicitação não se encontra em andamento.

---

### 3. Consultar Estado Consolidado das Filas
`GET /v1/queues/status`

**Respostas Possíveis:**
- `200 OK`: Retorna o snapshot em tempo real com filas ativas (`activeQueue`), fila de espera (`waitingQueue`) e resumo de capacidade por equipe (`teamSummaries`).

---

### 4. Healthcheck & Rota da Documentação
`GET /`

**Respostas Possíveis:**
- `200 OK`: Retorna o status de saúde da aplicação (`status: "UP"`) e o caminho para a documentação Swagger UI (`docs: "/swagger-ui/index.html"`).

---

### 5. Consultar Métricas Mensais e Histórico
`GET /v1/analytics/monthly`

**Respostas Possíveis:**
- `200 OK`: Retorna as métricas agregadas agrupadas mês a mês (`YYYY-MM`), incluindo total de chamados, resolvidos, recusados (`REJECTED`), em andamento, em fila, tempos médios de espera (`avgWaitingTimeSeconds`), tempos médios de atendimento (`avgServiceTimeSeconds`) e detalhamento por equipe.

---

### 6. Consultar Resumo Geral de Analytics
`GET /v1/analytics/overview`

**Respostas Possíveis:**
- `200 OK`: Retorna o sumário global com totais consolidados e médias de tempo de toda a vida da aplicação.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem & Framework:** Java 21 (LTS), Spring Boot 3.5.6
- **Persistência:** Spring Data JDBC, PostgreSQL (Produção), H2 Database (Testes/Dev)
- **Migrations:** Flyway Database Migrations
- **Resiliência:** Spring Retry, Lock Otimista (`@Version`)
- **Documentação:** SpringDoc OpenAPI 3 (Swagger UI)
- **Testes & Qualidade:** JUnit 5, Mockito, MockMvc, JaCoCo Coverage Plugin, Testcontainers