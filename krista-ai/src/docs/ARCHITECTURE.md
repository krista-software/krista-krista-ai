# Krista AI Extension - Architecture Document

**Version:** 1.0.2
**Last Updated:** 2025-11-27  
**Extension Package:** `app.krista.extensions.krista.sdk.kristaai`

---

## 1. Overview

The Krista AI Extension provides integration with Krista's AI model management platform, enabling users to manage AI models, upload training datasets, trigger retraining jobs, and monitor training status.

### Key Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime environment |
| Krista SDK | 1.0.110 | Platform integration |
| HK2 | 2.6.1 | Dependency injection |
| SLF4J | - | Logging framework |

---

## 2. Architecture Layers

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        LAYER 1: EXTENSION ENTRY POINT                        │
│                           KristaAiExtension.java                             │
│                    [@Extension, @StaticResource, @Java]                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      LAYER 2: CATALOG REQUEST LAYER                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐              │
│  │  DatasetsArea   │  │   ModelsArea    │  │  TrainingArea   │              │
│  │  [@Domain]      │  │   [@Domain]     │  │  [@Domain]      │              │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘              │
│           │                    │                    │                        │
│  • Upload Data        • Get Model Versions  • Retrain Model                  │
│    for Retraining     • Change Live Version • Get Retraining Status          │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        LAYER 3: ENTITY & STORE LAYER                         │
│  ┌─────────────────────────────┐  ┌─────────────────────────────┐           │
│  │         Model.java          │  │       ModelStore.java       │           │
│  │   [@Entity, @Domain]        │  │   [EntityStore<Model>]      │           │
│  │                             │  │                             │           │
│  │  • modelId                  │  │  • get(modelId)             │           │
│  │  • modelName                │  │  • search(query, page)      │           │
│  │  • creationTime             │  │  • contains(modelId)        │           │
│  └─────────────────────────────┘  └─────────────────────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         LAYER 4: UTILITY LAYER                               │
│  ┌─────────────────────────────┐  ┌─────────────────────────────┐           │
│  │      ModelUtils.java        │  │    TelemetryHelper.java     │           │
│  │                             │  │   [@Service, @Singleton]    │           │
│  │  • validateModelExists()    │  │                             │           │
│  │                             │  │  • recordOperationStart()   │           │
│  │                             │  │  • recordOperationSuccess() │           │
│  │                             │  │  • recordOperationFailure() │           │
│  └─────────────────────────────┘  └─────────────────────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    LAYER 5: KRISTA SDK INTEGRATION LAYER                     │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐ │
│  │    AiModel    │  │   AiDataset   │  │  RoleManager  │  │ TelemetryMet- │ │
│  │               │  │               │  │               │  │     rics      │ │
│  └───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘ │
│                         Krista SDK APIs (krista-apis)                        │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              Krista AI Extension                              │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                         KristaAiExtension                              │  │
│  │                    [Entry Point & Configuration]                       │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                     │                                        │
│           ┌─────────────────────────┼─────────────────────────┐              │
│           ▼                         ▼                         ▼              │
│  ┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐      │
│  │  DatasetsArea   │      │   ModelsArea    │      │  TrainingArea   │      │
│  │                 │      │                 │      │                 │      │
│  │ uploadDataFor   │      │ listModelVer-   │      │ retrainModel()  │      │
│  │ Retraining()    │      │ sions()         │      │ retrainingStatus│      │
│  │                 │      │ changeLiveVer-  │      │ ()              │      │
│  │                 │      │ sion()          │      │                 │      │
│  └────────┬────────┘      └────────┬────────┘      └────────┬────────┘      │
│           │                        │                        │                │
│           └────────────────────────┼────────────────────────┘                │
│                                    ▼                                         │
│                         ┌─────────────────────┐                              │
│                         │     ModelStore      │◄──────┐                      │
│                         │                     │       │                      │
│                         │  [Authorization &   │       │                      │
│                         │   Model Access]     │       │                      │
│                         └──────────┬──────────┘       │                      │
│                                    │                  │                      │
│           ┌────────────────────────┼──────────────────┤                      │
│           ▼                        ▼                  │                      │
│  ┌─────────────────┐      ┌─────────────────┐  ┌─────────────────┐          │
│  │   ModelUtils    │      │ TelemetryHelper │  │     Model       │          │
│  │                 │      │                 │  │   [Entity]      │          │
│  │ [Validation]    │      │ [Metrics]       │  │                 │          │
│  └─────────────────┘      └─────────────────┘  └─────────────────┘          │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                              Krista SDK Layer                                 │
│                                                                              │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  │
│  │    AiModel    │  │   AiDataset   │  │  RoleManager  │  │Authorization  │  │
│  │               │  │               │  │               │  │   Context     │  │
│  │• getAllModels │  │• uploadData   │  │• isWorkspace  │  │• getAuthorized│  │
│  │• getVersions  │  │  ToStaging()  │  │  Admin()      │  │  Account()    │  │
│  │• activateVer  │  │               │  │               │  │               │  │
│  │• retrainModel │  │               │  │               │  │               │  │
│  │• getStatus    │  │               │  │               │  │               │  │
│  └───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Data Flow Diagrams

