FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml ./pom.xml
RUN mvn -q dependency:go-offline
COPY backend/src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl python3 python3-venv \
    && rm -rf /var/lib/apt/lists/* \
    && python3 -m venv /opt/ml-venv
COPY ml/requirements.txt /app/ml/requirements.txt
RUN /opt/ml-venv/bin/pip install --no-cache-dir -r /app/ml/requirements.txt
COPY ml/predict.py /app/ml/predict.py
COPY ml/models/modelo.pkl /app/ml/models/modelo.pkl
COPY --from=build /app/target/*.jar /app/app.jar
ENV ML_PYTHON_COMMAND=/opt/ml-venv/bin/python \
    ML_PREDICT_SCRIPT=/app/ml/predict.py
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
