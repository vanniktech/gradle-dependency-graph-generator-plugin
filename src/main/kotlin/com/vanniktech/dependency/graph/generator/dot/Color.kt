package com.vanniktech.dependency.graph.generator.dot

data class Color(private val value: String) {
  override fun toString() = value

  companion object {
    private val HEX_PATTERN = Regex("#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})")

    internal const val MAX_COLOR_VALUE = 255

    @JvmStatic fun fromHex(hex: String): Color {
      require(hex.matches(HEX_PATTERN)) { "Color is not a valid hex color." }
      return Color(hex)
    }

    @JvmStatic fun fromRgb(r: Int, g: Int, b: Int): Color {
      require(r in 0..MAX_COLOR_VALUE) { "r should be in [0..256)" }
      require(g in 0..MAX_COLOR_VALUE) { "g should be in [0..256)" }
      require(b in 0..MAX_COLOR_VALUE) { "b should be in [0..256)" }
      return Color(String.format("#%02x%02x%02x", r, g, b))
    }
  }
}
