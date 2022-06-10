package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.ProjectTarget.MULTIPLATFORM
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

private val whitespaceRegex = Regex("\\s")

internal val String.dotIdentifier get() = replace("-", "")
  .replace(".", "")
  .replace(whitespaceRegex, "")

internal fun String.nonEmptyPrepend(prepend: String) =
  if (isNotEmpty()) prepend + this else this

internal fun String.toHyphenCase(): String {
  if (isBlank()) return this

  return this[0].lowercase().toString() + toCharArray()
    .map { it.toString() }
    .drop(1)
    .joinToString(separator = "") { if (it[0].isUpperCase()) "-${it[0].lowercase()}" else it }
}

internal val Project.dotIdentifier get() = "$group$name".dotIdentifier

fun Project.isDependingOnOtherProject() = configurations.any { configuration -> configuration.dependencies.any { it is ProjectDependency } }

fun Project.isCommonsProject() = plugins.hasPlugin("org.jetbrains.kotlin.platform.common")

internal fun Project.target(): ProjectTarget {
  val targets = ProjectTarget.values()
    .filter { target -> target.ids.any { plugins.hasPlugin(it) } }

  val withoutMultiplatform = targets.minus(MULTIPLATFORM)

  return when {
    targets.contains(MULTIPLATFORM) -> MULTIPLATFORM
    else -> withoutMultiplatform.firstOrNull() ?: ProjectTarget.OTHER
  }
}
