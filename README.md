# Log Watchman — RAG-BASED Log Monitoring Platform

**Log Watchman** is a distributed RAG-BASED-powered log monitoring and analysis platform designed to ingest microservice logs in real time, detect and analyze errors, retrieve similar historical incidents using semantic search, and provide AI-powered Root Cause Analysis (RCA) with actionable recommendations.

The platform combines **Apache Kafka, PostgreSQL/pgvector, Redis, Ollama, Spring AI, and Server-Sent Events (SSE)** into a complete real-time observability pipeline.

---

##  Key Features

* **Real-Time Log Ingestion**
  Decoupled and asynchronous log processing powered by **Apache Kafka**.

* **Semantic Vector Search**
  Converts logs into vector embeddings using `nomic-embed-text` and stores them in **PostgreSQL with pgvector** for semantic similarity search.

* **AI-Powered Root Cause Analysis**
  Uses **Ollama with `phi3:mini`** to analyze incoming errors and stack traces and generate:

    * Root cause summary
    * Severity level
    * Recommended fixes

* **RAG-Based Analysis**
  Historical incidents retrieved from pgvector are provided as context to the AI model, allowing analysis based on previously observed errors.

* **Redis Rate Limiting**
  Uses Redis-based sliding-window rate limiting to prevent excessive AI processing during high-volume error spikes.

* **Real-Time SSE Dashboard**
  Server-Sent Events (SSE) provide real-time log and analysis updates to the dashboard.

* **Dockerized Infrastructure**
  All major components can be started using Docker Compose.

* **Developer-Friendly JSON Responses**
  AI analysis responses are structured as JSON for easy integration with frontend applications and other services.

---

# Architecture

```text
                         ┌─────────────────────┐
                         │   Microservice Logs │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ POST /logWatchman   │
                         │    Log Ingestion    │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Apache Kafka     │
                         │  Event Streaming    │
                         └──────────┬──────────┘
                                    │
                                    ▼
                    ┌──────────────────────────────┐
                    │     Kafka Consumer Service   │
                    └──────────────┬───────────────┘
                                   │
                     ┌─────────────┴─────────────┐
                     │                           │
                     ▼                           ▼
             ┌────────────────┐          ┌─────────────────┐
             │ Redis          │          │ PostgreSQL      │
             │ Rate Limiting  │          │ + pgvector      │
             └───────┬────────┘          └────────┬────────┘
                     │                            │
                     │                            ▼
                     │                   ┌─────────────────┐
                     │                   │ Semantic Vector │
                     │                   │     Search      │
                     │                   └────────┬────────┘
                     │                            │
                     │      Historical Context    │
                     │             ┌──────────────┘
                     │             ▼
                     │      ┌─────────────────┐
                     └─────►│ Ollama          │
                            │ phi3:mini       │
                            │ AI / RAG        │
                            └────────┬────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │ SSE Stream      │
                            │ Engine          │
                            └────────┬────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │ Live Dashboard  │
                            └─────────────────┘
```

---

# Processing Flow

```text
1. Microservice generates an ERROR log
                  │
                  ▼
2. POST /logWatchman
                  │
                  ▼
3. Log published to Kafka
                  │
                  ▼
4. Kafka Consumer receives event
                  │
                  ├──────────────► Redis
                  │                 Rate Limiting
                  │
                  ▼
5. Generate embedding
   using nomic-embed-text
                  │
                  ▼
6. Store log + vector
   in PostgreSQL/pgvector
                  │
                  ▼
7. Search historical
   similar incidents
                  │
                  ▼
8. Build RAG prompt
                  │
                  ▼
9. Ollama phi3:mini
   performs RCA
                  │
                  ▼
10. Generate structured JSON
                  │
                  ▼
11. Send event through SSE
                  │
                  ▼
12. Live Dashboard
```

---

#  Tech Stack

| Component               | Technology               |
| ----------------------- | ------------------------ |
| Backend                 | Java 21                  |
| Framework               | Spring Boot 3.x          |
| AI Framework            | Spring AI                |
| Event Streaming         | Apache Kafka             |
| Vector Database         | PostgreSQL + pgvector    |
| Embedding Model         | `nomic-embed-text`       |
| LLM                     | Ollama + `phi3:mini`     |
| Caching / Rate Limiting | Redis                    |
| Real-Time Communication | Server-Sent Events (SSE) |
| Containerization        | Docker                   |
| Orchestration           | Docker Compose           |
| API                     | REST                     |

