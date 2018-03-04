package com.vanniktech.dependency.graph.generator.dot

enum class LabelJust(val string: String) {
  LEFT("l"),
  CENTER("c"),
  RIGHT("r");

  override fun toString() = string
}
