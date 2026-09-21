package de.chrgroth.quarkus.starters.domain

import io.mockk.mockk
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class ScheduledSkipPredicateCdiTest {

    @Inject
    lateinit var predicate: ScheduledSkipPredicate

    @Inject
    lateinit var statusProvider: TestStatusProvider

    @BeforeEach
    fun reset() {
        statusProvider.completed = false
    }

    @Test
    fun `predicate is injectable as CDI bean`() {
        assertThat(predicate).isNotNull()
    }

    @Test
    fun `predicate skips when starters have not completed`() {
        statusProvider.completed = false

        assertThat(predicate.test(mockk())).isTrue()
    }

    @Test
    fun `predicate does not skip when starters have completed`() {
        statusProvider.completed = true

        assertThat(predicate.test(mockk())).isFalse()
    }
}
