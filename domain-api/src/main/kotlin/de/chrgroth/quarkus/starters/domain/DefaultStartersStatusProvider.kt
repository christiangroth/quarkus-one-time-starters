package de.chrgroth.quarkus.starters.domain

import io.quarkus.arc.DefaultBean
import jakarta.enterprise.context.ApplicationScoped

/**
 * Fallback [StartersStatusProvider] that reports all starters as completed.
 *
 * This bean is only active when no other [StartersStatusProvider] is present in the CDI context.
 * In a full application (using `domain-impl`) [StartupAdapter] takes precedence.
 */
@DefaultBean
@ApplicationScoped
class DefaultStartersStatusProvider : StartersStatusProvider {
    override fun allCompleted(): Boolean = true
}
