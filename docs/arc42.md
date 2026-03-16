# arc42 Architecture Documentation

## 1. Introduction and Goals

**Quarkus One-Time Starters** is a Quarkus framework extension that manages one-time startup tasks for Quarkus applications.

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

![Context and Scope](https://kroki.io/plantuml/svg/eNpVkLtOxTAMhnc_hekEQ16AAR2gC8MZ0BEbi09qStTciN0KhHh33FaAmHLxl99ffBClpnOKcKGvnBhrpJBBppArNUroS6olc9aTfkTGxl4pj5EByGtp2PPCsVRuUMlPNDJ2jzO1aRa8rTUGTxpK7vAT8C8Lu7edcXZ0GhK7zYObdEiCMZz_40_CzQ38EjIPeNpRDKnGnd928AUDKZ1JzOFY8lj6u-d8-RNscTGavdlcbY_SigD8fgCdu9mT8HpbbRxZBfartWhaVhqC-LJwM5MlEN73D7AWVmCLNMTSJIgK8jv7eW2JpqGzwIHzYNP-BskbhKA=)

---

## 4. Solution Strategy

- **Hexagonal / Clean Architecture**: Client API (`starter-api`) → Domain Ports (`domain-api`) → Domain Impl (business logic) → Adapter (persistence)
- **Clean client artifact**: `starter-api` exposes only `Starter` and `ScheduledSkipPredicate`; internal port interfaces are not on the client compile classpath
- **CDI-based extensibility**: User starters are auto-discovered via `Instance<Starter>`
- **Idempotency via persistence**: Each starter's last status is stored in MongoDB; `SUCCEEDED` starters are skipped on restart
- **Scheduler integration**: `ScheduledSkipPredicate` blocks all scheduled jobs until the `StartupPort` completion flag is set

---

## 5. Building Block View

### Module Overview

![Module structure](https://kroki.io/plantuml/svg/hVLLbgIxDLzvV0Tclw-o0IpK7YEb6n6BSVxqkdhR4kjQx793UWhhd0V7HY9nxo91Vkhagm_ygThCgmCshCiMrL2ePJqEVoH3Hpsmgj3AHs3CSQDiFiItzGoVxBWPXWc-GmOIFdMrWDT9WRrTCHs-oi1KwltJOmeXOMNfMEomlXS6lKyHnE1v3_Ds6voDxW1CRxYUhzJyCVebQVVLbr7m2SlEPwtftX-7Hx3EOsHFtGb8gW9VoWKtFG0jpkxZkS22QXgvbnfH6bKiJ7ElDAu_zjplbNjhccOkBJ7eq_d4kuWyG9_lwTiMyC4b4eb_fH_33w069H120yNNF1hJ49uPd1kZtz-wHqyHv_wG)

### Module Descriptions

| Module | Responsibility |
|--------|---------------|
| `starter-api` | Client-facing API: `Starter` interface, `ScheduledSkipPredicate` |
| `domain-api` | Internal port contracts: `ExecutionPort`, `StartupPort`, `ExecutionStatus`, `RepositoryPort` |
| `domain-impl` | Core orchestration: `ExecutionAdapter` (execution loop, metrics), `StartupAdapter` (startup event handler and completion flag) |
| `adapter-out-persistence-mongodb` | MongoDB persistence: `StarterDocumentRepository` (implements `RepositoryPort`), `StarterIndexInitializer` |

---

## 6. Runtime View

### Startup Sequence

![Startup sequence](https://kroki.io/plantuml/svg/nZPfSsMwFMbv-xTHXXWIoLfCZGOrIKzqWnyAmJx1wbQJ-SPr23vabsVudeAumsD58v36ndN07jyzPpQqMrRLLg2rPEw2gdmv4CbAHGwGUt6eNwvBjEc7kJI98uClrsbESWtEC7I0quXmAz1Do5302tbv2vpGzwZ6imTOsJDO27qR0yjawN3TSSB4PBaSb6x8xJSHNQsV36VaINzM4PUtSxfrCE6NY6iSprDUFJheLuJphMrhb9zsIu50HgS0oVooRSQApbWBrbaAjO_gOJ3YUfco4LMGKZpjcI4hdEYsxZwnmw8uPh5tumXKIhM15B_LZZKsklWrjGNSwgQjmEcoWCgwfug4bZ8GKyGrAijilkmF4gIoJxC2dYynh3NNGBc4R-cOlb97sci1Fb0a9-GnF61pbwUvS6rdDvs4dIJ7jqax_jvH8-JlfXWI-z5E1c2u27v1nDR2AZlSeTNCFO34u-87KF11j7sQzTOnhf7-Hw)

### Key Runtime Behaviors

- Starters run **once on application startup** in `NORMAL` (production) mode
- In `DEV` and `TEST` modes, starters are skipped and the completion flag is immediately set
- Failed starters cause the application to **remain blocked** (scheduler stays paused until the next restart)
- Successful starters are never re-executed, even across restarts

---

## 7. Deployment View

The library is deployed as Maven artifacts to GitHub Packages:

- `de.chrgroth.quarkus.starters:starter-api`
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
| `ExecutionStatus.PENDING` is defined but not persisted | The status currently acts as documentation; `null` return from `lastStatus()` implies pending |
| No retry mechanism within a single run | By design — failed starters are retried on the next application start |
| MongoDB index sync runs on every startup | Lightweight operation; no impact expected in production workloads |
