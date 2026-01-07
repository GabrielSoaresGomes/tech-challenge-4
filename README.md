# Tech Challenge 4

Projeto composto por três Azure Functions em Java que juntos recebem, processam e reportam feedbacks:

- Recepção e persistência de feedbacks via HTTP.
- Notificação por e-mail para feedbacks críticos através de fila do Azure.
- Geração periódica de relatórios semanais em PDF.

## Visão Geral das Functions

- **PersistAndSendFeedbackFunction** ([persist-and-send-feedback-function/src/main/java/com/postech/function/PersistAndSendFeedbackFunction.java](persist-and-send-feedback-function/src/main/java/com/postech/function/PersistAndSendFeedbackFunction.java))
  - Endpoint HTTP que recebe feedbacks (JSON), persiste no banco e, se o feedback for crítico, envia uma mensagem para uma Azure Queue para processamento assíncrono.

- **NotifyCriticalFeedbackFunction** ([notify-critical-feedback-function/src/main/java](notify-critical-feedback-function/src/main/java))
  - Trigger: Azure Queue. Recebe mensagens sobre feedbacks críticos e dispara e-mails para os destinatários configurados (implementação via fábrica de `EmailSender`).

- **WeeklyReportFunction** ([generate-weekly-report-function/src/main/java/com/postech/function/WeeklyReportFunction.java](generate-weekly-report-function/src/main/java/com/postech/function/WeeklyReportFunction.java))
  - Trigger: Timer (agendado). Agrega dados semanalmente, gera relatório em PDF e armazena o arquivo no Blob Storage.

## Estrutura do Repositório

- `persist-and-send-feedback-function/` — recepção e persistência de feedbacks.
- `notify-critical-feedback-function/` — processamento de fila e envio de e-mails.
- `generate-weekly-report-function/` — geração de relatórios periódicos.
- `infra/db/` — containers e scripts (ex.: `docker-compose.yml`, scripts de inicialização do banco).

## Requisitos

- Java 17 (conforme pom.xml)
- Maven (uso do wrapper `mvnw` incluído)
- Azure Functions Core Tools (para execução local das Functions)
- Docker & Docker Compose (para subir o banco local definido em `infra/db/`)

## Execução Local

1) Subir banco local (Postgres) definido em `infra/db/docker-compose.yml`:

```bash
cd infra/db
docker-compose up -d
```

2) Configurar variáveis locais
- Cada function possui um `local.settings.json` para execução local. Ajuste `AzureWebJobsStorage` e `ConnectionStrings:Default` conforme necessário.

3) Rodar cada function localmente (na pasta da função):

```bash
# Persist & Send Feedback
cd persist-and-send-feedback-function
./mvnw azure-functions:run

# Notify Critical Feedback (queue trigger)
cd ../notify-critical-feedback-function
./mvnw azure-functions:run

# Generate Weekly Report
cd ../generate-weekly-report-function
./mvnw azure-functions:run
```

Observação: os comandos acima usam o Maven wrapper incluído em cada subprojeto.

## Configurações importantes

- `AzureWebJobsStorage` — connection string do Azure Storage (necessário para filas e blobs).
- `QueueName` — nome da fila usada para mensagens de feedback crítico.
- `Email` settings — configurados via `EmailSenderFactory` e `local.settings.json` (SMTP ou provider equivalente).
- `DB` connection — string de conexão ao PostgreSQL usada por `DatabaseConnector`.

## Como funciona o fluxo

1. Cliente envia POST para a function de persistência com payload JSON.
2. `PersistAndSendFeedbackFunction` valida e salva o feedback no banco (`FeedbackRepository`).
3. Se `critical == true` (ou critério similar), a function coloca uma mensagem na Azure Queue.
4. `NotifyCriticalFeedbackFunction` é disparada pela fila, constrói e envia e-mails para os usuários configurados.
5. Periodicamente, `WeeklyReportFunction` agrega feedbacks (via `WeeklyReportService`), gera PDF (`PDFReportGenerator`) e salva no Blob Storage (`BlobReportStorage`).

## Exemplos de uso

Exemplo de payload para enviar um feedback (HTTP POST):

```bash
curl -X POST http://localhost:7071/api/PersistAndSendFeedback \
  -H "Content-Type: application/json" \
  -d '{"description":"Feedback positivo","note":8}'
```

Exemplo de payload crítico:

```bash
curl -X POST http://localhost:7071/api/PersistAndSendFeedback \
  -H "Content-Type: application/json" \
  -d '{"description":"Feedback negativo","note":2}'
```
