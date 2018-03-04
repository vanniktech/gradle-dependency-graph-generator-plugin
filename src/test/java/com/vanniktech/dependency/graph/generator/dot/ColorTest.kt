package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.assertThrows
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class ColorTest {
  @Test fun color() {
    assertThat(Color.fromHex("#ff0000")).hasToString("#ff0000")
    assertThat(Color.fromRgb(0, 128, 255)).hasToString("#0080ff")
    assertThat(Color.fromRgb(255, 203, 43)).hasToString("#ffcb2b")
  }

  @Test fun invalidHexColor() {
    assertThrows<IllegalArgumentException> { Color.fromHex("#") }.hasMessage("Color is not a valid hex color.")
    assertThrows<IllegalArgumentException> { Color.fromHex("#1") }.hasMessage("Color is not a valid hex color.")
    assertThrows<IllegalArgumentException> { Color.fromHex("#12") }.hasMessage("Color is not a valid hex color.")
    assertThrows<IllegalArgumentException> { Color.fromHex("#1234") }.hasMessage("Color is not a valid hex color.")
    assertThrows<IllegalArgumentException> { Color.fromHex("#12345") }.hasMessage("Color is not a valid hex color.")
    assertThrows<IllegalArgumentException> { Color.fromHex("#1234567") }.hasMessage("Color is not a valid hex color.")
  }

  @Test fun invalidRgbColor() {
    assertThrows<IllegalArgumentException> { Color.fromRgb(-1, 0, 0) }.hasMessage("r should be in [0..256)")
    assertThrows<IllegalArgumentException> { Color.fromRgb(256, 0, 0) }.hasMessage("r should be in [0..256)")
    assertThrows<IllegalArgumentException> { Color.fromRgb(0, -1, 0) }.hasMessage("g should be in [0..256)")
    assertThrows<IllegalArgumentException> { Color.fromRgb(0, 256, 0) }.hasMessage("g should be in [0..256)")
    assertThrows<IllegalArgumentException> { Color.fromRgb(0, 0, -1) }.hasMessage("b should be in [0..256)")
    assertThrows<IllegalArgumentException> { Color.fromRgb(0, 0, 256) }.hasMessage("b should be in [0..256)")
  }
}
