# quarkus-one-time-starters

A Quarkus extension for managing **one-time startup tasks**.

Register tasks that must run exactly once during the application lifecycle. The framework persists execution results to MongoDB, skips already-succeeded tasks on restart, blocks the Quarkus scheduler until all tasks complete, and exposes metrics via Micrometer.

---

## Features

- **One-time execution** — tasks that succeeded are skipped on subsequent restarts
- **Failure recovery** — failed tasks are retried on the next application start
- **Scheduler integration** — all Quarkus-scheduled jobs are paused until starters complete
- **Micrometer metrics** — per-starter execution duration and status gauges
- **Dev/Test bypass** — starters are skipped in non-production launch modes

---

## Modules

| Module | Description |
|--------|-------------|
| `domain-api` | Client-facing API: `Starter` interface, `ScheduledSkipPredicate`, `StartersStatusProvider` |
| `domain-impl` | Core orchestration, internal port contracts: `ExecutionAdapter`, `StartupAdapter` |
| `adapter-out-persistence-mongodb` | MongoDB persistence adapter via Quarkus Panache |

---

## Getting Started

### 1. Add dependencies

```kotlin
// Gradle (Kotlin DSL)
implementation("de.chrgroth.quarkus.starters:domain-api:<version>")
implementation("de.chrgroth.quarkus.starters:domain-impl:<version>")
implementation("de.chrgroth.quarkus.starters:adapter-out-persistence-mongodb:<version>")
```

Artifacts are published to [GitHub Packages](https://github.com/christiangroth/quarkus-one-time-starters/packages).

### 2. Implement a starter

```kotlin
import de.chrgroth.quarkus.starters.domain.Starter
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MyDatabaseMigrationStarter : Starter {

    override val id = "my-database-migration"

    override fun execute() {
        // Perform one-time migration logic here
        // Throw any exception to mark this starter as failed
    }
}
```

The `id` must be **unique** across all starters. Starters are executed in **alphabetical order by id**.

### 3. Block scheduled jobs (optional)

Use `ScheduledSkipPredicate` on any `@Scheduled` method to pause it until all starters complete:

```kotlin
import de.chrgroth.quarkus.starters.domain.ScheduledSkipPredicate
import io.quarkus.scheduler.Scheduled

@Scheduled(every = "10s", skipExecutionIf = ScheduledSkipPredicate::class)
fun periodicJob() {
    // Only runs after all starters have succeeded
}
```

---

## Metrics

| Metric | Type | Tags | Description |
|--------|------|------|-------------|
| `starter_execution_duration_seconds` | Timer | `id`, `status` | Execution time per starter per run |
| `starter_overall_status` | Gauge | `id` | `1.0` = SUCCEEDED, `0.0` = FAILED |

---

## MongoDB

Execution records are stored in the `starters` collection. Each document holds the starter's last status and a full execution history.

---

## Build & Development

```bash
# Build (includes tests, detekt, and coverage check)
./gradlew build

# Run tests only
./gradlew test
```

Requires Java 25 and a `GHCR_PAT` environment variable with read access to GitHub Packages (for plugin resolution).

---

## Architecture

See [docs/arc42.md](docs/arc42.md) for the full arc42 architecture documentation.

---

## License

[MIT](LICENSE)
