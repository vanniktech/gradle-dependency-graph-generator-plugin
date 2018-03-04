package com.vanniktech.dependency.graph.generator

@Suppress("Detekt.TopLevelPropertyNaming") // https://github.com/arturbosch/detekt/issues/769
private val WHITESPACE_REGEX = Regex("\\s")

internal val String.dotIdentifier get() = replace("-", "")
    .replace(".", "")
    .replace(WHITESPACE_REGEX, "")

internal fun String.nonEmptyPrepend(prepend: String) =
    if (isNotEmpty()) prepend + this else this

internal fun String.toHyphenCase(): String {
  if (isBlank()) return this

  return this[0].toLowerCase().toString() + toCharArray()
      .map { it.toString() }
      .drop(1)
      .joinToString(separator = "") { if (it[0].isUpperCase()) "-${it[0].toLowerCase()}" else it }
}

internal fun Map<String, Any?>.asDotSyntax(separator: String = ", ") = filter { (_, value) -> value != null }
    .map { (key, value) -> "$key=\"$value\"" }
    .joinToString(separator)
