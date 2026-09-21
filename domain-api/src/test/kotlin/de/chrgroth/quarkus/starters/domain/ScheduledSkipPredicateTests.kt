package de.chrgroth.quarkus.starters.domain

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScheduledSkipPredicateTests {

    private val statusProvider = mockk<StartersStatusProvider>()
    private val predicate = ScheduledSkipPredicate(statusProvider)

    @Test
    fun `test returns true (skip) when starters have not completed`() {
        every { statusProvider.allCompleted() } returns false

        assertThat(predicate.test(mockk())).isTrue()
    }

    @Test
    fun `test returns false (run) when starters have completed`() {
        every { statusProvider.allCompleted() } returns true

        assertThat(predicate.test(mockk())).isFalse()
    }
}