### 4.1 Upload Data for Retraining Flow

```
┌──────────┐     ┌──────────────┐     ┌────────────┐     ┌───────────┐     ┌──────────┐
│  Client  │     │ DatasetsArea │     │ ModelStore │     │ AiDataset │     │ Telemetry│
└────┬─────┘     └──────┬───────┘     └─────┬──────┘     └─────┬─────┘     └────┬─────┘
     │                  │                   │                  │                │
     │ uploadDataFor    │                   │                  │                │
     │ Retraining()     │                   │                  │                │
     │─────────────────►│                   │                  │                │
     │                  │                   │                  │                │
     │                  │ startOperation()  │                  │                │
     │                  │──────────────────────────────────────────────────────►│
     │                  │                   │                  │                │
     │                  │ contains(modelId) │                  │                │
     │                  │──────────────────►│                  │                │
     │                  │                   │                  │                │
     │                  │   true/false      │                  │                │
     │                  │◄──────────────────│                  │                │
     │                  │                   │                  │                │
     │                  │ uploadDataToStaging(modelId, file)   │                │
     │                  │─────────────────────────────────────►│                │
     │                  │                   │                  │                │
     │                  │                   │    success       │                │
     │                  │◄─────────────────────────────────────│                │
     │                  │                   │                  │                │
     │                  │ recordSuccess()   │                  │                │
     │                  │──────────────────────────────────────────────────────►│
     │                  │                   │                  │                │
     │  {ERROR: false,  │                   │                  │                │
     │   MESSAGE: "...",│                   │                  │                │
     │   STATUS: "..."}│                   │                  │                │
     │◄─────────────────│                   │                  │                │
     │                  │                   │                  │                │
```

### 4.2 Retrain Model Flow

```
┌──────────┐     ┌──────────────┐     ┌────────────┐     ┌───────────┐     ┌──────────┐
│  Client  │     │ TrainingArea │     │ ModelStore │     │  AiModel  │     │ Telemetry│
└────┬─────┘     └──────┬───────┘     └─────┬──────┘     └─────┬─────┘     └────┬─────┘
     │                  │                   │                  │                │
     │ retrainModel()   │                   │                  │                │
     │─────────────────►│                   │                  │                │
     │                  │                   │                  │                │
     │                  │ startOperation()  │                  │                │
     │                  │──────────────────────────────────────────────────────►│
     │                  │                   │                  │                │
     │                  │ validateModel()   │                  │                │
     │                  │──────────────────►│                  │                │
     │                  │                   │                  │                │
     │                  │ retrainModel(modelId)                │                │
     │                  │─────────────────────────────────────►│                │
     │                  │                   │                  │                │
     │                  │                   │     jobId        │                │
     │                  │◄─────────────────────────────────────│                │
     │                  │                   │                  │                │
     │                  │ recordSuccess()   │                  │                │
     │                  │──────────────────────────────────────────────────────►│
     │                  │                   │                  │                │
     │ {RETRAINING_JOB_ID: "...",          │                  │                │
     │  ERROR: false, MESSAGE: "..."}      │                  │                │
     │◄─────────────────│                   │                  │                │
     │                  │                   │                  │                │
```

---

## 5. Sequence Diagram - Model Version Change

