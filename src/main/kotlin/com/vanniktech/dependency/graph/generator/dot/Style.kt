package com.vanniktech.dependency.graph.generator.dot

enum class Style(val string: String) {
  SOLID("solid"),
  DASHED("dashed"),
  FILLED("filled"),
  DIAGONALS("diagonals"),
  ROUNDED("rounded"),
  DOTTED("dotted"),
  BOLD("bold"),
  INVIS("invis");

  override fun toString() = string
}
