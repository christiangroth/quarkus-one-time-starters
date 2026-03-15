package de.chrgroth.quarkus.starters.domain

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.quarkus.runtime.LaunchMode
import io.quarkus.runtime.StartupEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StarterStartupTests {Scheduler

    private val startersAdapter: ExecutionAdapter = mockk()
    private val completionFlag: StartupAdapter = mockk()

    private val startup = StarterStartup(startersAdapter, completionFlag)

    @BeforeEach
    fun setup() {
        mockkStatic(LaunchMode::class)
        justRun { completionFlag.markCompleted() }
    }

    @AfterEach
    fun teardown() {
        unmockkStatic(LaunchMode::class)
    }

    @Test
    fun `onStart skips starters and marks completion immediately in DEV mode`() {
        every { LaunchMode.current() } returns LaunchMode.DEVELOPMENT

        startup.onStart(StartupEvent())

        verify(exactly = 0) { startersAdapter.runAll() }
        verify { completionFlag.markCompleted() }
    }

    @Test
    fun `onStart skips starters and marks completion immediately in TEST mode`() {
        every { LaunchMode.current() } returns LaunchMode.TEST

        startup.onStart(StartupEvent())

        verify(exactly = 0) { startersAdapter.runAll() }
        verify { completionFlag.markCompleted() }
    }

    @Test
    fun `onStart runs starters and marks completion when all succeed in NORMAL mode`() {
        every { LaunchMode.current() } returns LaunchMode.NORMAL
        every { startersAdapter.runAll() } returns true

        startup.onStart(StartupEvent())

        verify { startersAdapter.runAll() }
        verify { completionFlag.markCompleted() }
    }

    @Test
    fun `onStart runs starters but does not mark completion when some fail in NORMAL mode`() {
        every { LaunchMode.current() } returns LaunchMode.NORMAL
        every { startersAdapter.runAll() } returns false

        startup.onStart(StartupEvent())

        verify { startersAdapter.runAll() }
        verify(exactly = 0) { completionFlag.markCompleted() }
    }
}
