package com.vanniktech.dependency.graph.generator

import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DependencyGraphGeneratorPluginTest {
  @get:Rule val testProjectDir = TemporaryFolder()

  private lateinit var singleProject: DefaultProject

  @Before fun setUp() {
    // Casting this to DefaultProject so we can call evaluate later.
    singleProject = ProjectBuilder.builder().withName("single").build() as DefaultProject
    singleProject.plugins.apply(JavaLibraryPlugin::class.java)
    singleProject.repositories.run { add(mavenCentral()) }
    singleProject.dependencies.add("api", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")
    singleProject.dependencies.add("implementation", "io.reactivex.rxjava2:rxjava:2.1.10")
  }

  @Test fun taskProperties() {
    singleProject.plugins.apply(DependencyGraphGeneratorPlugin::class.java)

    singleProject.evaluate() // Need to call this for afterEvaluate() to pick up.

    val task = singleProject.tasks.getByName("generateDependencyGraph") as DependencyGraphGeneratorTask
    assertEquals(true, task.generator === DependencyGraphGeneratorExtension.Generator.ALL)
    assertEquals("reporting", task.group)
    assertEquals("Generates a dependency graph", task.description)
    assertEquals(File(singleProject.buildDir, "reports/dependency-graph/").toString(), task.outputDirectory.toString())
  }

  @Test fun taskPropertiesProject() {
    singleProject.plugins.apply(DependencyGraphGeneratorPlugin::class.java)

    singleProject.evaluate() // Need to call this for afterEvaluate() to pick up.

    val task = singleProject.tasks.getByName("generateProjectDependencyGraph") as ProjectDependencyGraphGeneratorTask
    assertEquals(true, task.projectGenerator === DependencyGraphGeneratorExtension.ProjectGenerator.ALL)
    assertEquals("reporting", task.group)
    assertEquals("Generates a project dependency graph", task.description)
    assertEquals(File(singleProject.buildDir, "reports/project-dependency-graph/").toString(), task.outputDirectory.toString())
  }

  @Test fun integrationTestGradle742() {
    integrationTest("7.4.2")
  }

  @Suppress("Detekt.LongMethod") private fun integrationTest(gradleVersion: String) {
    val buildFile = testProjectDir.newFile("build.gradle")
    buildFile.writeText(
      """
        |plugins {
        |  id "java"
        |  id "com.vanniktech.dependency.graph.generator"
        |}
        |
        |repositories {
        |  mavenCentral()
        |}
        |
        |dependencies {
        |  implementation "org.jetbrains.kotlin:kotlin-stdlib:1.2.30"
        |  implementation "io.reactivex.rxjava2:rxjava:2.1.10"
        |}
        |
      """.trimMargin()
    )

    fun runBuild(): BuildResult {
      return GradleRunner.create()
        .withPluginClasspath()
        .withGradleVersion(gradleVersion)
        .withProjectDir(testProjectDir.root)
        .withArguments("generateDependencyGraph", "generateProjectDependencyGraph")
        .build()
    }

    val result = runBuild()
    assertEquals(TaskOutcome.SUCCESS, result.task(":generateDependencyGraph")?.outcome)
    assertEquals(TaskOutcome.SUCCESS, result.task(":generateProjectDependencyGraph")?.outcome)

    // We don't want to assert the content of the images, just that they exist.
    assertEquals(true, File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.png").exists())
    assertEquals(true, File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.svg").exists())

    assertEquals(
      """
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "${testProjectDir.root.name}" ["shape"="rectangle","label"="${testProjectDir.root.name}"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        {
        graph ["rank"="same"]
        "${testProjectDir.root.name}"
        }
        "${testProjectDir.root.name}" -> "orgjetbrainskotlinkotlinstdlib"
        "${testProjectDir.root.name}" -> "ioreactivexrxjava2rxjava"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        }
      """.trimIndent(),
      File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.dot").readText()
    )

    // We don't want to assert the content of the images, just that they exist.
    assertEquals(true, File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.png").exists())
    assertEquals(true, File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.svg").exists())

    assertEquals(
      """
        digraph {
        graph ["fontsize"="35","label"="${testProjectDir.root.name}","labelloc"="t"]
        node ["fontname"="Times New Roman","style"="filled"]
        {
        graph ["rank"="same"]
        }
        }
      """.trimIndent(),
      File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.dot").readText()
    )

    val secondResult = runBuild()
    assertEquals(TaskOutcome.UP_TO_DATE, secondResult.task(":generateDependencyGraph")?.outcome)
    assertEquals(TaskOutcome.UP_TO_DATE, secondResult.task(":generateProjectDependencyGraph")?.outcome)

    buildFile.appendText(
      """
      |import guru.nidi.graphviz.engine.Format
      |dependencyGraphGenerator {
      |  generators {
      |    configureEach {
      |      it.outputFormats = [Format.SVG]
      |    }
      |  }
      |  projectGenerators {
      |    configureEach {
      |      it.outputFormats = [Format.SVG]
      |    }
      |  }
      |}
      |
      """.trimMargin()
    )

    val thirdResult = runBuild()
    assertEquals(TaskOutcome.SUCCESS, thirdResult.task(":generateDependencyGraph")?.outcome)
    assertEquals(TaskOutcome.SUCCESS, thirdResult.task(":generateProjectDependencyGraph")?.outcome)
  }

  @Test @Suppress("Detekt.LongMethod") fun multiProjectIntegrationTest() {
    testProjectDir.newFile("build.gradle").writeText(
      """
        |plugins {
        |  id "com.vanniktech.dependency.graph.generator"
        |}
        |
      """.trimMargin()
    )

    testProjectDir.newFile("settings.gradle").writeText(
      """
        |include ":lib"
        |include ":lib1"
        |include ":lib2"
        |include ":app"
        |include ":empty"
        |
      """.trimMargin()
    )

    val lib = testProjectDir.newFolder("lib").run { parentFile.name + name }
    testProjectDir.newFile("lib/build.gradle").writeText(
      """
        |plugins { id "java-library" }
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  api "io.reactivex.rxjava2:rxjava:2.1.10"
        |}
        |
      """.trimMargin()
    )

    val lib1 = testProjectDir.newFolder("lib1").run { parentFile.name + name }
    testProjectDir.newFile("lib1/build.gradle").writeText(
      """
        |plugins { id "java-library" }
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  api project(":lib")
        |  implementation "org.jetbrains.kotlin:kotlin-stdlib:1.2.30"
        |}
        |
      """.trimMargin()
    )

    val lib2 = testProjectDir.newFolder("lib2").run { parentFile.name + name }
    testProjectDir.newFile("lib2/build.gradle").writeText(
      """
        |plugins { id "java-library" }
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  api project(":lib")
        |}
        |
      """.trimMargin()
    )

    val app = testProjectDir.newFolder("app").run { parentFile.name + name }
    testProjectDir.newFile("app/build.gradle").writeText(
      """
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
        |
      """.trimMargin()
    )

    val empty = testProjectDir.newFolder("empty").run { parentFile.name + name }

    val result = GradleRunner.create()
      .withPluginClasspath()
      .withGradleVersion("7.4.2")
      .withProjectDir(testProjectDir.root)
      .withArguments("generateDependencyGraph", "generateProjectDependencyGraph", "app:generateProjectDependencyGraph")
      .build()

    result.tasks.filter { it.path.contains("DependencyGraph") }.forEach {
      assertEquals(TaskOutcome.SUCCESS, it?.outcome)
    }

    // We don't want to assert the content of the image, just that it exists.
    assertEquals(true, File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.svg").exists())

    assertEquals(
      """
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "$app" ["shape"="rectangle","label"="app"]
        "$lib1" ["shape"="rectangle","label"="lib1"]
        "$lib" ["shape"="rectangle","label"="lib"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        "$lib2" ["shape"="rectangle","label"="lib2"]
        "$empty" ["shape"="rectangle","label"="empty"]
        {
        graph ["rank"="same"]
        "$app"
        "$empty"
        }
        "$app" -> "$lib1"
        "$app" -> "$lib2"
        "$lib1" -> "$lib"
        "$lib1" -> "orgjetbrainskotlinkotlinstdlib"
        "$lib" -> "ioreactivexrxjava2rxjava"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "$lib2" -> "$lib"
        }
      """.trimIndent(),
      File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.dot").readText()
    )

    // We don't want to assert the content of the image, just that it exists.
    assertEquals(true, File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.svg").exists())

    fun projectDependencyGraph(label: String) = """
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
        }
    """.trimIndent()

    assertEquals(projectDependencyGraph(testProjectDir.root.name), File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.dot").readText())

    // We don't want to assert the content of the image, just that it exists.
    assertEquals(true, File(testProjectDir.root, "app/build/reports/project-dependency-graph/project-dependency-graph.svg").exists())

    assertEquals(projectDependencyGraph("app"), File(testProjectDir.root, "app/build/reports/project-dependency-graph/project-dependency-graph.dot").readText())
  }
}
