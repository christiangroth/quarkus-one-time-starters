package de.chrgroth.quarkus.starters.domain.port.`in`

/**
 * Exposes the one-time starter completion state to applications using this library.
 *
 * Implementations indicate whether all starters have finished successfully so that
 * [de.chrgroth.quarkus.starters.domain.ScheduledSkipPredicate] can unblock the Quarkus scheduler.
 */
interface StartupStatus {
  /** Returns `true` once all starters have completed, `false` otherwise. */
  fun allCompleted(): Boolean
}
