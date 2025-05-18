package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import guru.nidi.graphviz.attribute.Shape
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DependencyGraphGeneratorMapperTest {
  private lateinit var project: Project

  @Before
  fun setUp() {
    project = buildTestProject(name = "root")
    with(project) {
      plugins.apply(JavaLibraryPlugin::class.java)
      repositories.run { add(mavenCentral()) }
      dependencies.add("api", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")
      dependencies.add("api", "org.jetbrains.kotlin:kotlin-reflect:1.2.30")
      dependencies.add("implementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    }
  }

  @Test
  fun tintedByGroupDependencyMapperTest() {
    assertEquals(
      // language=dot
      """
      digraph "G" {
      edge ["dir"="forward"]
      graph ["dpi"="100"]
      "root" ["label"="root","shape"="rectangle"]
      "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle","fillcolor"="#9b5ba3"]
      "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle","fillcolor"="#504d8c"]
      "orgjetbrainskotlinkotlinreflect" ["label"="kotlin-reflect","shape"="rectangle","fillcolor"="#9b5ba3"]
      "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle","fillcolor"="#0afeb3"]
      "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle","fillcolor"="#ea70d0"]
      {
      edge ["dir"="none"]
      graph ["rank"="same"]
      "root"
      }
      "root" -> "orgjetbrainskotlinkotlinstdlib"
      "root" -> "orgjetbrainskotlinkotlinreflect"
      "root" -> "ioreactivexrxjava2rxjava"
      "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
      "orgjetbrainskotlinkotlinreflect" -> "orgjetbrainskotlinkotlinstdlib"
      "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
      }
      """.trimIndent(),
      DependencyGraphGenerator(
        project = project,
        generator = ALL.copy(dependencyMapper = TintDependencyByGroup),
      ).generateGraph().toString(),
    )
  }

  @Test
  fun customDependencyMapperTest() {
    val mapper: DependencyMapper = { dependency ->
      println("${dependency.moduleGroup} ${dependency.name} ${dependency.moduleName}")
      when {
        dependency.moduleName.contains("x") -> BasicNodeType("#ABC123", Shape.EGG)
        dependency.moduleName.contains("k") -> BasicNodeType("#DEF789", Shape.POINT)
//        !dependency.moduleGroup.contains("kotlin") -> BasicNodeType("#000000", Shape.OCTAGON)
        else -> null
      }
    }
    assertEquals(
      // language=dot
      """
      digraph "G" {
      edge ["dir"="forward"]
      graph ["dpi"="100"]
      "root" ["label"="root","shape"="rectangle"]
      "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="point","fillcolor"="#DEF789"]
      "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
      "orgjetbrainskotlinkotlinreflect" ["label"="kotlin-reflect","shape"="point","fillcolor"="#DEF789"]
      "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="egg","fillcolor"="#ABC123"]
      "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
      {
      edge ["dir"="none"]
      graph ["rank"="same"]
      "root"
      }
      "root" -> "orgjetbrainskotlinkotlinstdlib"
      "root" -> "orgjetbrainskotlinkotlinreflect"
      "root" -> "ioreactivexrxjava2rxjava"
      "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
      "orgjetbrainskotlinkotlinreflect" -> "orgjetbrainskotlinkotlinstdlib"
      "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
      }
      """.trimIndent(),
      DependencyGraphGenerator(project, ALL.copy(dependencyMapper = mapper)).generateGraph().toString(),
    )
  }
}
