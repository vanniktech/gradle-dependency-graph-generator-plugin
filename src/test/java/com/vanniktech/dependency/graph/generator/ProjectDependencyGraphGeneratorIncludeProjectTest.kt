package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator.Companion.ALL
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Shape
import org.gradle.api.Project
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ProjectDependencyGraphGeneratorIncludeProjectTest {

  private lateinit var root: Project
  private lateinit var app: Project
  private lateinit var lib1: Project
  private lateinit var lib2: Project

  @Before
  fun setUp() {
    root = buildTestProject("root", parent = null, "java-library")
    app = buildTestProject("app", root, "java-library")
    lib1 = buildTestProject("lib1", root, "java-library")
    lib2 = buildTestProject("lib2", root, "java-library")

    app.dependencies.add("implementation", lib1)
    app.dependencies.add("implementation", lib2)
    lib1.dependencies.add("implementation", lib2)
  }

  @Test
  fun excludeNonLeafProject() {
    assertEquals(
      // language=dot
      """
      digraph {
      edge ["dir"="forward"]
      graph ["dpi"="100","label"="root","labelloc"="t","fontsize"="35"]
      node ["style"="filled"]
      ":app" ["shape"="rectangle","fillcolor"="#FF7043"]
      ":lib2" ["fillcolor"="#FF7043"]
      {
      edge ["dir"="none"]
      graph ["rank"="same"]
      ":app"
      }
      ":app" -> ":lib2" ["style"="dotted"]
      }
      """.trimIndent(),
      ProjectDependencyGraphGenerator(root, ALL.copy(includeProject = { it != lib1 })).generateGraph().toString(),
    )
  }

  @Test
  fun excludeLeafProject() {
    assertEquals(
      // language=dot
      """
      digraph {
      edge ["dir"="forward"]
      graph ["dpi"="100","label"="root","labelloc"="t","fontsize"="35"]
      node ["style"="filled"]
      ":app" ["shape"="rectangle","fillcolor"="#FF7043"]
      ":lib1" ["fillcolor"="#FF7043"]
      {
      edge ["dir"="none"]
      graph ["rank"="same"]
      ":app"
      }
      ":app" -> ":lib1" ["style"="dotted"]
      }
      """.trimIndent(),
      ProjectDependencyGraphGenerator(root, ALL.copy(includeProject = { it != lib2 })).generateGraph().toString(),
    )
  }

  @Test
  fun withDifferentProjectTypesUsingDefaultMapper() {
    val androidLib = buildTestProject("android", root, "com.android.library")
    val iosLib = buildTestProject("ios", root, "org.jetbrains.kotlin.native.cocoapods")
    val jvmLib = buildTestProject("jvm", root, "org.jetbrains.kotlin.jvm")
    val kmpLib = buildTestProject("kmp", root, "org.jetbrains.kotlin.multiplatform")
    val otherLib = buildTestProject("other", root, pluginId = null)

    app.dependencies.add("implementation", androidLib)
    app.dependencies.add("implementation", iosLib)
    app.dependencies.add("implementation", jvmLib)
    app.dependencies.add("implementation", kmpLib)
    app.dependencies.add("implementation", otherLib)

    assertEquals(
      // language=dot
      """
      digraph {
      edge ["dir"="forward"]
      graph ["dpi"="100","label"="root","labelloc"="t","fontsize"="35"]
      node ["style"="filled"]
      ":app" ["shape"="rectangle","fillcolor"="#FF7043"]
      ":lib1" ["fillcolor"="#FF7043"]
      ":lib2" ["fillcolor"="#FF7043"]
      ":android" ["fillcolor"="#66BB6A"]
      ":ios" ["fillcolor"="#42A5F5"]
      ":jvm" ["fillcolor"="#FF7043"]
      ":kmp" ["fillcolor"="#A280FF"]
      ":other" ["fillcolor"="#BDBDBD"]
      {
      edge ["dir"="none"]
      graph ["rank"="same"]
      ":app"
      }
      ":app" -> ":lib1" ["style"="dotted"]
      ":app" -> ":lib2" ["style"="dotted"]
      ":app" -> ":android" ["style"="dotted"]
      ":app" -> ":ios" ["style"="dotted"]
      ":app" -> ":jvm" ["style"="dotted"]
      ":app" -> ":kmp" ["style"="dotted"]
      ":app" -> ":other" ["style"="dotted"]
      ":lib1" -> ":lib2" ["style"="dotted"]
      }
      """.trimIndent(),
      ProjectDependencyGraphGenerator(root, ALL).generateGraph().toString(),
    )
  }

  private enum class TestProject(override val color: Color?, override val shape: Shape?) : NodeType {
    ABC(color = Color.rgb("#FFFFFF").fill(), shape = Shape.PARALLELOGRAM),
    DEF(color = Color.rgb("#ABCDEF").fill(), shape = Shape.THREE_P_OVERHANG),
    XYZ(color = null, shape = Shape.ASSEMBLY),
  }

  @Test
  fun withDifferentProjectTypesUsingCustomMapper() {
    val root = buildTestProject("root", parent = null, pluginId = null)
    val app = buildTestProject("app", root, "java-library")

    val abcLib = buildTestProject("abc", root, pluginId = null) {
      extensions.extraProperties.set("some-property", "abc")
    }

    val defLib = buildTestProject("def", root, pluginId = null)
    val xyzLib = buildTestProject("xyz", root, pluginId = null)

    app.dependencies.add("implementation", abcLib)
    app.dependencies.add("implementation", defLib)
    app.dependencies.add("implementation", xyzLib)

    val mapper: ProjectMapper = { proj ->
      when {
        proj.extensions.extraProperties.has("some-property") -> TestProject.ABC
        proj.path.contains("def") -> TestProject.DEF
        else -> TestProject.XYZ
      }
    }
    val customGenerator = ALL.copy(projectMapper = mapper)

    assertEquals(
      // language=dot
      """
      digraph {
      edge ["dir"="forward"]
      graph ["dpi"="100","label"="root","labelloc"="t","fontsize"="35"]
      node ["style"="filled"]
      ":app" ["shape"="assembly"]
      ":abc" ["fillcolor"="#FFFFFF","shape"="parallelogram"]
      ":def" ["fillcolor"="#ABCDEF","shape"="threepoverhang"]
      ":xyz" ["shape"="assembly"]
      {
      edge ["dir"="none"]
      graph ["rank"="same"]
      ":app"
      }
      ":app" -> ":abc" ["style"="dotted"]
      ":app" -> ":def" ["style"="dotted"]
      ":app" -> ":xyz" ["style"="dotted"]
      }
      """.trimIndent(),
      ProjectDependencyGraphGenerator(root, customGenerator).generateGraph().toString(),
    )
  }
}
