package de.chrgroth.quarkus.starters

import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class StarterDocumentRepository : StarterRepository, PanacheMongoRepositoryBase<StarterDocument, String> {

    override fun lastStatus(starterId: String): StarterStatus? =
        findById(starterId)?.lastStatus?.let { StarterStatus.valueOf(it) }

    override fun recordExecution(
        starterId: String,
        status: StarterStatus,
        startedAt: Instant,
        finishedAt: Instant,
        errorMessage: String?,
    ) {
        val existingDoc = findById(starterId)
        val doc = existingDoc ?: StarterDocument().apply { this.starterId = starterId }
        doc.lastStatus = status.name
        val execution = StarterExecutionDocument().apply {
            this.startedAt = startedAt
            this.finishedAt = finishedAt
            this.status = status.name
            this.errorMessage = errorMessage
        }
        doc.executions = doc.executions + execution
        persistOrUpdate(doc)
    }
}
