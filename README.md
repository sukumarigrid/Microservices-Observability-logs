# Microservices Observability

This project now runs as a Spring Boot service with:

- ELK for logs
- Actuator and Prometheus for metrics
- Grafana dashboards provisioned from files in the repo

## Run

```bash
docker compose up --build
```

## Application

- App: http://localhost:8085
- Prometheus metrics: http://localhost:8080/actuator/prometheus
- Health: http://localhost:8080/actuator/health

Useful traffic endpoints:

- `GET /api/hello`
- `GET /api/slow?delayMs=500`
- `GET /api/busy?durationMs=500`
- `GET /api/error`

## Observability Stack

- Elasticsearch: http://localhost:9200
- Logstash TCP input: `localhost:5000`
- Kibana: http://localhost:5601
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

Grafana is provisioned with a Prometheus datasource and a dashboard called `Microservices Observability`.
The dashboard tracks request rate, p95 response time, error rate, CPU usage, heap memory, heap utilization, live threads, and process uptime.

## Dashboard data

If the Grafana panels are empty after startup, send a few requests to `/api/slow`, `/api/busy`, and `/api/error`, then wait for the next Prometheus scrape.
