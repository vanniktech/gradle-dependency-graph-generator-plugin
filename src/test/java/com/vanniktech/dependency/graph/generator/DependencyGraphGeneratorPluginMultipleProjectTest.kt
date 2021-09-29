package com.vanniktech.dependency.graph.generator

import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DependencyGraphGeneratorPluginMultipleProjectTest {
  @get:Rule val testProjectDir = TemporaryFolder()

  private fun buildMultiProject(vararg task: String): BuildResult {
    testProjectDir.newFile("build.gradle").writeText("""
        |plugins {
        |  id "com.vanniktech.dependency.graph.generator"
        |}
        |""".trimMargin())

    testProjectDir.newFile("settings.gradle").writeText("""
        |include ":lib"
        |include ":lib1"
        |include ":lib2"
        |include ":app"
        |include ":empty"
        |""".trimMargin())

    testProjectDir.newFolder("lib")
    testProjectDir.newFile("lib/build.gradle").writeText("""
        |plugins { id "java-library" }
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  api "io.reactivex.rxjava2:rxjava:2.1.10"
        |}
        |""".trimMargin())

    testProjectDir.newFolder("lib1")
    testProjectDir.newFile("lib1/build.gradle").writeText("""
        |plugins { id "java-library" }
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  api project(":lib")
        |  implementation "org.jetbrains.kotlin:kotlin-stdlib:1.2.30"
        |}
        |""".trimMargin())

    testProjectDir.newFolder("lib2")
    testProjectDir.newFile("lib2/build.gradle").writeText("""
        |plugins { id "java-library" }
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  api project(":lib")
        |}
        |""".trimMargin())

    testProjectDir.newFolder("app")
    testProjectDir.newFile("app/build.gradle").writeText("""
        |plugins {
        |  id "java-library"
        |  id "com.vanniktech.dependency.graph.generator"
        |}
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  implementation project(":lib1")
        |  implementation project(":lib2")
        |}
        |""".trimMargin())

    testProjectDir.newFolder("empty").run { parentFile.name + name }

    return GradleRunner.create()
      .withPluginClasspath()
      .withGradleVersion("5.0")
      .withProjectDir(testProjectDir.root)
      .withArguments(*task)
      .build()
  }

  private fun projectDependencyGraph(label: String) = """
        digraph {
        graph ["fontsize"="35","label"="$label","labelloc"="t"]
        node ["fontname"="Times New Roman","style"="filled"]
        ":app" ["fillcolor"="#ff8a65","shape"="rectangle"]
        ":lib1" ["fillcolor"="#ff8a65"]
        ":lib" ["fillcolor"="#ff8a65"]
        ":lib2" ["fillcolor"="#ff8a65"]
        {
        graph ["rank"="same"]
        ":app"
        }
        ":app" -> ":lib1" ["style"="dotted"]
        ":app" -> ":lib2" ["style"="dotted"]
        ":lib1" -> ":lib"
        ":lib2" -> ":lib"
        }""".trimIndent()

  @Test @Suppress("Detekt.LongMethod") fun rootProjectIntegrationTest() {
    val result = buildMultiProject("generateDependencyGraph", "generateProjectDependencyGraph")
    val project = testProjectDir.root.name

    result.tasks.forEach {
      assertThat(it.path).endsWith("DependencyGraph")
      assertThat(it.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    // We don't want to assert the content of the image, just that it exists.
    assertThat(File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.svg")).exists()

    assertThat(File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.dot")).hasContent("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "${project}app" ["shape"="rectangle","label"="app"]
        "${project}lib1" ["shape"="rectangle","label"="lib1"]
        "${project}lib" ["shape"="rectangle","label"="lib"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        "${project}lib2" ["shape"="rectangle","label"="lib2"]
        "${project}empty" ["shape"="rectangle","label"="empty"]
        {
        graph ["rank"="same"]
        "${project}app"
        "${project}empty"
        }
        "${project}app" -> "${project}lib1"
        "${project}app" -> "${project}lib2"
        "${project}lib1" -> "${project}lib"
        "${project}lib1" -> "orgjetbrainskotlinkotlinstdlib"
        "${project}lib" -> "ioreactivexrxjava2rxjava"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "${project}lib2" -> "${project}lib"
        }""".trimIndent())

    // We don't want to assert the content of the image, just that it exists.
    assertThat(File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.svg")).exists()

    assertThat(File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.dot")).hasContent(projectDependencyGraph(testProjectDir.root.name))
  }

  @Test @Suppress("Detekt.LongMethod") fun subProjectDependencyGraphTest() {
    val result = buildMultiProject("app:generateProjectDependencyGraph")

    result.tasks.first().let {
      assertThat(it.path).isEqualTo(":app:generateProjectDependencyGraph")
      assertThat(it.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    // We don't want to assert the content of the image, just that it exists.
    assertThat(File(testProjectDir.root, "app/build/reports/project-dependency-graph/project-dependency-graph.svg")).exists()

    assertThat(File(testProjectDir.root, "app/build/reports/project-dependency-graph/project-dependency-graph.dot")).hasContent(projectDependencyGraph("app"))
  }

  @Test @Suppress("Detekt.LongMethod") fun subProjectDependenciesTest() {
    val result = buildMultiProject("app:projectDependencies")

    result.tasks.first().let {
      assertThat(it.path).isEqualTo(":app:projectDependencies")
      assertThat(it.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    assertThat(result.output).contains(":app:projectDependencies\n:lib\n:lib1\n:lib2")
  }
}
