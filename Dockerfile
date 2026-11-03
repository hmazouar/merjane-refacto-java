# Multi-stage Dockerfile for cloud-native deployment
# Stage 1: Build & Package
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

# Copy maven wrapper & pom files for caching dependencies
COPY api/.mvn/ api/.mvn/
COPY api/mvnw api/pom.xml api/
WORKDIR /workspace/api
RUN ./mvnw dependency:go-offline -B

# Copy source code and build jar
COPY api/src/ src/
RUN ./mvnw clean package -DskipTests

# Extract layers for layered image build (faster container startup and caching)
RUN java -Djarmode=layertools -jar target/springboilerplate-*.jar extract --destination target/extracted

# Stage 2: Minimal & Secure Runtime
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Cloud-native: Run as non-root user
RUN groupadd -r spring && useradd -r -g spring -u 10001 spring

# Copy extracted layers in order of change frequency
COPY --from=builder /workspace/api/target/extracted/dependencies/ ./
COPY --from=builder /workspace/api/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/api/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/api/target/extracted/application/ ./

USER spring:spring

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]