```
┌────────┐      ┌────────────┐      ┌────────────┐      ┌─────────┐      ┌───────────┐
│ Client │      │ ModelsArea │      │ ModelStore │      │ AiModel │      │ Telemetry │
└───┬────┘      └─────┬──────┘      └─────┬──────┘      └────┬────┘      └─────┬─────┘
    │                 │                   │                  │                 │
    │changeLiveVersion│                   │                  │                 │
    │(modelId,version)│                   │                  │                 │
    │────────────────►│                   │                  │                 │
    │                 │                   │                  │                 │
    │                 │startOperation     │                  │                 │
    │                 │("changeLiveVer")  │                  │                 │
    │                 │─────────────────────────────────────────────────────►│
    │                 │                   │                  │                 │
    │                 │validateModel      │                  │                 │
    │                 │ExistsAndAccessible│                  │                 │
    │                 │──────────────────►│                  │                 │
    │                 │                   │                  │                 │
    │                 │                   │ensureUserHas     │                 │
    │                 │                   │Access()          │                 │
    │                 │                   │──────┐           │                 │
    │                 │                   │      │           │                 │
    │                 │                   │◄─────┘           │                 │
    │                 │                   │                  │                 │
    │                 │      validated    │                  │                 │
    │                 │◄──────────────────│                  │                 │
    │                 │                   │                  │                 │
    │                 │activateModelVersion(modelId, version)│                 │
    │                 │─────────────────────────────────────►│                 │
    │                 │                   │                  │                 │
    │                 │                   │     success      │                 │
    │                 │◄─────────────────────────────────────│                 │
    │                 │                   │                  │                 │
    │                 │recordSuccess()    │                  │                 │
    │                 │─────────────────────────────────────────────────────►│
    │                 │                   │                  │                 │
    │{ERROR: false,   │                   │                  │                 │
    │ MESSAGE: "Live  │                   │                  │                 │
    │ version changed │                   │                  │                 │
    │ successfully!"} │                   │                  │                 │
    │◄────────────────│                   │                  │                 │
    │                 │                   │                  │                 │
```

---

## 6. Performance Limitations

### 6.1 Connection & Throughput Constraints

| Constraint | Limit | Impact |
|------------|-------|--------|
| Model List Fetch | All models loaded in memory | High memory usage for large model counts |
| Search Pagination | In-memory filtering | Performance degrades with large datasets |
| File Upload Size | Krista SDK limit | Large training files may timeout |
| Concurrent Requests | SDK connection pool | Throttling under high load |

### 6.2 Latency Considerations

| Operation | Expected Latency | Factors |
|-----------|------------------|---------|
| Get Model | 100ms - 500ms | SDK API call, network |
| Search Models | 200ms - 2s | Model count, filter complexity |
| Upload Dataset | 1s - 60s | File size, network bandwidth |
| Retrain Model | 100ms - 500ms | Job queue submission only |
| Get Retraining Status | 100ms - 500ms | SDK API call |

### 6.3 Resource Consumption

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Resource Usage Over Time                              │
│                                                                             │
│  Memory │                                                                   │
│    ▲    │                    ┌────┐                                         │
│    │    │                    │    │ Model List                              │
│    │    │         ┌────┐     │    │ Fetch                                   │
│    │    │         │    │     │    │                                         │
│    │    │    ┌────┤    ├─────┤    ├────┐                                    │
│    │    │    │    │    │     │    │    │                                    │
│    │    │────┴────┴────┴─────┴────┴────┴────────────────────────────►       │
│    │                                                                Time    │
│    │    Init   Search  Search  List    Search  Idle                         │
│    │           Op 1    Op 2    Models  Op 3                                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.4 Recommendations

| Scenario | Recommendation |
|----------|----------------|
| Large model count (>100) | Implement server-side pagination |
| Frequent searches | Add caching layer with TTL |
| Large training files | Use chunked upload if available |
| High concurrency | Implement request queuing |

---

## 7. Error Scenarios

