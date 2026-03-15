package de.chrgroth.quarkus.starters.domain

interface Starter {
    val id: String
    fun execute()
}
