package de.chrgroth.quarkus.starters.domain

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TestStatusProvider : StartersStatusProvider {
    var completed: Boolean = false

    override fun allCompleted(): Boolean = completed
}