### 7.1 Error Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Error Handling Flow                                │
│                                                                             │
│  ┌─────────────┐                                                            │
│  │   Request   │                                                            │
│  └──────┬──────┘                                                            │
│         │                                                                   │
│         ▼                                                                   │
│  ┌─────────────────┐     ┌─────────────────┐                               │
│  │  Authorization  │────►│ AccessDenied    │──► {ERROR: true,              │
│  │     Check       │ NO  │   Exception     │    MESSAGE: "Not authorized"} │
│  └────────┬────────┘     └─────────────────┘                               │
│           │ YES                                                             │
│           ▼                                                                 │
│  ┌─────────────────┐     ┌─────────────────┐                               │
│  │ Model Validation│────►│ IllegalState    │──► {ERROR: true,              │
│  │                 │ NO  │   Exception     │    MESSAGE: "Model not found"}│
│  └────────┬────────┘     └─────────────────┘                               │
│           │ YES                                                             │
│           ▼                                                                 │
│  ┌─────────────────┐     ┌─────────────────┐                               │
│  │  SDK Operation  │────►│ RuntimeException│──► {ERROR: true,              │
│  │                 │FAIL │                 │    MESSAGE: "Unexpected..."}  │
│  └────────┬────────┘     └─────────────────┘                               │
│           │ SUCCESS                                                         │
│           ▼                                                                 │
│  ┌─────────────────┐                                                        │
│  │ {ERROR: false,  │                                                        │
│  │  MESSAGE: "...",│                                                        │
│  │  STATUS: "..."}│                                                        │
│  └─────────────────┘                                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

### 7.2 Error Categories

| Category | Exception Type | Cause | Response |
|----------|---------------|-------|----------|
| **Authorization** | `AccessDeniedException` | User not workspace admin | `{ERROR: true, MESSAGE: "Not authorized..."}` |
| **Validation** | `IllegalStateException` | Model not found/accessible | `{ERROR: true, MESSAGE: "Model does not exist..."}` |
| **SDK Failure** | `RuntimeException` | API call failure | `{ERROR: true, MESSAGE: "Unexpected error..."}` |
| **I/O Error** | `IOException` | File/network issues | `{ERROR: true, MESSAGE: "..."}` |

### 7.3 Error Handling Matrix

| Operation | Auth Error | Validation Error | SDK Error | I/O Error |
|-----------|------------|------------------|-----------|-----------|
| Upload Data | ✓ | ✓ | ✓ | ✓ |
| List Versions | ✓ | ✓ | ✓ | - |
| Change Version | ✓ | ✓ | ✓ | - |
| Retrain Model | ✓ | ✓ | ✓ | - |
| Get Status | ✓ | ✓ | ✓ | - |

### 7.4 Telemetry on Errors

All errors are tracked via `TelemetryHelper`:

```java
// On error, telemetry records:
telemetryHelper.recordOperationFailure(
    operationId,           // Unique operation identifier
    operationName,         // e.g., "uploadDataForRetraining"
    errorMessage           // Exception message
);
```

---

## 8. Security Architecture

### 8.1 Authorization Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Authorization Architecture                           │
│                                                                             │
│  ┌─────────────┐      ┌─────────────────┐      ┌─────────────────┐         │
│  │   Request   │─────►│  ModelStore     │─────►│  RoleManager    │         │
│  │             │      │                 │      │                 │         │
│  └─────────────┘      │ensureUserHas    │      │isWorkspaceAdmin │         │
│                       │Access()         │      │(accountId)      │         │
│                       └────────┬────────┘      └────────┬────────┘         │
│                                │                        │                   │
│                                │                        ▼                   │
│                                │               ┌─────────────────┐         │
│                                │               │Authorization    │         │
│                                │               │Context          │         │
│                                │               │                 │         │
│                                │               │getAuthorized    │         │
│                                │               │Account()        │         │
│                                │               └────────┬────────┘         │
│                                │                        │                   │
│                                ▼                        ▼                   │
│                       ┌─────────────────────────────────────────┐          │
│                       │  isAdmin = true  │  isAdmin = false     │          │
│                       │        │         │         │            │          │
│                       │        ▼         │         ▼            │          │
│                       │   Continue       │  AccessDenied        │          │
│                       │   Operation      │  Exception           │          │
│                       └─────────────────────────────────────────┘          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 8.2 Security Considerations

| Aspect | Implementation |
|--------|----------------|
| **Authentication** | Handled by Krista SDK (AuthorizationContext) |
| **Authorization** | Workspace admin check via RoleManager |
| **Data Access** | All operations require workspace admin role |
| **Audit Trail** | Telemetry records all operations with user context |

---

