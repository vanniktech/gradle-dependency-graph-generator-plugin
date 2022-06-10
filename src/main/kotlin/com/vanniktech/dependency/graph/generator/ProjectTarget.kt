package com.vanniktech.dependency.graph.generator

internal enum class ProjectTarget(
  val ids: Set<String>,
) {
  ANDROID(
    ids = setOf("com.android.library", "com.android.application", "com.android.test", "com.android.feature", "com.android.instantapp"),
  ),
  JVM(
    ids = setOf("java-library", "java", "java-gradle-plugin", "application", "org.jetbrains.kotlin.jvm"),
  ),
  IOS(
    ids = setOf("org.jetbrains.kotlin.native.cocoapods"),
  ),
  JS(
    ids = setOf("com.eriwen.gradle.js"),
  ),
  MULTIPLATFORM(
    ids = setOf("org.jetbrains.kotlin.multiplatform"),
  ),
  OTHER(
    ids = emptySet(),
  ),
  ;
}
