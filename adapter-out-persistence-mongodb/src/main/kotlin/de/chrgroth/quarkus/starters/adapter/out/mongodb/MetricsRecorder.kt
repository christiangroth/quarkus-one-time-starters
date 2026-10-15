package de.chrgroth.quarkus.starters.adapter.out.mongodb

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import jakarta.enterprise.context.ApplicationScoped
import mu.KLogging
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@ApplicationScoped
class MetricsRecorder(
  private val meterRegistry: MeterRegistry,
  @param:ConfigProperty(name = "starters.mongodb.slow-query-threshold-ms", defaultValue = DEFAULT_SLOW_QUERY_THRESHOLD_MS)
  private val slowQueryThresholdMs: Long,
) {

  private val timers = ConcurrentHashMap<String, Timer>()
  private val slowQueryCounters = ConcurrentHashMap<String, Counter>()

  fun <T> timed(operation: String, block: () -> T): T {
    val startNanos = System.nanoTime()
    try {
      return block()
    } finally {
      record(operation, System.nanoTime() - startNanos)
    }
  }

  private fun record(operation: String, durationNanos: Long) {
    timers.getOrPut(operation) {
      Timer.builder(QUERY_TIMER)
        .tag(OPERATION_TAG, operation)
        .register(meterRegistry)
    }.record(durationNanos, TimeUnit.NANOSECONDS)

    val durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos)
    if (durationMs >= slowQueryThresholdMs) {
      logger.warn { "Slow MongoDB query detected: operation=$operation duration=${durationMs}/${slowQueryThresholdMs}ms" }
      slowQueryCounters.getOrPut(operation) {
        meterRegistry.counter(SLOW_QUERY_COUNTER, OPERATION_TAG, operation)
      }.increment()
    }
  }

  companion object : KLogging() {
    const val DEFAULT_SLOW_QUERY_THRESHOLD_MS = "100"
    const val QUERY_TIMER = "mongodb.query"
    const val SLOW_QUERY_COUNTER = "mongodb.slow.queries"
    const val OPERATION_TAG = "operation"
  }
}
