package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.asDotSyntax

class GraphFormattingOptions @JvmOverloads constructor(
  private val shape: Shape = Shape.BOX,
  private val style: Style? = null,
  private val color: Color? = null
) {
  private var label: String? = null

  override fun toString() = "[" + mapOf(
      "label" to label,
      "shape" to shape,
      "style" to style,
      "color" to color
  ).asDotSyntax() + "]"

  /** Used internally for the label. */
  internal fun withLabel(label: String) = apply {
    this.label = label
  }
}
