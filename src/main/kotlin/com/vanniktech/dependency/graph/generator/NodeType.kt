package com.vanniktech.dependency.graph.generator

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Shape
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency

interface NodeType {
  val color: Color?
  val shape: Shape?
}

data class BasicNodeType(
  override val color: Color?,
  override val shape: Shape?,
) : NodeType {
  constructor(rgbHex: String, shape: Shape?) : this(Color.rgb(rgbHex).fill(), shape)
  constructor(rgbInt: Int, shape: Shape?) : this(Color.rgb(rgbInt).fill(), shape)
}

typealias ProjectMapper = (Project) -> NodeType?

typealias DependencyMapper = (ResolvedDependency) -> NodeType?

/**
 * Use this to apply a semi-random fill color onto each node, where the same color is used on every dependency of the
 * same group. E.g. "a.b:c" will have the same color as "a.b:d", but different to "e.f:c"
 */
val TintDependencyByGroup: DependencyMapper = { dependency ->
  BasicNodeType(dependency.moduleGroup.hashCode(), shape = null)
}

internal enum class DefaultProjectType(
  override val color: Color,
  override val shape: Shape? = null,
) : NodeType {
  ANDROID(color = Color.rgb("#66BB6A").fill()), // green
  JVM(color = Color.rgb("#FF7043").fill()), // orange
  IOS(color = Color.rgb("#42A5F5").fill()), // blue
  JS(color = Color.rgb("#FFCA28").fill()), // yellow
  MULTIPLATFORM(color = Color.rgb("#A280FF").fill()), // lilac
  OTHER(color = Color.rgb("#BDBDBD").fill()), // grey
  ;
}

val DefaultProjectMapper: ProjectMapper = { project ->
  when {
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
}
