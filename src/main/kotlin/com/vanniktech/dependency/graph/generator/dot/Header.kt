package com.vanniktech.dependency.graph.generator.dot

import com.vanniktech.dependency.graph.generator.asDotSyntax
import com.vanniktech.dependency.graph.generator.dot.LabelJust.CENTER
import com.vanniktech.dependency.graph.generator.dot.LabelLoc.TOP

data class Header @JvmOverloads constructor(
  val text: String,
  val fontsize: Int = 24,
  val height: Int = 5,
  val labelLoc: LabelLoc = TOP,
  val labelJust: LabelJust = CENTER
) {
  override fun toString() = mapOf(
      "label" to text,
      "fontsize" to fontsize,
      "height" to height,
      "labelloc" to labelLoc,
      "labeljust" to labelJust
  ).asDotSyntax(separator = " ")
}
