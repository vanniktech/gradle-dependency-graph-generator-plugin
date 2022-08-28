package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency

@JvmInline
internal value class DotIdentifier(internal val value: String) {
  init {
    require(value.none(illegalChars))
  }
}

private val illegalChars: (Char) -> Boolean = { it == '-' || it == '.' || it.isWhitespace() }

internal val String.dotIdentifier get() = DotIdentifier(filterNot(illegalChars))

internal val Project.dotIdentifier get() = "$group$name".dotIdentifier

internal val ResolvedDependency.dotIdentifier get() = (moduleGroup + moduleName).dotIdentifier

internal val DependencyContainer.dotIdentifier get() = when (this) {
  is DependencyContainer.Project -> project.dotIdentifier
  is DependencyContainer.ResolvedDependency -> resolvedDependency.dotIdentifier
}
