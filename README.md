# tc-oficina-execution-service

Microsserviço de **execução e diagnóstico** da Oficina Mecânica. Acompanha a execução da OS (diagnóstico, reparo, teste, conclusão, entrega) e publica eventos de progresso no Kafka.

- **Porta:** `8083`
- **Banco:** MongoDB (`oficina_execution`)
- **Namespace Kubernetes:** `oficina-execution`
- **Repositório:** https://github.com/rtmaraujo/tc-oficina-execution-service

## Stack

- Java 17 + Spring Boot 3.2.2
- Spring Web, Spring Data MongoDB
- Spring Kafka
- JUnit 5 + Mockito
- JaCoCo (cobertura mínima de 85%)
- Docker multi-stage + Kubernetes (Traefik)

## Arquitetura

```
presentation/   -> Controller REST
application/    -> Casos de uso (interface + impl)
domain/         -> ExecucaoOS, StatusExecucao
infrastructure/ -> Documento Mongo e repositório
config/         -> Listeners Kafka
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/execution/iniciar/{osNumero}` | Inicia diagnóstico (cria ou reutiliza execução) |
| `PUT` | `/api/execution/{id}/status?statusExecucao={status}` | Atualiza o status da execução |
| `POST` | `/api/execution/{id}/pecas?pecas={texto}` | Registra peças substituídas |
| `POST` | `/api/execution/{id}/finalizar` | Finaliza/entrega a execução |
| `GET` | `/api/execution/{id}` | Consulta execução por ID |
| `GET` | `/api/execution/status/{statusExecucao}` | Lista execuções por status |

### Exemplos

```bash
# Iniciar diagnóstico
curl -X POST "http://localhost:8083/api/execution/iniciar/OS-123"

# Atualizar status
curl -X PUT "http://localhost:8083/api/execution/1/status?statusExecucao=REPARO"

# Registrar peça
curl -X POST "http://localhost:8083/api/execution/1/pecas?pecas=Pastilha%20de%20freio"

# Finalizar
curl -X POST "http://localhost:8083/api/execution/1/finalizar"

# Listar por status
curl "http://localhost:8083/api/execution/status/DIAGNOSTICO"
```

## Documentação da API (Swagger)

Desabilitado no perfil local e habilitado apenas no perfil `k8s`:

- `http://44.253.115.185/execution/swagger-ui.html`
- OpenAPI: `http://44.253.115.185/execution/v3/api-docs`

## Status da execução

`AGUARDANDO` → `DIAGNOSTICO` → `REPARO` → `TESTE` → `CONCLUIDA` → `ENTREGUE`
Alternativo: `CANCELADA`.

## Modelo de dados

Coleção MongoDB `execucao_os`:

| Campo | Tipo | Observação |
|---|---|---|
| `id` | Long | `@Id` |
| `osNumero` | String | número da OS |
| `descricaoDiagnostico` | String | diagnóstico técnico |
| `statusExecucao` | String | status atual |
| `pecasSubstituidas` | String | peças trocadas |
| `observacoes` | String | observações |
| `dataInicio` | LocalDateTime | preenchida no início |
| `dataConclusao` | LocalDateTime | preenchida ao concluir/entregar |

## Eventos Kafka

| Evento | Direção | Quando |
|---|---|---|
| `order-progress` | Produz | Início, mudança de status e registro de peças |
| `order-finalized` | Produz | Status `CONCLUIDA` ou ao finalizar a execução |
| `order-finalized` | Consome | Listener `execution-service-group` |

## Configuração

| Variável | Padrão (local) | Descrição |
|---|---|---|
| `SERVER_PORT` | `8083` | Porta HTTP |
| `SPRING_PROFILES_ACTIVE` | — | `docker` ou `k8s` |
| `SPRING_DATA_MONGODB_URI` | `mongodb://localhost:27017/oficina_execution` | URI do MongoDB |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Endereço do Kafka |

No cluster, a URI do MongoDB Atlas deve ser configurada em `k8s/secret.yaml`.

## Como executar localmente

### Com Docker Compose (recomendado)

```bash
docker compose up --build
```

Sobe MongoDB, Zookeeper, Kafka e o serviço. A porta HTTP é `8083`.

### Sem Docker

Pré-requisitos: MongoDB em `localhost:27017` e Kafka em `localhost:9092`.

```bash
mvn spring-boot:run
```

## Testes e cobertura

```bash
mvn test              # testes unitários
mvn verify            # testes + jacoco:check (mínimo 85% LINE e BRANCH)
```

- Testes unitários: domínio, entidade de persistência e serviço de aplicação.
- Cobertura atual: **100%** de linhas e branches.
- Relatório: `target/site/jacoco/index.html`.

O build falha se a cobertura ficar abaixo de 85%, via `jacoco-maven-plugin` (`check`).

## Docker

```bash
docker build -t tc-oficina-execution-service:local .
docker run -p 8083:8083 --env SPRING_DATA_MONGODB_URI=... --env KAFKA_BOOTSTRAP_SERVERS=... tc-oficina-execution-service:local
```

## Deploy no Kubernetes

Manifests em `k8s/`:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
cp k8s/secret.example.yaml k8s/secret.yaml   # preencha a URI do MongoDB Atlas
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/ingressroute.yaml
```

- Imagem: `200111905660.dkr.ecr.us-west-2.amazonaws.com/tc-oficina-execution-service:latest`
- 2 réplicas, probes em `/actuator/health/{readiness,liveness}`
- Rota Traefik: `http://44.253.115.185/execution/*` (prefixo removido)

## CI/CD

`.github/workflows/ci.yml`:

1. Build e testes com Java 17.
2. `mvn verify` — executa testes + `jacoco:check` (>= 85%).
3. Em push para `main`: build/push da imagem no ECR e `kubectl set image` no namespace `oficina-execution`.

## Observabilidade

Actuator exposto em `/actuator` (`health`, `info`, `metrics`, `prometheus` no perfil `k8s`).

## Estrutura

```
src/main/java/com/oficina/execution/service/
├── application/        # interface e implementação dos casos de uso
├── config/             # listeners Kafka
├── domain/             # ExecucaoOS, StatusExecucao
├── infrastructure/     # documento Mongo e repositório
└── presentation/       # controller REST
src/test/java/...        # testes unitários
```
