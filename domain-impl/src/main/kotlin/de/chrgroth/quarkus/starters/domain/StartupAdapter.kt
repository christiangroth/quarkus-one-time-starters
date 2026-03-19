package de.chrgroth.quarkus.starters.domain

import de.chrgroth.quarkus.starters.domain.port.`in`.StartupPort
import de.chrgroth.quarkus.starters.domain.StartersStatusProvider
import io.quarkus.runtime.LaunchMode
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import mu.KLogging
import java.util.concurrent.atomic.AtomicBoolean

@ApplicationScoped
@Suppress("Unused", "UnusedParameter")
class StartupAdapter(
  private val startersAdapter: ExecutionAdapter,
) : StartupPort, StartersStatusProvider {

  private val completed = AtomicBoolean(false)

  override fun markCompleted() {
    completed.set(true)
  }

  override fun allCompleted(): Boolean =
    completed.get()

  fun onStart(@Observes event: StartupEvent) {
    if (LaunchMode.current() != LaunchMode.NORMAL) {
      logger.info { "Skipping starters – not in NORMAL (prod) mode. Marking completion flag immediately." }
      markCompleted()
      return
    }

    val allSucceeded = startersAdapter.runAll()
    if (allSucceeded) {
      markCompleted()
      logger.info { "All starters succeeded – scheduler unblocked." }
    } else {
      logger.warn { "Some starters failed – scheduler remains blocked until next application start." }
    }
  }

  companion object : KLogging()
}
