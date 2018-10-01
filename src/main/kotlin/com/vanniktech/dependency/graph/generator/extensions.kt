package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project

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

fun Project.isJavaProject() = listOf("java-library", "java", "java-gradle-plugin").any { plugins.hasPlugin(it) }

fun Project.isKotlinProject() = listOf("kotlin", "kotlin-android", "kotlin-platform-jvm").any { plugins.hasPlugin(it) }

fun Project.isAndroidProject() = listOf("com.android.library", "com.android.application", "com.android.test", "com.android.feature", "com.android.instantapp").any { plugins.hasPlugin(it) }

fun Project.isJsProject() = plugins.hasPlugin("kotlin2js")

fun Project.isCommonsProject() = plugins.hasPlugin("org.jetbrains.kotlin.platform.common")
