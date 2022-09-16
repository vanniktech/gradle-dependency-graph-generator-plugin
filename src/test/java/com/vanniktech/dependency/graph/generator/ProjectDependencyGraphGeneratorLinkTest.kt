package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator.Companion.ALL
import guru.nidi.graphviz.attribute.Color.BLUE
import guru.nidi.graphviz.attribute.Style.DASHED
import guru.nidi.graphviz.model.Link
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ProjectDependencyGraphGeneratorLinkTest {

  private lateinit var root: Project
  private lateinit var app: Project
  private lateinit var lib1: Project
  private lateinit var lib2: Project

  @Before fun setUp() {
    root = ProjectBuilder.builder().withName("root").build()
    root.plugins.apply(JavaLibraryPlugin::class.java)

    app = ProjectBuilder.builder().withParent(root).withName("app").build()
    app.plugins.apply(JavaLibraryPlugin::class.java)

    lib1 = ProjectBuilder.builder().withParent(root).withName("lib1").build()
    lib1.plugins.apply(JavaLibraryPlugin::class.java)

    lib2 = ProjectBuilder.builder().withParent(root).withName("lib2").build()
    lib2.plugins.apply(JavaLibraryPlugin::class.java)

    app.dependencies.add("implementation", lib1)
    app.dependencies.add("implementation", lib2)
  }

  @Test fun customizeLinkBasedOnFromAndTo() {
    val blueDashedLinkForAppToLib1 = { link: Link, from: Project, to: Project, _: Configuration ->
      if (from == app && to == lib1) {
        link.attrs().add(BLUE, DASHED)
      } else {
        link
      }
    }
    assertEquals(
      // language=DOT
      """
      digraph {
      edge ["dir"="forward"]
      graph ["dpi"="100","label"="root","labelloc"="t","fontsize"="35"]
      node ["style"="filled"]
      ":app" ["shape"="rectangle","fillcolor"="#FF7043"]
      ":lib1" ["fillcolor"="#FF7043"]
      ":lib2" ["fillcolor"="#FF7043"]
      {
      edge ["dir"="none"]
      graph ["rank"="same"]
      ":app"
      }
      ":app" -> ":lib1" ["color"="blue","style"="dashed"]
      ":app" -> ":lib2"
      }
      """.trimIndent(),
      ProjectDependencyGraphGenerator(root, ALL.copy(link = blueDashedLinkForAppToLib1)).generateGraph().toString(),
    )
  }
}
