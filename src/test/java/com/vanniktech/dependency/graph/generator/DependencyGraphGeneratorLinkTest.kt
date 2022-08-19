package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyContainer.Project
import com.vanniktech.dependency.graph.generator.DependencyContainer.ResolvedDependency
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import guru.nidi.graphviz.attribute.Color.BLUE
import guru.nidi.graphviz.attribute.Style.DASHED
import guru.nidi.graphviz.model.Link
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DependencyGraphGeneratorLinkTest {

  private lateinit var project: DefaultProject

  @Before
  fun setUp() {
    project = ProjectBuilder.builder().withName("root").build() as DefaultProject
    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.repositories.run { add(mavenCentral()) }
    project.dependencies.add("api", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")
    project.dependencies.add("implementation", "io.reactivex.rxjava2:rxjava:2.1.10")
  }

  @Test
  fun customizeLinkBasedOnFromAndTo() {
    val blueDashedLinkForRootToRxJava = { link: Link, from: DependencyContainer, to: DependencyContainer ->
      if ((from as? Project)?.project == project && (to as? ResolvedDependency)?.moduleName == "rxjava") link.attrs()
        .add(BLUE, DASHED)
      else link
    }
    assertEquals(
      // language=DOT
      """
      digraph "G" {
      edge ["dir"="forward"]
      graph ["dpi"="100"]
      "root" ["label"="root","shape"="rectangle"]
      "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
      "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
      "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
      "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
      {
      edge ["dir"="none"]
      graph ["rank"="same"]
      "root"
      }
      "root" -> "orgjetbrainskotlinkotlinstdlib"
      "root" -> "ioreactivexrxjava2rxjava" ["color"="blue","style"="dashed"]
      "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
      "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
      }
      """.trimIndent(),
      DependencyGraphGenerator(project, ALL.copy(link = blueDashedLinkForRootToRxJava)).generateGraph().toString()
    )
  }
}
