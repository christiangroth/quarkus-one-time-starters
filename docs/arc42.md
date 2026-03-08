# arc42 Architecture Documentation

## 1. Introduction and Goals

**Quarkus One-Time Starters** is a Quarkus framework extension that manages idempotent, one-time startup tasks for Quarkus applications.

### Requirements Overview

| # | Requirement |
|---|-------------|
| R1 | Allow applications to register one-time startup tasks (`Starter` implementations) |
| R2 | Track the execution status of each starter persistently (MongoDB) |
| R3 | Skip starters that have already succeeded in a previous run |
| R4 | Block the Quarkus scheduler until all starters complete successfully |
| R5 | Expose per-starter execution metrics via Micrometer |
| R6 | Run in production mode only (skip in DEV / TEST) |

### Quality Goals

| Priority | Goal | Description |
|----------|------|-------------|
| 1 | Reliability | Starters are retried on restart after failure; completed starters are never re-executed |
| 2 | Observability | Execution duration and status are exposed as Micrometer metrics |
| 3 | Extensibility | New starters are registered via CDI — no framework changes needed |

---

## 2. Constraints

- Requires Quarkus (CDI, Scheduler, Micrometer)
- Persistence adapter requires MongoDB (via Quarkus Panache MongoDB Kotlin)
- JVM 25 / Kotlin 2.x
- Published to GitHub Packages

---

## 3. Context and Scope

The library integrates into a Quarkus application as a set of CDI beans. Application developers implement the `Starter` interface and register it as a CDI bean. The framework handles execution, persistence, and scheduling.

```
┌─────────────────────────────────────────────────┐
│              Quarkus Application                │
│                                                 │
│  ┌──────────────────────────────────────────┐   │
│  │    quarkus-one-time-starters             │   │
│  │  (StarterStartup, StarterService, ...)   │   │
│  └──────────────────────────────────────────┘   │
│                                                 │
│  ┌─────────────────┐   ┌────────────────────┐   │
│  │  User-defined   │   │   MongoDB          │   │
│  │  Starter impls  │   │   (starters coll.) │   │
│  └─────────────────┘   └────────────────────┘   │
└─────────────────────────────────────────────────┘
```

---

## 4. Solution Strategy

- **Hexagonal / Clean Architecture**: Domain API (interfaces) → Domain Impl (business logic) → Adapter (persistence)
- **CDI-based extensibility**: User starters are auto-discovered via `Instance<Starter>`
- **Idempotency via persistence**: Each starter's last status is stored in MongoDB; `SUCCEEDED` starters are skipped on restart
- **Scheduler integration**: `StarterSkipPredicate` blocks all scheduled jobs until the `StarterCompletionFlag` is set

---

## 5. Building Block View

### Module Overview

