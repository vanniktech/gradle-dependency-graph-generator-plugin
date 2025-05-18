package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import java.io.File

internal fun String.nonEmptyPrepend(prepend: String) =
  if (isNotEmpty()) prepend + this else this

internal fun String.toHyphenCase(): String {
  if (isBlank()) return this

  return this[0].lowercase().toString() + toCharArray()
    .map { it.toString() }
    .drop(1)
    .joinToString(separator = "") { if (it[0].isUpperCase()) "-${it[0].lowercase()}" else it }
}

fun Project.isDependingOnOtherProject() = configurations.any { configuration -> configuration.dependencies.any { it is ProjectDependency } }

internal fun Configuration.isImplementation() = name.lowercase().endsWith("implementation")

internal val Project.buildDirectory: File
  get() = layout.buildDirectory.asFile.get()

internal fun Project.hasAnyPlugin(vararg ids: String) = ids.any { pluginManager.hasPlugin(it) }
