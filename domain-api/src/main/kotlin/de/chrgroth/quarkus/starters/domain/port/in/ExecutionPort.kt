package de.chrgroth.quarkus.starters.domain.port.`in`

import java.time.Instant

enum class ExecutionStatus {
  PENDING,
  SUCCEEDED,
  FAILED,
}

interface ExecutionPort {
  fun lastStatus(starterId: String): ExecutionStatus?
  fun recordExecution(starterId: String, status: ExecutionStatus, startedAt: Instant, finishedAt: Instant, errorMessage: String?)
}
