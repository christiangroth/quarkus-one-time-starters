package de.chrgroth.quarkus.starters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StarterCompletionFlagTests {

    private val flag = StarterCompletionFlag()

    @Test
    fun `isCompleted returns false by default`() {
        assertThat(flag.isCompleted()).isFalse()
    }

    @Test
    fun `isCompleted returns true after markCompleted`() {
        flag.markCompleted()

        assertThat(flag.isCompleted()).isTrue()
    }

    @Test
    fun `markCompleted is idempotent`() {
        flag.markCompleted()
        flag.markCompleted()

        assertThat(flag.isCompleted()).isTrue()
    }
}
