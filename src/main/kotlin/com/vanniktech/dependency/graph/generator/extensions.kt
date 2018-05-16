package com.vanniktech.dependency.graph.generator

private val whitespaceRegex = Regex("\\s")

internal val String.dotIdentifier get() = replace("-", "")
    .replace(".", "")
    .replace(whitespaceRegex, "")

internal fun String.nonEmptyPrepend(prepend: String) =
    if (isNotEmpty()) prepend + this else this

internal fun String.toHyphenCase(): String {
  if (isBlank()) return this

  return this[0].toLowerCase().toString() + toCharArray()
      .map { it.toString() }
      .drop(1)
      .joinToString(separator = "") { if (it[0].isUpperCase()) "-${it[0].toLowerCase()}" else it }
}
