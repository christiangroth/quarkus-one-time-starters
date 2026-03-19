package de.chrgroth.quarkus.starters.domain

import de.chrgroth.quarkus.starters.domain.port.`in`.StartupStatus
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.ScheduledExecution
import jakarta.enterprise.inject.spi.CDI

/**
 * Quarkus [Scheduled.SkipPredicate] that blocks all scheduled jobs until the one-time starters
 * have completed successfully.
 *
 * Reads the completion state from [StartupStatus] via CDI so that it works with any implementation.
 * Register this predicate on scheduled methods via `@Scheduled(skipExecutionIf = ScheduledSkipPredicate::class)`.
 */
class ScheduledSkipPredicate : Scheduled.SkipPredicate {

    override fun test(execution: ScheduledExecution): Boolean =
        !CDI.current().select(StartupStatus::class.java).get().allCompleted()
}
