package com.vanniktech.dependency.graph.generator

import guru.nidi.graphviz.attribute.Color

internal enum class ProjectTarget(
  val ids: Set<String>,
  val color: Color,
) {
  ANDROID(
    ids = setOf("com.android.library", "com.android.application", "com.android.test", "com.android.feature", "com.android.instantapp"),
    color = Color.rgb("#66BB6A").fill(),
  ),
  JVM(
    ids = setOf("java-library", "java", "java-gradle-plugin", "application", "org.jetbrains.kotlin.jvm"),
    color = Color.rgb("#FF7043").fill(),
  ),
  IOS(
    ids = setOf("org.jetbrains.kotlin.native.cocoapods"),
    color = Color.rgb("#42A5F5").fill(),
  ),
  JS(
    ids = setOf("com.eriwen.gradle.js"),
    color = Color.rgb("#FFCA28").fill(),
  ),
  MULTIPLATFORM(
    ids = setOf("org.jetbrains.kotlin.multiplatform"),
    color = Color.rgb("#AB47BC").fill(),
  ),
  OTHER(
    ids = emptySet(),
    color = Color.rgb("#BDBDBD").fill(),
  ),
  ;
}
