package de.chrgroth.quarkus.starters.domain

import de.chrgroth.quarkus.starters.domain.port.`in`.ExecutionPort
import de.chrgroth.quarkus.starters.domain.port.`in`.ExecutionStatus
import de.chrgroth.quarkus.starters.domain.port.out.RepositoryPort
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
class ExecutionAdapter(
  private val starters: Instance<Starter>,
  private val repository: RepositoryPort,
  private val meterRegistry: MeterRegistry,
) : ExecutionPort {

  private val statusGauges = ConcurrentHashMap<String, AtomicInteger>()

  override fun lastStatus(starterId: String): ExecutionStatus? =
    repository.lastStatus(starterId)

  override fun recordExecution(starterId: String, status: ExecutionStatus, startedAt: Instant, finishedAt: Instant, errorMessage: String?) =
    repository.recordExecution(starterId, status, startedAt, finishedAt, errorMessage)

  fun runAll(): Boolean {
    val sortedStarters = starters.stream().toList().sortedBy { it.id }
    logger.info { "Running ${sortedStarters.size} starter(s)" }

    var allSucceeded = true
    for (starter in sortedStarters) {
      val existingStatus = repository.lastStatus(starter.id)
      if (existingStatus == ExecutionStatus.SUCCEEDED) {
        logger.info { "Skipping starter ${starter.id} – already SUCCEEDED" }
        updateGauge(starter.id, ExecutionStatus.SUCCEEDED)
        continue
      }

      logger.info { "Executing starter ${starter.id}" }
      val startedAt = Instant.now()
      val timerSample = Timer.start(meterRegistry)
      try {
        starter.execute()
        val finishedAt = Instant.now()
        repository.recordExecution(starter.id, ExecutionStatus.SUCCEEDED, startedAt, finishedAt, null)
        timerSample.stop(
          Timer.builder("starter_execution_duration_seconds")
            .tag("id", starter.id)
            .tag("status", ExecutionStatus.SUCCEEDED.name)
            .register(meterRegistry),
        )
        updateGauge(starter.id, ExecutionStatus.SUCCEEDED)
        logger.info { "Starter ${starter.id} SUCCEEDED" }
      } catch (e: Exception) {
        val finishedAt = Instant.now()
        repository.recordExecution(starter.id, ExecutionStatus.FAILED, startedAt, finishedAt, e.message)
        timerSample.stop(
          Timer.builder("starter_execution_duration_seconds")
            .tag("id", starter.id)
            .tag("status", ExecutionStatus.FAILED.name)
            .register(meterRegistry),
        )
        updateGauge(starter.id, ExecutionStatus.FAILED)
        logger.error(e) { "Starter ${starter.id} FAILED: ${e.message}" }
        allSucceeded = false
      }
    }

    return allSucceeded
  }

  private fun updateGauge(starterId: String, status: ExecutionStatus) {
    val gaugeValue = statusGauges.getOrPut(starterId) {
      val atomicInt = AtomicInteger(0)
      Gauge.builder("starter_overall_status", atomicInt) { it.get().toDouble() }
        .tag("id", starterId)
        .register(meterRegistry)
      atomicInt
    }
    gaugeValue.set(if (status == ExecutionStatus.SUCCEEDED) 1 else 0)
  }

  companion object : KLogging()
}
