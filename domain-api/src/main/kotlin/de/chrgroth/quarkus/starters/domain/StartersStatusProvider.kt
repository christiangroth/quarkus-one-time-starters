package de.chrgroth.quarkus.starters.domain

/**
 * Exposes the one-time starter completion state to applications using this library.
 *
 * Implementations indicate whether all starters have finished successfully so that
 * [ScheduledSkipPredicate] can unblock the Quarkus scheduler.
 */
interface StartersStatusProvider {
  /** Returns `true` once all starters have completed, `false` otherwise. */
  fun allCompleted(): Boolean
}
