package de.chrgroth.quarkus.starters.domain.port.`in`

/**
 * Inbound port for managing the one-time starter completion flag.
 *
 * Implementations track whether all starters have finished successfully so that
 * [de.chrgroth.quarkus.starters.domain.ScheduledSkipPredicate] can unblock the Quarkus scheduler.
 */
interface StartupPort {
  /** Marks all starters as completed, allowing the Quarkus scheduler to proceed. */
  fun markCompleted()

  /** Returns `true` once [markCompleted] has been called, `false` otherwise. */
  fun isCompleted(): Boolean
}
