FROM node:24-bookworm-slim AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm install --no-audit --no-fund
COPY frontend/ ./
ENV VITE_API_BASE_URL=/api
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /backend
COPY backend/ ./
COPY --from=frontend-build /frontend/dist/ ./src/main/resources/static/
RUN sh ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /backend/build/libs/*.jar ./app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
