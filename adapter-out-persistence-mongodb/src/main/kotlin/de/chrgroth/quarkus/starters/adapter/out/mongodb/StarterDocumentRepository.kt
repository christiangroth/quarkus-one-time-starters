package de.chrgroth.quarkus.starters.adapter.out.mongodb

import de.chrgroth.quarkus.starters.domain.port.`in`.ExecutionStatus
import de.chrgroth.quarkus.starters.domain.port.out.RepositoryPort
import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class StarterDocumentRepository(
  private val metricsRecorder: MetricsRecorder,
) : RepositoryPort, PanacheMongoRepositoryBase<StarterDocument, String> {

  override fun lastStatus(starterId: String): ExecutionStatus? =
    metricsRecorder.timed(OPERATION_FIND_BY_ID) { findById(starterId) }?.lastStatus?.let { ExecutionStatus.valueOf(it) }

  override fun recordExecution(
    starterId: String,
    status: ExecutionStatus,
    startedAt: Instant,
    finishedAt: Instant,
    errorMessage: String?,
  ) {
    val existingDoc = metricsRecorder.timed(OPERATION_FIND_BY_ID) { findById(starterId) }
    val doc = existingDoc ?: StarterDocument().apply { this.starterId = starterId }
    doc.lastStatus = status.name
    val execution = StarterExecutionDocument().apply {
      this.startedAt = startedAt
      this.finishedAt = finishedAt
      this.status = status.name
      this.errorMessage = errorMessage
    }
    doc.executions = doc.executions + execution
    metricsRecorder.timed(OPERATION_SAVE) { persistOrUpdate(doc) }
  }

  companion object {
    const val OPERATION_FIND_BY_ID = "starters.findById"
    const val OPERATION_SAVE = "starters.save"
  }
}
