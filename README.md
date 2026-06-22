# Microservices Observability

This project now includes a local ELK stack and a sample Java app that ships logs to Logstash over TCP.

## Run

```bash
docker compose up --build
```

## Endpoints

- Elasticsearch: http://localhost:9200
- Logstash TCP input: `localhost:5000`
- Kibana: http://localhost:5601

## Log index

Logstash writes application events to `application-logs-*`.
