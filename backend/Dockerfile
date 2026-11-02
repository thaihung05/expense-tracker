# --- Buoc 1: build file jar bang JDK ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
# Copy pom truoc de Docker cache phan tai thu vien (code doi thi khong phai tai lai)
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests

# --- Buoc 2: chi chay bang JRE (image nho hon, khong chua cong cu build) ---
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=UTC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
