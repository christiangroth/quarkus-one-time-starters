package de.chrgroth.quarkus.starters.domain

import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.ScheduledExecution
import jakarta.enterprise.context.ApplicationScoped

/**
 * Quarkus [Scheduled.SkipPredicate] that blocks all scheduled jobs until the one-time starters
 * have completed successfully.
 *
 * Reads the completion state from [StartersStatusProvider] via injection.
 * Register this predicate on scheduled methods via `@Scheduled(skipExecutionIf = ScheduledSkipPredicate::class)`.
 */
@ApplicationScoped
class ScheduledSkipPredicate(private val statusProvider: StartersStatusProvider) : Scheduled.SkipPredicate {

    override fun test(execution: ScheduledExecution): Boolean =
        !statusProvider.allCompleted()
}
