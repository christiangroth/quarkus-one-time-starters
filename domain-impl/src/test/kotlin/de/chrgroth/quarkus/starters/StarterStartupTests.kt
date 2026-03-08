package de.chrgroth.quarkus.starters

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

class StarterStartupTests {

    private val starterService: StarterService = mockk()
    private val completionFlag: StarterCompletionFlag = mockk()

    private val startup = StarterStartup(starterService, completionFlag)

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

        verify(exactly = 0) { starterService.runAll() }
        verify { completionFlag.markCompleted() }
    }

    @Test
    fun `onStart skips starters and marks completion immediately in TEST mode`() {
        every { LaunchMode.current() } returns LaunchMode.TEST

        startup.onStart(StartupEvent())

        verify(exactly = 0) { starterService.runAll() }
        verify { completionFlag.markCompleted() }
    }

    @Test
    fun `onStart runs starters and marks completion when all succeed in NORMAL mode`() {
        every { LaunchMode.current() } returns LaunchMode.NORMAL
        every { starterService.runAll() } returns true

        startup.onStart(StartupEvent())

        verify { starterService.runAll() }
        verify { completionFlag.markCompleted() }
    }

    @Test
    fun `onStart runs starters but does not mark completion when some fail in NORMAL mode`() {
        every { LaunchMode.current() } returns LaunchMode.NORMAL
        every { starterService.runAll() } returns false

        startup.onStart(StartupEvent())

        verify { starterService.runAll() }
        verify(exactly = 0) { completionFlag.markCompleted() }
    }
}
