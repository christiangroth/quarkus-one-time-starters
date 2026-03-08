package de.chrgroth.quarkus.starters

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import mu.KLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@ApplicationScoped
@Suppress("TooGenericExceptionCaught")
class StarterService(
    private val starters: Instance<Starter>,
    private val repository: StarterRepository,
    private val meterRegistry: MeterRegistry,
) {

    private val statusGauges = ConcurrentHashMap<String, AtomicInteger>()

    fun runAll(): Boolean {
        val sortedStarters = starters.stream().toList().sortedBy { it.id }
        logger.info { "Running ${sortedStarters.size} starter(s)" }

        var allSucceeded = true
        for (starter in sortedStarters) {
            val existingStatus = repository.lastStatus(starter.id)
            if (existingStatus == StarterStatus.SUCCEEDED) {
                logger.info { "Skipping starter ${starter.id} – already SUCCEEDED" }
                updateGauge(starter.id, StarterStatus.SUCCEEDED)
                continue
            }

            logger.info { "Executing starter ${starter.id}" }
            val startedAt = Instant.now()
            val timerSample = Timer.start(meterRegistry)
            try {
                starter.execute()
                val finishedAt = Instant.now()
                repository.recordExecution(starter.id, StarterStatus.SUCCEEDED, startedAt, finishedAt, null)
                timerSample.stop(
                    Timer.builder("starter_execution_duration_seconds")
                        .tag("id", starter.id)
                        .tag("status", StarterStatus.SUCCEEDED.name)
                        .register(meterRegistry),
                )
                updateGauge(starter.id, StarterStatus.SUCCEEDED)
                logger.info { "Starter ${starter.id} SUCCEEDED" }
            } catch (e: Exception) {
                val finishedAt = Instant.now()
                repository.recordExecution(starter.id, StarterStatus.FAILED, startedAt, finishedAt, e.message)
                timerSample.stop(
                    Timer.builder("starter_execution_duration_seconds")
                        .tag("id", starter.id)
                        .tag("status", StarterStatus.FAILED.name)
                        .register(meterRegistry),
                )
                updateGauge(starter.id, StarterStatus.FAILED)
                logger.error(e) { "Starter ${starter.id} FAILED: ${e.message}" }
                allSucceeded = false
            }
        }
        return allSucceeded
    }

    private fun updateGauge(starterId: String, status: StarterStatus) {
        val gaugeValue = statusGauges.getOrPut(starterId) {
            val atomicInt = AtomicInteger(0)
            Gauge.builder("starter_overall_status", atomicInt) { it.get().toDouble() }
                .tag("id", starterId)
                .register(meterRegistry)
            atomicInt
        }
        gaugeValue.set(if (status == StarterStatus.SUCCEEDED) 1 else 0)
    }

    companion object : KLogging()
}