---

# Project Architecture

```text
logWatchman/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── dev/
│       │       └── logMonitor/
│       │           │
│       │           ├── config/
│       │           ├── controllers/
│       │           ├── dto/
│       │           ├── service/
│       │           ├── repository/
│       │           └── LogMonitorApplication.java
│       │
│       └── resources/
│           └── application.properties
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# Quick Start

## Prerequisites

Make sure you have:

* [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* Java 21 JDK
* Git

Docker Desktop should be running before starting the application.

---

## 1. Clone the Repository

```bash
git clone https://github.com/Lovekush-1509/Auto-Log-Diagnoser.git
cd logMonitor
```

---

## 2. Start the Complete Application

Start PostgreSQL, Kafka, Redis, Ollama, and the Spring Boot application:

```powershell
docker compose up -d --build
```

Check running containers:

```powershell
docker ps
```

You should see containers similar to:

```text
log-monitor
postgres-vectordb
kafka-broker
redis-cache
ollama-llm
```

---

# 🤖 Ollama Configuration

The application uses two Ollama models.

### LLM

```text
phi3:mini
```

Used for AI-powered Root Cause Analysis.

### Embedding Model

```text
nomic-embed-text
```

Used to generate vector embeddings for semantic similarity search.

Check installed models:

```powershell
docker exec -it ollama-llm ollama list
```

Expected output:

```text
NAME                       SIZE
nomic-embed-text:latest    274 MB
phi3:mini                  2.2 GB
```

If the models are not automatically downloaded:

```powershell
docker exec -it ollama-llm ollama pull phi3:mini
docker exec -it ollama-llm ollama pull nomic-embed-text
```

---

# API Endpoints

## 1. Ingest Log Event

### Endpoint

```text
POST /logWatchman
```

### Content-Type

```text
application/json
```

### PowerShell

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/logWatchman" -Method Post -ContentType "application/json" -Body '{"serviceName":"payment-service","logLevel":"ERROR","message":"Database connection timeout while processing payment","stackTrace":"java.sql.SQLException: Connection timeout after 3000ms","traceId":"trace-abc-123"}'
```

Example payload:

```json
{
  "serviceName": "payment-service",
  "logLevel": "ERROR",
  "message": "Database connection timeout while processing payment",
  "stackTrace": "java.sql.SQLException: Connection timeout after 3000ms",
  "traceId": "trace-abc-123"
}
```

---

# 2. Semantic Vector Search

Search historical logs using semantic similarity.

### Endpoint

```text
GET /logWatchman/search?query={query}&topK={topK}
```

### PowerShell

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/logWatchman/search?query=Database connection timeout&topK=3" -Method Get
```

Example:

```text
query = Database connection timeout
topK  = 3
```

The service retrieves the most semantically similar historical logs from PostgreSQL/pgvector.

---

# 3. Manual AI Root Cause Analysis

### Endpoint

```text
POST /logWatchman/analyze
```

### PowerShell

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/logWatchman/analyze" -Method Post -ContentType "application/json" -Body '{"serviceName":"payment-service","logLevel":"ERROR","message":"Database connection timeout while processing payment","stackTrace":"java.sql.SQLException: Connection timeout after 3000ms","traceId":"trace-abc-123"}'
```

Example AI response:

```json
{
  "rootCauseSummary": "The likely root cause is insufficient database resources or network latency causing the database connection to time out.",
  "severityLevel": "HIGH",
  "recommendedFix": [
    "Review the current database infrastructure capacity and scaling capabilities.",
    "Analyze network performance between the payment-service and the database.",
    "Implement connection pooling to optimize database access."
  ]
}
```

---


The connection remains open and receives events whenever new log/analysis information is published.

---



This sends **5 different error types × 2 iterations = 10 log events**.

---

#  Docker Commands

### Start everything

```powershell
docker compose up -d
```

### Build and start

```powershell
docker compose up -d --build
```

### Rebuild only the Spring Boot application

If only the Java/Spring Boot code changed:

