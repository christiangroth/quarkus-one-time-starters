package de.chrgroth.quarkus.starters.adapter.out.mongodb

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MetricsRecorderTests {

  private val meterRegistry = SimpleMeterRegistry()

  @Test
  fun `timed records duration under operation tag and returns result`() {
    val recorder = MetricsRecorder(meterRegistry, 10_000)

    val result = recorder.timed("starters.findById") { "value" }

    assertThat(result).isEqualTo("value")
    val timer = meterRegistry.find(MetricsRecorder.QUERY_TIMER).tag(MetricsRecorder.OPERATION_TAG, "starters.findById").timer()
    assertThat(timer).isNotNull
    assertThat(timer!!.count()).isEqualTo(1)
    assertThat(meterRegistry.find(MetricsRecorder.SLOW_QUERY_COUNTER).counter()).isNull()
  }

  @Test
  fun `timed records duration when block throws`() {
    val recorder = MetricsRecorder(meterRegistry, 10_000)

    assertThatThrownBy { recorder.timed<Unit>("starters.save") { error("boom") } }
      .isInstanceOf(IllegalStateException::class.java)

    val timer = meterRegistry.find(MetricsRecorder.QUERY_TIMER).tag(MetricsRecorder.OPERATION_TAG, "starters.save").timer()
    assertThat(timer!!.count()).isEqualTo(1)
  }

  @Test
  fun `timed counts slow queries above threshold`() {
    val recorder = MetricsRecorder(meterRegistry, 0)

    recorder.timed("starters.indexes.sync") { }
    recorder.timed("starters.indexes.sync") { }

    val counter = meterRegistry.find(MetricsRecorder.SLOW_QUERY_COUNTER).tag(MetricsRecorder.OPERATION_TAG, "starters.indexes.sync").counter()
    assertThat(counter!!.count()).isEqualTo(2.0)
  }
}
