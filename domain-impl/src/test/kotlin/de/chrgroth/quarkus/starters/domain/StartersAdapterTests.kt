package de.chrgroth.quarkus.starters.domain

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import jakarta.enterprise.inject.Instance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.stream.Stream
import de.chrgroth.quarkus.starters.domain.port.`in`.ExecutionStatus
import de.chrgroth.quarkus.starters.domain.port.out.RepositoryPort

class StartersAdapterTests {

  private val repository: RepositoryPort = mockk()
  private val meterRegistry = SimpleMeterRegistry()
  private val starters: Instance<Starter> = mockk()

  private val service = ExecutionAdapter(starters, repository, meterRegistry)

  @BeforeEach
  fun setup() {
    every { starters.stream() } returns Stream.empty()
  }

  @Test
  fun `runAll returns true when there are no starters`() {
    val result = service.runAll()

    assertThat(result).isTrue()
  }

  @Test
  fun `runAll skips starter that already succeeded`() {
    val starter = mockk<Starter> { every { id } returns "starter-1" }
    every { starters.stream() } returns Stream.of(starter)
    every { repository.lastStatus("starter-1") } returns ExecutionStatus.SUCCEEDED

    val result = service.runAll()

    assertThat(result).isTrue()
    verify(exactly = 0) { starter.execute() }
  }

  @Test
  fun `runAll executes and persists success for a pending starter`() {
    val starter = mockk<Starter> {
      every { id } returns "starter-1"
      justRun { execute() }
    }
    every { starters.stream() } returns Stream.of(starter)
    every { repository.lastStatus("starter-1") } returns null
    justRun { repository.recordExecution(any(), any(), any<Instant>(), any<Instant>(), isNull()) }

    val result = service.runAll()

    assertThat(result).isTrue()
    verify { starter.execute() }
    verify { repository.recordExecution(eq("starter-1"), eq(ExecutionStatus.SUCCEEDED), any(), any(), isNull()) }
  }

  @Test
  fun `runAll records failure and returns false when starter throws`() {
    val starter = mockk<Starter> {
      every { id } returns "starter-fail"
      every { execute() } throws RuntimeException("boom")
    }
    every { starters.stream() } returns Stream.of(starter)
    every { repository.lastStatus("starter-fail") } returns null
    justRun { repository.recordExecution(any(), any(), any<Instant>(), any<Instant>(), any()) }

    val result = service.runAll()

    assertThat(result).isFalse()
    verify { repository.recordExecution(eq("starter-fail"), eq(ExecutionStatus.FAILED), any(), any(), eq("boom")) }
  }

  @Test
  fun `runAll processes starters in id order`() {
    val order = mutableListOf<String>()
    val starterB = mockk<Starter> {
      every { id } returns "b-starter"
      every { execute() } answers { order.add("b-starter"); Unit }
    }
    val starterA = mockk<Starter> {
      every { id } returns "a-starter"
      every { execute() } answers { order.add("a-starter"); Unit }
    }
    every { starters.stream() } returns Stream.of(starterB, starterA)
    every { repository.lastStatus(any()) } returns null
    justRun { repository.recordExecution(any(), any(), any<Instant>(), any<Instant>(), any()) }

    service.runAll()

    assertThat(order).containsExactly("a-starter", "b-starter")
  }

  @Test
  fun `runAll records timer metric on success`() {
    val starter = mockk<Starter> {
      every { id } returns "starter-1"
      justRun { execute() }
    }
    every { starters.stream() } returns Stream.of(starter)
    every { repository.lastStatus("starter-1") } returns null
    justRun { repository.recordExecution(any(), any(), any<Instant>(), any<Instant>(), isNull()) }

    service.runAll()

    val timer = meterRegistry.find("starter_execution_duration_seconds")
      .tag("id", "starter-1")
      .tag("status", ExecutionStatus.SUCCEEDED.name)
      .timer()
    assertThat(timer).isNotNull()
    assertThat(timer!!.count()).isEqualTo(1)
  }

  @Test
  fun `runAll records timer metric on failure`() {
    val starter = mockk<Starter> {
      every { id } returns "starter-fail"
      every { execute() } throws RuntimeException("oops")
    }
    every { starters.stream() } returns Stream.of(starter)
    every { repository.lastStatus("starter-fail") } returns null
    justRun { repository.recordExecution(any(), any(), any<Instant>(), any<Instant>(), any()) }

    service.runAll()

    val timer = meterRegistry.find("starter_execution_duration_seconds")
      .tag("id", "starter-fail")
      .tag("status", ExecutionStatus.FAILED.name)
      .timer()
    assertThat(timer).isNotNull()
    assertThat(timer!!.count()).isEqualTo(1)
  }

  @Test
  fun `runAll records gauge as success for already-succeeded starter`() {
    val starter = mockk<Starter> { every { id } returns "starter-1" }
    every { starters.stream() } returns Stream.of(starter)
    every { repository.lastStatus("starter-1") } returns ExecutionStatus.SUCCEEDED

    service.runAll()

    val gauge = meterRegistry.find("starter_overall_status").tag("id", "starter-1").gauge()
    assertThat(gauge).isNotNull()
    assertThat(gauge!!.value()).isEqualTo(1.0)
  }

  @Test
  fun `runAll records gauge as success for successful starter`() {
    val starter = mockk<Starter> {
      every { id } returns "starter-1"
      justRun { execute() }
    }
    every { starters.stream() } returns Stream.of(starter)
    every { repository.lastStatus("starter-1") } returns null
    justRun { repository.recordExecution(any(), any(), any<Instant>(), any<Instant>(), isNull()) }

    service.runAll()

    val gauge = meterRegistry.find("starter_overall_status").tag("id", "starter-1").gauge()
    assertThat(gauge).isNotNull()
    assertThat(gauge!!.value()).isEqualTo(1.0)
  }

  @Test
  fun `runAll records gauge as failure for failed starter`() {
    val starter = mockk<Starter> {
      every { id } returns "starter-fail"
      every { execute() } throws RuntimeException("oops")
    }
    every { starters.stream() } returns Stream.of(starter)
    every { repository.lastStatus("starter-fail") } returns null
    justRun { repository.recordExecution(any(), any(), any<Instant>(), any<Instant>(), any()) }

    service.runAll()

    val gauge = meterRegistry.find("starter_overall_status").tag("id", "starter-fail").gauge()
    assertThat(gauge).isNotNull()
    assertThat(gauge!!.value()).isEqualTo(0.0)
  }
}
