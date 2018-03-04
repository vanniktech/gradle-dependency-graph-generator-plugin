package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.dot.LabelLoc.BOTTOM
import com.vanniktech.dependency.graph.generator.dot.LabelLoc.TOP
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class LabelLocTest {
  @Test fun values() {
    assertThat(LabelLoc.values()).containsExactly(TOP, BOTTOM)
  }

  @Test fun string() {
    assertThat(TOP).hasToString("t")
    assertThat(BOTTOM).hasToString("b")
  }
}
