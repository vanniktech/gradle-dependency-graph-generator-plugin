package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency

@JvmInline
internal value class DotIdentifier(internal val value: String)

internal val String.dotIdentifier get() = filterNot { it == '-' || it == '.' || it.isWhitespace() }

internal val Project.dotIdentifier get() = DotIdentifier("$group$name".dotIdentifier)

internal val ResolvedDependency.dotIdentifier get() = DotIdentifier((moduleGroup + moduleName).dotIdentifier)

internal val DependencyContainer.dotIdentifier get() = when (this) {
  is DependencyContainer.Project -> project.dotIdentifier
  is DependencyContainer.ResolvedDependency -> resolvedDependency.dotIdentifier
}
