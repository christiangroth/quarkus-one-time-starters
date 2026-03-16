package de.chrgroth.quarkus.starters.domain

import de.chrgroth.quarkus.starters.domain.port.`in`.StartupPort
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.ScheduledExecution
import jakarta.enterprise.inject.spi.CDI

/**
 * Quarkus [Scheduled.SkipPredicate] that blocks all scheduled jobs until the one-time starters
 * have completed successfully.
 *
 * Reads the completion state from [StartupPort] via CDI so that it works with any implementation.
 * Register this predicate on scheduled methods via `@Scheduled(skipExecutionIf = ScheduledSkipPredicate::class)`.
 */
class ScheduledSkipPredicate : Scheduled.SkipPredicate {

    override fun test(execution: ScheduledExecution): Boolean =
        !CDI.current().select(StartupPort::class.java).get().isCompleted()
}
