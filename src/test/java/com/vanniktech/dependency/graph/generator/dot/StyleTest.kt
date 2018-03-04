package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.dot.Style.BOLD
import com.vanniktech.dependency.graph.generator.dot.Style.DASHED
import com.vanniktech.dependency.graph.generator.dot.Style.DIAGONALS
import com.vanniktech.dependency.graph.generator.dot.Style.DOTTED
import com.vanniktech.dependency.graph.generator.dot.Style.FILLED
import com.vanniktech.dependency.graph.generator.dot.Style.INVIS
import com.vanniktech.dependency.graph.generator.dot.Style.ROUNDED
import com.vanniktech.dependency.graph.generator.dot.Style.SOLID
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class StyleTest {
  @Test fun values() {
    assertThat(Style.values()).containsExactly(SOLID, DASHED, FILLED, DIAGONALS, ROUNDED, DOTTED, BOLD, INVIS)
  }

  @Test fun string() {
    assertThat(SOLID).hasToString("solid")
    assertThat(DASHED).hasToString("dashed")
    assertThat(FILLED).hasToString("filled")
    assertThat(DIAGONALS).hasToString("diagonals")
    assertThat(ROUNDED).hasToString("rounded")
    assertThat(DOTTED).hasToString("dotted")
    assertThat(BOLD).hasToString("bold")
    assertThat(INVIS).hasToString("invis")
  }
}