## 9. Dependency Injection Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      Dependency Injection Container                          │
│                              (HK2 / Krista SDK)                              │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         @Service Beans                               │   │
│  │                                                                      │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐      │   │
│  │  │   ModelStore    │  │ TelemetryHelper │  │  DatasetsArea   │      │   │
│  │  │   @Singleton    │  │   @Singleton    │  │                 │      │   │
│  │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘      │   │
│  │           │                    │                    │               │   │
│  │           │                    │                    │               │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐      │   │
│  │  │   ModelsArea    │  │  TrainingArea   │  │ KristaAiExtension│     │   │
│  │  │                 │  │                 │  │                 │      │   │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘      │   │
│  │                                                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      @Inject Dependencies                            │   │
│  │                                                                      │   │
│  │  ModelStore ──────────► AiModel, AiDataset, RoleManager,            │   │
│  │                         AuthorizationContext                         │   │
│  │                                                                      │   │
│  │  DatasetsArea ────────► ModelStore, TelemetryHelper, AiDataset      │   │
│  │                                                                      │   │
│  │  ModelsArea ──────────► ModelStore, TelemetryHelper, AiModel        │   │
│  │                                                                      │   │
│  │  TrainingArea ────────► ModelStore, TelemetryHelper, AiModel        │   │
│  │                                                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 10. File Structure

```
krista-ai/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── app/krista/extensions/krista/sdk/kristaai/
│   │   │       ├── KristaAiExtension.java          # Extension entry point
│   │   │       ├── catalog/
│   │   │       │   ├── DatasetsArea.java           # Dataset operations
│   │   │       │   ├── ModelsArea.java             # Model version operations
│   │   │       │   └── TrainingArea.java           # Training operations
│   │   │       ├── entity/
│   │   │       │   ├── Model.java                  # Model entity
│   │   │       │   └── store/
│   │   │       │       └── ModelStore.java         # Model data store
│   │   │       └── util/
│   │   │           ├── AiResponseKeys.java         # Response key constants
│   │   │           ├── ModelUtils.java             # Validation utilities
│   │   │           └── TelemetryHelper.java        # Telemetry utilities
│   │   └── resources/
│   │       └── ...
│   ├── test/
│   │   └── java/
│   │       └── ...
│   └── docs/
│       └── ARCHITECTURE.md                         # This document
└── ...
```

---

## 11. Extension Lifecycle

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Extension Lifecycle                                  │
│                                                                             │
│  ┌─────────────┐                                                            │
│  │   LOADING   │  Extension JAR loaded by Krista platform                   │
│  └──────┬──────┘                                                            │
│         │                                                                   │
│         ▼                                                                   │
│  ┌─────────────┐                                                            │
│  │ INITIALIZING│  @Extension annotation processed                           │
│  │             │  Static resources loaded                                   │
│  │             │  DI container initialized                                  │
│  └──────┬──────┘                                                            │
│         │                                                                   │
│         ▼                                                                   │
│  ┌─────────────┐                                                            │
│  │   READY     │  Extension ready to handle requests                        │
│  │             │  @CatalogRequest methods available                         │
│  │             │  Entity stores accessible                                  │
│  └──────┬──────┘                                                            │
│         │                                                                   │
│         ▼                                                                   │
│  ┌─────────────┐                                                            │
│  │  RUNNING    │  Processing catalog requests                               │
│  │             │  Telemetry recording                                       │
│  │             │  Authorization checks                                      │
│  └──────┬──────┘                                                            │
│         │                                                                   │
│         ▼                                                                   │
│  ┌─────────────┐                                                            │
│  │  SHUTDOWN   │  Extension unloaded                                        │
│  │             │  Resources released                                        │
│  └─────────────┘                                                            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 12. Appendix

### 12.1 Response Key Constants

| Constant | Value | Type | Description |
|----------|-------|------|-------------|
| `ERROR` | "Error" | Boolean | Indicates operation failure |
| `MESSAGE` | "Message" | Text | Human-readable status message |
| `STATUS` | "Status" | Text | Operation status code |
| `RETRAINING_JOB_ID` | "Retraining Job Id" | Identifier | Unique job identifier |

### 12.2 Telemetry Metrics

| Metric | Description |
|--------|-------------|
| `operation_start` | Records operation initiation with timestamp |
| `operation_success` | Records successful completion with duration |
| `operation_failure` | Records failure with error details |

### 12.3 Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-11-27 | Initial architecture document |
| 1.0.1 | 2025-11-27 | Added security and DI sections |
```