![Module structure](https://kroki.io/plantuml/svg/eNqFUttKBDEMfe9X1H2f_QBZhgVF2DfZ-YLYxjFMm5Q2I66XfzcKCuNefM3hXHKSbVOoOufkrvQJM_qSgNi1ibhAheyD5CKMrIMeEvqKQYHHhM4VCBOM6FdRsnE6KLTym02WOCfse__mvCdWrI8Q0A9fPlhPzfZYpJFKPRgaErT2g9yYeUIl4bsE4190mKjcV4wUQNFA5Dn_Ygo6N_dxHJNM8ijnUhfrMwU8Gn83VRaaEKEY1MmsXcHaqClywC4LjxIfLvvcSpizNXt-_x1HfNkxKUGiV2vPvJd7rNf98gDXPmJBjs0Lu__zXeafDWq89_7E_bbGtGf6BD991Kg=)

### Module Descriptions

| Module | Responsibility |
|--------|---------------|
| `domain-api` | Public contracts: `Starter`, `StarterRepository`, `StarterStatus`, `StarterCompletionFlag`, `StarterSkipPredicate` |
| `domain-impl` | Core orchestration: `StarterService` (execution loop, metrics), `StarterStartup` (startup event handler) |
| `adapter-out-persistence-mongodb` | MongoDB persistence: `StarterDocumentRepository` (implements `StarterRepository`), `StarterIndexInitializer` |

---

## 6. Runtime View

### Startup Sequence

![Startup sequence](https://kroki.io/plantuml/svg/eNqdk99qwjAUxu_7FEevKmOw3Q4cinYwsBu27AGy5FjD0iTkj-jbL22qrKWOzYsm5Zzv_Pqd02RhHTHO1yKZuD3WCFoQLhMdgpxyTaSD6dYT8-XtFIiFbS9VNsVo2s3r0RSaA6fYB3Y54LUWLbUcKy1QK8udMqdGUvQkObaCilsX03mSbOH-eeAInqB7yw4oXUKEgw3xku5zxRAmc3h7L_LlJoFh4RiqDmNYqeA5fJylswSFxZ-4-Z9wcSABZ7xcChE4AEIpDTtlAAndn5WQWhV2Bp8n4KyRwRASsEUgCWJdyDhv07Ow6ZQIg4SdoPxYrbJsna3bzBgkDxCvGXEIFfEVpo-R0naoUTIuKwj2doQLZFcxZcDgEal3mM46VWPEekrR2i5yrQuDVBmWtfVcyfRie_ZLYX4pBMfrMLS7fgddD3ikqBvqPz28LF83Nxp4uBiQcWJxj-uQM3bciBBlMzhk7cjjH-2Fbjq10ULzLMISbv43aKlPJA==)

### Key Runtime Behaviors

- Starters run **once on application startup** in `NORMAL` (production) mode
- In `DEV` and `TEST` modes, starters are skipped and the completion flag is immediately set
- Failed starters cause the application to **remain blocked** (scheduler stays paused until the next restart)
- Successful starters are never re-executed, even across restarts

---

## 7. Deployment View

The library is deployed as Maven artifacts to GitHub Packages:

- `de.chrgroth.quarkus.starters:domain-api`
- `de.chrgroth.quarkus.starters:domain-impl`
- `de.chrgroth.quarkus.starters:adapter-out-persistence-mongodb`

Consumer applications declare dependencies on the relevant artifacts and implement the `Starter` interface.

---

## 8. Cross-Cutting Concepts

### Metrics (Micrometer)

| Metric | Type | Tags | Description |
|--------|------|------|-------------|
| `starter_execution_duration_seconds` | Timer | `id`, `status` | Execution duration per starter |
| `starter_overall_status` | Gauge | `id` | `1` = SUCCEEDED, `0` = FAILED |

### Persistence (MongoDB)

Documents are stored in the `starters` collection. Each document represents one starter and contains its last status and a full execution history list.

### Error Handling

Exceptions thrown by a `Starter.execute()` are caught, the failure is recorded, and execution continues with the remaining starters. The `allSucceeded` flag is set to `false`, preventing the scheduler from unblocking.

---

## 9. Architecture Decisions

| ID | Decision | Rationale |
|----|----------|-----------|
| AD1 | MongoDB as persistence backend | Native Quarkus support via Panache; document model fits per-starter history |
| AD2 | CDI `Instance<Starter>` for auto-discovery | Zero configuration for new starters; follows Quarkus conventions |
| AD3 | Alphabetical execution order | Deterministic and reproducible ordering without explicit priority |
| AD4 | Skip non-NORMAL launch modes | Avoids blocking dev/test environments with potentially slow startup tasks |
| AD5 | `AtomicBoolean` completion flag | Thread-safe flag without CDI scope complications |

---

## 10. Quality Requirements

| Scenario | Response |
|----------|----------|
| A starter throws a `RuntimeException` | The failure is logged, recorded, and remaining starters continue executing |
| Application restarts after a partial failure | Only failed/pending starters are re-executed; succeeded starters are skipped |
| Multiple starters are registered | All run in alphabetical order; failure of one does not skip subsequent starters |
| Application starts in DEV mode | All starters are bypassed; scheduler is unblocked immediately |

---

## 11. Risks and Technical Debt

| Risk | Mitigation |
|------|-----------|
| `StarterStatus.PENDING` is defined but not persisted | The status currently acts as documentation; `null` return from `lastStatus()` implies pending |
| No retry mechanism within a single run | By design — failed starters are retried on the next application start |
| MongoDB index sync runs on every startup | Lightweight operation; no impact expected in production workloads |
