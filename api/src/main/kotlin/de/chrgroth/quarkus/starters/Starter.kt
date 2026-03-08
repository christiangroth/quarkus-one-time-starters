package de.chrgroth.quarkus.starters

interface Starter {
    val id: String
    fun execute()
}
