package com.vanniktech.dependency.graph.generator

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Shape
import org.gradle.api.Project

interface ProjectType {
  val color: Color?
  val shape: Shape?
}

typealias ProjectMapper = (Project) -> ProjectType?

internal enum class DefaultProjectType(
  override val color: Color,
  override val shape: Shape? = null,
) : ProjectType {
  ANDROID(color = Color.rgb("#66BB6A").fill()), // green
  JVM(color = Color.rgb("#FF7043").fill()), // orange
  IOS(color = Color.rgb("#42A5F5").fill()), // blue
  JS(color = Color.rgb("#FFCA28").fill()), // yellow
  MULTIPLATFORM(color = Color.rgb("#A280FF").fill()), // lilac
  OTHER(color = Color.rgb("#BDBDBD").fill()), // grey
  ;
}

internal fun defaultProjectMapper(project: Project): ProjectType = when {
  project.hasAnyPlugin("org.jetbrains.kotlin.multiplatform") ->
    DefaultProjectType.MULTIPLATFORM

  project.hasAnyPlugin(
    "com.android.library",
    "com.android.application",
    "com.android.test",
    "com.android.feature",
    "com.android.instantapp",
  ) -> DefaultProjectType.ANDROID

  project.hasAnyPlugin(
    "java-library",
    "java",
    "java-gradle-plugin",
    "application",
    "org.jetbrains.kotlin.jvm",
  ) -> DefaultProjectType.JVM

  project.hasAnyPlugin("org.jetbrains.kotlin.native.cocoapods") ->
    DefaultProjectType.IOS

  project.hasAnyPlugin("com.eriwen.gradle.js") ->
    DefaultProjectType.JS

  else ->
    DefaultProjectType.OTHER
}
