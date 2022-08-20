package com.vanniktech.dependency.graph.generator

import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class DotIdentifierTest {

  @Test
  fun valid() {
    assertEquals("javainject", "java.inject".dotIdentifier.value)
    assertEquals("firebasecore", "firebase-core".dotIdentifier.value)
    assertEquals("firebasecore", "firebase core".dotIdentifier.value)
  }

  @Test
  fun validProject() {
    val project = ProjectBuilder.builder().withName("app").build()
    assertEquals("app", project.dotIdentifier.value)
  }

  @Test
  fun validResolvedDependency() {
    val project = ProjectBuilder.builder().withName("app").build()
    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.repositories.run { add(mavenCentral()) }
    project.dependencies.add("compileOnly", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")
    val dependency = project.configurations
      .filter { it.isCanBeResolved }
      .flatMap { it.resolvedConfiguration.firstLevelModuleDependencies }
      .single()
    assertEquals("orgjetbrainskotlinkotlinstdlib", dependency.dotIdentifier.value)
  }

  @Test(expected = IllegalArgumentException::class)
  fun dash() {
    DotIdentifier("a-b")
  }

  @Test(expected = IllegalArgumentException::class)
  fun dot() {
    DotIdentifier("a.b")
  }

  @Test(expected = IllegalArgumentException::class)
  fun whitespace() {
    DotIdentifier("a b")
  }
}
