package de.chrgroth.quarkus.starters

import java.time.Instant

interface StarterRepository {
    fun lastStatus(starterId: String): StarterStatus?
    fun recordExecution(starterId: String, status: StarterStatus, startedAt: Instant, finishedAt: Instant, errorMessage: String?)
}
