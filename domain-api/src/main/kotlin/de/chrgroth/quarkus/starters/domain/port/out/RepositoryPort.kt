package de.chrgroth.quarkus.starters.domain.port.out

import de.chrgroth.quarkus.starters.domain.port.`in`.ExecutionStatus
import java.time.Instant

/**
 * Outbound port for persisting and querying starter execution history.
 *
 * Implemented by persistence adapters (e.g. MongoDB) and injected into the domain layer.
 */
interface RepositoryPort {
  /**
   * Returns the last recorded [ExecutionStatus] for the starter with the given [starterId],
   * or `null` if the starter has never been executed.
   */
  fun lastStatus(starterId: String): ExecutionStatus?

  /**
   * Persists the result of a single starter execution.
   *
   * @param starterId unique identifier of the starter
   * @param status outcome of this execution ([ExecutionStatus.SUCCEEDED] or [ExecutionStatus.FAILED])
   * @param startedAt instant when execution started
   * @param finishedAt instant when execution finished
   * @param errorMessage optional error message when [status] is [ExecutionStatus.FAILED]
   */
  fun recordExecution(
    starterId: String,
    status: ExecutionStatus,
    startedAt: Instant,
    finishedAt: Instant,
    errorMessage: String?,
  )
}
