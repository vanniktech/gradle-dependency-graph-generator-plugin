package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.dot.LabelJust.CENTER
import com.vanniktech.dependency.graph.generator.dot.LabelJust.LEFT
import com.vanniktech.dependency.graph.generator.dot.LabelJust.RIGHT
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class LabelJustTest {
  @Test fun values() {
    assertThat(LabelJust.values()).containsExactly(LEFT, CENTER, RIGHT)
  }

  @Test fun string() {
    assertThat(LEFT).hasToString("l")
    assertThat(CENTER).hasToString("c")
    assertThat(RIGHT).hasToString("r")
  }
}
