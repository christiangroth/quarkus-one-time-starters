package de.chrgroth.quarkus.starters.domain

/**
 * Contract for a one-time startup task.
 *
 * Implement this interface and register the implementation as a CDI bean to have it discovered and
 * executed automatically on application startup. Each starter runs exactly once; once it succeeds,
 * it is skipped on subsequent restarts.
 */
interface Starter {
    /** Unique identifier for this starter, used to track execution state in the repository. */
    val id: String

    /** Executes the startup task. Throw any exception to signal failure. */
    fun execute()
}
