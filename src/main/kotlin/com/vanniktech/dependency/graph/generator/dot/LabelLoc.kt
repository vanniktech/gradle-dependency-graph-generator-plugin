package com.vanniktech.dependency.graph.generator.dot

enum class LabelLoc(val string: String) {
  TOP("t"),
  BOTTOM("b");

  override fun toString() = string
}