```powershell
docker compose up -d --build app
```

Or:

```powershell
docker compose build app
docker compose up -d app
```

### View application logs

```powershell
docker logs -f --tail 100 log-monitor
```

### View Ollama logs

```powershell
docker logs -f --tail 100 ollama-llm
```

### View PostgreSQL logs

```powershell
docker logs -f --tail 100 postgres-vectordb
```

### View Kafka logs

```powershell
docker logs -f --tail 100 kafka-broker
```

### Check container resource usage

```powershell
docker stats
```

### Stop the application

```powershell
docker compose down
```

---



Inside Docker Compose, the Ollama URL is configured as:

```text
http://ollama:11434
```

because Docker services communicate using their Compose service names.

---

# 🧠 RAG Pipeline

Log Watchman uses Retrieval-Augmented Generation (RAG) to provide historical context to the AI model.

```text
Incoming Error
      │
      ▼
Generate Embedding
      │
      ▼
nomic-embed-text
      │
      ▼
PostgreSQL + pgvector
      │
      ▼
Similarity Search
      │
      ▼
Retrieve Similar Historical Logs
      │
      ▼
Build RAG Prompt
      │
      ▼
phi3:mini
      │
      ▼
Root Cause Analysis
      │
      ▼
Structured JSON Response
```

This allows the AI analysis to use previously stored incidents rather than relying only on the incoming error message.

---

# Rate Limiting

Redis is used to control the number of errors processed within a given time window.

The purpose is to prevent a sudden error spike from overwhelming:

* Ollama
* CPU/memory resources
* PostgreSQL
* Kafka consumers

Example scenario:

```text
Normal traffic
     │
     ▼
  10 errors
     │
     ▼
AI analysis
```

During an incident:

```text
1000 errors/sec
       │
       ▼
     Redis
       │
       ├── Allowed requests
       │
       └── Rate-limited requests
```

---

#  Real-Time Monitoring

The SSE pipeline allows the dashboard to receive updates without continuously polling the backend.

```text
Kafka
  │
  ▼
Consumer
  │
  ▼
Log Processing
  │
  ▼
AI Analysis
  │
  ▼
SSE Notification Service
  │
  ▼
Browser Dashboard
```

The browser maintains a persistent connection to:

```text
GET /logWatchman/stream
```

---

# 🩺 Example Use Case

Suppose the payment service produces:

```text
ERROR:
Database connection timeout while processing payment

java.sql.SQLException:
Connection timeout after 3000ms
```

Log Watchman:

```text
1. Receives the error
        ↓
2. Publishes it to Kafka
        ↓
3. Consumer processes the event
        ↓
4. Searches historical incidents
        ↓
5. Retrieves similar database timeout logs
        ↓
6. Builds RAG context
        ↓
7. Sends context + error to phi3:mini
        ↓
8. Generates RCA
        ↓
9. Sends result through SSE
        ↓
10. Dashboard displays the analysis
```

---

# Example RCA Output

```json
{
  "rootCauseSummary": "The likely root cause is insufficient database resources or network latency causing the database connection to time out.",
  "severityLevel": "HIGH",
  "recommendedFix": [
    "Review the current database infrastructure capacity and scaling capabilities.",
    "Analyze network performance between the payment-service and the database.",
    "Implement connection pooling to optimize database access."
  ]
}
```



# 🚧 Future Improvements

Potential improvements include:

* [ ] Multi-model AI support
* [ ] Prometheus metrics
* [ ] Grafana dashboards
* [ ] Alerting through Slack/Email
* [ ] Authentication and authorization
* [ ] Distributed tracing
* [ ] Kubernetes deployment
* [ ] Automatic incident correlation
* [ ] AI-generated incident reports
* [ ] Historical incident analytics
* [ ] Model performance monitoring
* [ ] Configurable rate-limit policies

---


---

#  Project Goal

Log Watchman aims to demonstrate how modern distributed systems, event streaming, vector databases, caching, RAG, and local LLMs can be combined to build an intelligent real-time log monitoring and incident analysis platform.

```text
Kafka
  +
PostgreSQL / pgvector
  +
Redis
  +
Ollama
  +
Spring AI
  +
SSE
  =
AI-Powered Log Monitoring
```
