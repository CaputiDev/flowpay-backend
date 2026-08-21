# ==============================================================================
# ESTÁGIO 1: Builder (Compilação e Cache de Dependências)
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Otimização de Cache: Copia apenas o pom.xml primeiro para baixar as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e compila o artefato .jar sem executar testes unitários
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================================================================
# ESTÁGIO 2: Runtime (Execução Leve e Segura)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# DevSecOps: Criação de usuário e grupo sem privilégios root para execução segura
RUN addgroup -S flowpaygroup && adduser -S flowpayuser -G flowpaygroup

# Copia apenas o arquivo .jar gerado no estágio de build
COPY --from=builder /app/target/*.jar app.jar

# Ajusta permissões dos arquivos para o usuário não-root
RUN chown -R flowpayuser:flowpaygroup /app

# Transfere a execução para o usuário não-root
USER flowpayuser

# FinOps & JVM Tuning: Garante o limite dinâmico de memória para instâncias EC2 (t2.micro / t3.micro) e Timezone de Brasília
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENV TZ="America/Sao_Paulo"

# Exposição da porta padrão da aplicação Spring Boot
EXPOSE 8080

# Comando de inicialização da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
