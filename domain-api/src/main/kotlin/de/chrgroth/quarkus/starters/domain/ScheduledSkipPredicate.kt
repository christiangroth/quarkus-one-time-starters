package de.chrgroth.quarkus.starters.domain

import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.ScheduledExecution
import jakarta.enterprise.inject.spi.CDI

/**
 * Quarkus [Scheduled.SkipPredicate] that blocks all scheduled jobs until the one-time starters
 * have completed successfully.
 *
 * Reads the completion state from [StartersStatusProvider] via CDI so that it works with any implementation.
 * Register this predicate on scheduled methods via `@Scheduled(skipExecutionIf = ScheduledSkipPredicate::class)`.
 */
class ScheduledSkipPredicate : Scheduled.SkipPredicate {

    override fun test(execution: ScheduledExecution): Boolean =
        !CDI.current().select(StartersStatusProvider::class.java).get().allCompleted()
}
