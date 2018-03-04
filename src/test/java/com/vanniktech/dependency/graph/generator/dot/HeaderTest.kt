package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.dot.LabelJust.LEFT
import com.vanniktech.dependency.graph.generator.dot.LabelLoc.BOTTOM
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class HeaderTest {
  @Test fun syntax() {
    assertThat(Header("1234", 100, 5, BOTTOM, LEFT)).hasToString("label=\"1234\" fontsize=\"100\" height=\"5\" labelloc=\"b\" labeljust=\"l\"")
  }
}
