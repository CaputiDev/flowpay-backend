# 💳 FlowPay - Engine Inteligente de Roteamento de Atendimento (MVP)

> **FlowPay** é um motor inteligente e resiliente de roteamento e distribuição de chamados de atendimento ao cliente para operações financeiras. O sistema resolve o problema de sobrecarga operacional, gargalos de distribuição e perda de chamados através de balanceamento automático de carga por times especializados, filas sequenciais FIFO com capacidade controlada e mecanismos robustos de concorrência.

---

## ✨ Funcionalidades (Features)

- 🎯 **Roteamento Inteligente por Assunto (Strategy Pattern):** Classificação automática de solicitações para os times responsáveis (*Cartões*, *Empréstimos* ou *Outros Assuntos*).
- ⚖️ **Balanceamento e Limite de Carga (Workload Cap):** Controle estrito de no máximo 3 atendimentos simultâneos por atendente para garantir qualidade e evitar sobrecarga.
- ⏳ **Filas Sequenciais FIFO com Backpressure:** Enfileiramento ordenado por tempo de chegada quando todos os operadores estão ocupados, com teto de fila (máximo 3 pendentes por time) e recusa explícita com HTTP 422 ao exceder.
- 🔄 **Deslocamento e Atribuição Automática:** Ao finalizar um chamado (`PATCH /v1/tickets/{id}/finish`), o operador liberado recebe instantaneamente o próximo chamado mais antigo da fila.
- 🛡️ **Resiliência e Concorrência Segura:** Uso de Lock Otimista (`@Version`) combinado com Spring Retry (`@Retryable`) com backoff automático, prevenindo deadlocks e race conditions.
- 📊 **Monitoramento & Analytics em Tempo Real:** Endpoints dedicados para consulta do snapshot operacional das filas (`/v1/queues/status`), resumo global (`/v1/analytics/overview`) e histórico mensal consolidado (`/v1/analytics/monthly`).
- 📖 **Documentação de Contrato Desacoplada (Clean OpenAPI):** Swagger UI integrado com interfaces desacopladas dos controllers para documentação viva da API.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** [Java 21 (LTS)](https://www.oracle.com/java/technologies/downloads/#java21)
- **Framework:** [Spring Boot 3.5.6](https://spring.io/projects/spring-boot) (Spring Web, Spring Data JDBC, Spring Validation, Spring Retry, Spring AOP)
- **Banco de Dados:** [PostgreSQL 16](https://www.postgresql.org/) (Produção / Docker) e [H2 Database](https://www.h2database.com/) (Ambiente de Testes / Dev)
- **Database Migrations:** [Flyway 11.7.2](https://flywaydb.org/)
- **Documentação da API:** [SpringDoc OpenAPI 3 / Swagger UI 2.8.5](https://springdoc.org/)
- **Testes & Qualidade:** [JUnit 5](https://junit.org/junit5/), [Mockito](https://site.mockito.org/), [Testcontainers](https://testcontainers.com/), [JaCoCo 0.8.12](https://www.jacoco.org/jacoco/)
- **Containerização:** [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- **Produtividade:** [Lombok](https://projectlombok.org/)

---

## 📋 Pré-requisitos

Antes de iniciar, certifique-se de ter instalado em sua máquina:

- [Git](https://git-scm.com/)
- [Java Development Kit (JDK) 21+](https://adoptium.net/) *(necessário para execução local sem Docker)*
- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/install/) *(recomendado para ambiente completo)*

---

## 🚀 Instalação e Execução

### 1. Clonar o Repositório

```bash
git clone https://github.com/CaputiDev/flowpay-mvp.git
cd flowpay-mvp
```

### 2. Configurar Variáveis de Ambiente (Opcional)

O projeto já possui valores padrão funcionais, mas você pode criar seu arquivo `.env` baseado no exemplo:

```bash
# Linux / macOS
cp .env.example .env

# Windows (PowerShell)
Copy-Item .env.example .env
```

---

### 3. Executar a Aplicação

Escolha uma das opções abaixo:

#### Opção A: Execução via Docker Compose (Recomendado - App + PostgreSQL)

Executa a aplicação compilada junto ao banco PostgreSQL e migrations automáticas do Flyway:

```bash
docker-compose up -d --build
```

#### Opção B: Execução Local com Maven Wrapper (Perfil de Teste / H2 em Memória)

Ideal para desenvolvimento ágil sem necessidade de subir containers:

```bash
# Windows (PowerShell / CMD)
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=test"

# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

---

### 4. Executar Testes Automatizados

O projeto conta com testes unitários, integrados e de ponta a ponta:

```bash
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

> 📈 **Relatório de Cobertura JaCoCo:** Após os testes, visualize o relatório abrindo `target/site/jacoco/index.html` no navegador.

---

## 💡 Como Usar

Com a aplicação em execução (`http://localhost:8080`), acesse a documentação interativa ou utilize os exemplos abaixo:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🔌 Principais Endpoints da API REST

### 📊 Dashboard Operacional
- `POST /v1/tickets`: Recebe e roteia nova solicitação de atendimento (201 Atribuído, 202 Na fila, 422 Recusado por fila cheia).
- `PATCH /v1/tickets/{id}/finish`: Encerra o chamado (200 RESOLVED) e puxa o próximo da fila FIFO.
- `GET /v1/queues/status`: Snapshot em tempo real das filas ativas, fila de espera e capacidade dos operadores.

### 📈 Analytics & Relatórios
- `GET /v1/analytics/monthly`: Métricas consolidadas agrupadas mês a mês com médias de tempo e SLA.
- `GET /v1/analytics/overview`: Sumário global de toda a vida da aplicação.
- `GET /v1/analytics/teams`: Métricas e histórico mensal consolidado de todas as equipes.
- `GET /v1/analytics/teams/{team}`: Métricas analíticas exclusivas de uma equipe (`CREDIT_CARDS`, `LOANS`, `OTHERS`).

### 🩺 Health Check
- `GET /`: Health check da aplicação com link para a documentação Swagger UI.