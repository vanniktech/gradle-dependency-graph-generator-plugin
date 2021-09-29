package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import com.vanniktech.dependency.graph.generator.ProjectDependencyGraphGenerator.Connection

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

internal val Project.dotIdentifier get() = "$group$name".dotIdentifier

fun Project.isDependingOnOtherProject() = configurations.any { configuration -> configuration.dependencies.any { it is ProjectDependency } }

fun Project.isJavaProject() = listOf("java-library", "java", "java-gradle-plugin").any { plugins.hasPlugin(it) }

fun Project.isKotlinProject() = listOf("kotlin", "kotlin-android", "kotlin-platform-jvm").any { plugins.hasPlugin(it) }

fun Project.isAndroidProject() = listOf("com.android.library", "com.android.application", "com.android.test", "com.android.feature", "com.android.instantapp").any { plugins.hasPlugin(it) }

fun Project.isJsProject() = plugins.hasPlugin("kotlin2js")

fun Project.isCommonsProject() = plugins.hasPlugin("org.jetbrains.kotlin.platform.common")

internal fun Project.getProjectDependencies(
  projectGenerator: DependencyGraphGeneratorExtension.ProjectGenerator
): Pair<Set<Project>, List<Connection>> {
  val projects = mutableSetOf<Project>()
  val dependencies = mutableListOf<Connection>()
  fun addProject(project: Project) {
    if (projectGenerator.includeProject(project) && projects.add(project)) {
      project.configurations
        .flatMap { configuration ->
          configuration.dependencies
            .withType(ProjectDependency::class.java)
            .map { Connection(project, it.dependencyProject, configuration.name.endsWith("implementation", true)) }
        }
        .forEach {
          dependencies.add(it)
          addProject(it.to)
        }
    }
  }
  project.allprojects.filter { it.isDependingOnOtherProject() }.forEach { addProject(it) }
  return projects to dependencies
}
