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

class GraphFormattingOptionsTest {
  @Test fun shape() {
    assertThat(GraphFormattingOptions()).hasToString("""[shape="box"]""")
  }

  @Test fun style() {
    assertThat(GraphFormattingOptions(style = SOLID)).hasToString("""[shape="box", style="solid"]""")
    assertThat(GraphFormattingOptions(style = DASHED)).hasToString("""[shape="box", style="dashed"]""")
    assertThat(GraphFormattingOptions(style = FILLED)).hasToString("""[shape="box", style="filled"]""")
    assertThat(GraphFormattingOptions(style = DIAGONALS)).hasToString("""[shape="box", style="diagonals"]""")
    assertThat(GraphFormattingOptions(style = ROUNDED)).hasToString("""[shape="box", style="rounded"]""")
    assertThat(GraphFormattingOptions(style = DOTTED)).hasToString("""[shape="box", style="dotted"]""")
    assertThat(GraphFormattingOptions(style = BOLD)).hasToString("""[shape="box", style="bold"]""")
    assertThat(GraphFormattingOptions(style = INVIS)).hasToString("""[shape="box", style="invis"]""")
  }

  @Test fun color() {
    assertThat(GraphFormattingOptions(color = Color.fromHex("#ff00AA"))).hasToString("""[shape="box", color="#ff00AA"]""")
    assertThat(GraphFormattingOptions(color = Color.fromRgb(255, 128, 0))).hasToString("""[shape="box", color="#ff8000"]""")
  }

  @Test fun withLabel() {
    assertThat(GraphFormattingOptions().withLabel("label")).hasToString("""[label="label", shape="box"]""")
  }
}
