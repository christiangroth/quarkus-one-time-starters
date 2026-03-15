package de.chrgroth.quarkus.starters.domain.port.out

import de.chrgroth.quarkus.starters.domain.port.`in`.ExecutionStatus
import java.time.Instant

interface RepositoryPort {
  fun lastStatus(starterId: String): ExecutionStatus?
  fun recordExecution(
    starterId: String,
    status: ExecutionStatus,
    startedAt: Instant,
    finishedAt: Instant,
    errorMessage: String?,
  )
}
