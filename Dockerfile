# Этап 1: сборка с Maven
FROM maven:3.9.2-eclipse-temurin-17 as build

WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (для кэширования)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем весь исходный код
COPY src ./src

# Собираем приложение
RUN mvn clean package -DskipTests

# Этап 2: запускаем приложение на JRE
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Копируем собранный jar с предыдущего этапа
COPY --from=build /app/target/*.jar bankapplication.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "bankapplication.jar"]
