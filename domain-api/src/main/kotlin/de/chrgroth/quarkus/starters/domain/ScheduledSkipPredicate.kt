package de.chrgroth.quarkus.starters.domain

import de.chrgroth.quarkus.starters.domain.port.`in`.StartupPort
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.ScheduledExecution
import jakarta.enterprise.inject.spi.CDI

class ScheduledSkipPredicate : Scheduled.SkipPredicate {

    override fun test(execution: ScheduledExecution): Boolean =
        !CDI.current().select(StartupPort::class.java).get().isCompleted()
}
