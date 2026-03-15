package de.chrgroth.quarkus.starters.domain.port.`in`

interface StartupPort {
  fun markCompleted()
  fun isCompleted(): Boolean
}
