package de.chrgroth.quarkus.starters.domain

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StartupAdapterTests {

    private val flag = StartupAdapter(mockk())

    @Test
    fun `allCompleted returns false by default`() {
        assertThat(flag.allCompleted()).isFalse()
    }

    @Test
    fun `allCompleted returns true after markCompleted`() {
        flag.markCompleted()

        assertThat(flag.allCompleted()).isTrue()
    }

    @Test
    fun `markCompleted is idempotent`() {
        flag.markCompleted()
        flag.markCompleted()

        assertThat(flag.allCompleted()).isTrue()
    }
}
