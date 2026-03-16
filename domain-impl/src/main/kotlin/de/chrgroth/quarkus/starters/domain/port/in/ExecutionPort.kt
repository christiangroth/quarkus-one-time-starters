package de.chrgroth.quarkus.starters.domain.port.`in`

import java.time.Instant

/**
 * Execution status of a one-time starter.
 *
 * - [PENDING] – not yet executed (implied when no record exists in the repository)
 * - [SUCCEEDED] – executed and completed without errors; will be skipped on subsequent startups
 * - [FAILED] – executed but threw an exception; will be retried on the next application restart
 */
enum class ExecutionStatus {
  PENDING,
  SUCCEEDED,
  FAILED,
}

/**
 * Inbound port for recording and querying the execution state of individual starters.
 *
 * Provides the bridge between the domain orchestration logic and the persistence layer.
 */
interface ExecutionPort {
  /**
   * Returns the last recorded [ExecutionStatus] for the starter with the given [starterId],
   * or `null` if the starter has never been executed.
   */
  fun lastStatus(starterId: String): ExecutionStatus?

  /**
   * Records the result of a single starter execution.
   *
   * @param starterId unique identifier of the starter
   * @param status outcome of this execution ([ExecutionStatus.SUCCEEDED] or [ExecutionStatus.FAILED])
   * @param startedAt instant when execution started
   * @param finishedAt instant when execution finished
   * @param errorMessage optional error message when [status] is [ExecutionStatus.FAILED]
   */
  fun recordExecution(starterId: String, status: ExecutionStatus, startedAt: Instant, finishedAt: Instant, errorMessage: String?)
}
