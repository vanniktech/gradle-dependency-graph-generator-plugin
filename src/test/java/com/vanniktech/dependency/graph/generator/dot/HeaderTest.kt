package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.dot.LabelJust.LEFT
import com.vanniktech.dependency.graph.generator.dot.LabelLoc.BOTTOM
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class HeaderTest {
  @Test fun syntax() {
    assertThat(Header("foo")).hasToString("label=\"foo\" fontsize=\"24\" height=\"5\" labelloc=\"t\" labeljust=\"c\"")
    assertThat(Header("foo", 10)).hasToString("label=\"foo\" fontsize=\"10\" height=\"5\" labelloc=\"t\" labeljust=\"c\"")
    assertThat(Header("foo", 10, 25)).hasToString("label=\"foo\" fontsize=\"10\" height=\"25\" labelloc=\"t\" labeljust=\"c\"")
    assertThat(Header("foo", 10, 25, BOTTOM)).hasToString("label=\"foo\" fontsize=\"10\" height=\"25\" labelloc=\"b\" labeljust=\"c\"")
    assertThat(Header("1234", 100, 5, BOTTOM, LEFT)).hasToString("label=\"1234\" fontsize=\"100\" height=\"5\" labelloc=\"b\" labeljust=\"l\"")
  }
}
