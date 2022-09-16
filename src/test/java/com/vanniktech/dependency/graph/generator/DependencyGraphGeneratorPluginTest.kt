package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
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
    assertEquals(true, task.generator === Generator.ALL)
    assertEquals("reporting", task.group)
    assertEquals("Generates a dependency graph", task.description)
    assertEquals(File(singleProject.buildDir, "reports/dependency-graph/").toString(), task.outputDirectory.toString())
  }

  @Test fun taskPropertiesProject() {
    singleProject.plugins.apply(DependencyGraphGeneratorPlugin::class.java)

    singleProject.evaluate() // Need to call this for afterEvaluate() to pick up.

    val task = singleProject.tasks.getByName("generateProjectDependencyGraph") as ProjectDependencyGraphGeneratorTask
    assertEquals(true, task.projectGenerator === ProjectGenerator.ALL)
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
      // language=groovy
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
      """.trimMargin(),
    )

    fun runBuild(): BuildResult {
      return GradleRunner.create().withDebug(true)
        .withPluginClasspath()
        .withGradleVersion(gradleVersion)
        .withProjectDir(testProjectDir.root)
        .withArguments("generateDependencyGraph", "generateProjectDependencyGraph", "-Dorg.gradle.jvmargs=-Xmx2048m", "--stacktrace")
        .build()
    }

    val result = runBuild()
    assertEquals(TaskOutcome.SUCCESS, result.task(":generateDependencyGraph")?.outcome)
    assertEquals(TaskOutcome.SUCCESS, result.task(":generateProjectDependencyGraph")?.outcome)

    // We don't want to assert the content of the images, just that they exist and that their path is logged
    Generator.ALL.outputFormats.forEach {
      val file = File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.${it.fileExtension}")
      assertEquals(true, file.exists())
      assertEquals(true, file.absolutePath in result.output)
    }
    Generator.ALL.outputFormats.map {
      File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.${it.fileExtension}")
    }.forEach {
      assertEquals(true, it.exists())
      assertEquals(true, it.absolutePath in result.output)
    }

    val dependencyGraphDot = File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.dot")
    assertEquals(true, dependencyGraphDot.absolutePath in result.output)
    assertEquals(
      // language=dot
      """
        digraph "G" {
        edge ["dir"="forward"]
        graph ["dpi"="100"]
        "${testProjectDir.root.name}" ["label"="${testProjectDir.root.name}","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "${testProjectDir.root.name}"
        }
        "${testProjectDir.root.name}" -> "orgjetbrainskotlinkotlinstdlib"
        "${testProjectDir.root.name}" -> "ioreactivexrxjava2rxjava"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        }
      """.trimIndent(),
      dependencyGraphDot.readText(),
    )

    // We don't want to assert the content of the images, just that they exist and that their path is logged
    ProjectGenerator.ALL.outputFormats.map {
      File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.${it.fileExtension}")
    }.forEach {
      assertEquals(true, it.exists())
      assertEquals(true, it.absolutePath in result.output)
    }

    val projectDependencyGraphDot = File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.dot")
    assertEquals(true, projectDependencyGraphDot.absolutePath in result.output)
    assertEquals(
      // language=dot
      """
        digraph {
        edge ["dir"="forward"]
        graph ["dpi"="100","label"="${testProjectDir.root.name}","labelloc"="t","fontsize"="35"]
        node ["style"="filled"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        }
        }
      """.trimIndent(),
      projectDependencyGraphDot.readText(),
    )

    val secondResult = runBuild()
    assertEquals(TaskOutcome.UP_TO_DATE, secondResult.task(":generateDependencyGraph")?.outcome)
    assertEquals(TaskOutcome.UP_TO_DATE, secondResult.task(":generateProjectDependencyGraph")?.outcome)

    buildFile.appendText(
      // language=groovy
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
      """.trimMargin(),
    )

    val thirdResult = runBuild()
    assertEquals(TaskOutcome.SUCCESS, thirdResult.task(":generateDependencyGraph")?.outcome)
    assertEquals(TaskOutcome.SUCCESS, thirdResult.task(":generateProjectDependencyGraph")?.outcome)
  }

  @Test @Suppress("Detekt.LongMethod") fun multiProjectIntegrationTest() {
    testProjectDir.newFile("build.gradle").writeText(
      // language=groovy
      """
        |plugins {
        |  id "com.vanniktech.dependency.graph.generator"
        |}
        |
      """.trimMargin(),
    )

    testProjectDir.newFile("settings.gradle").writeText(
      // language=groovy
      """
        |include ":lib"
        |include ":lib1"
        |include ":lib2"
        |include ":app"
        |include ":empty"
        |
      """.trimMargin(),
    )

    val lib = testProjectDir.newFolder("lib").run { parentFile.name + name }
    testProjectDir.newFile("lib/build.gradle").writeText(
      // language=groovy
      """
        |plugins { id "java-library" }
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  api "io.reactivex.rxjava2:rxjava:2.1.10"
        |}
        |
      """.trimMargin(),
    )

    val lib1 = testProjectDir.newFolder("lib1").run { parentFile.name + name }
    testProjectDir.newFile("lib1/build.gradle").writeText(
      // language=groovy
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
      """.trimMargin(),
    )

    val lib2 = testProjectDir.newFolder("lib2").run { parentFile.name + name }
    testProjectDir.newFile("lib2/build.gradle").writeText(
      // language=groovy
      """
        |plugins { id "java-library" }
        |
        |repositories { mavenCentral() }
        |
        |dependencies {
        |  api project(":lib")
        |}
        |
      """.trimMargin(),
    )

    val app = testProjectDir.newFolder("app").run { parentFile.name + name }
    testProjectDir.newFile("app/build.gradle").writeText(
      // language=groovy
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
      """.trimMargin(),
    )

    val empty = testProjectDir.newFolder("empty").run { parentFile.name + name }

    val result = GradleRunner.create().withDebug(true)
      .withPluginClasspath()
      .withGradleVersion("7.4.2")
      .withProjectDir(testProjectDir.root)
      .withArguments("generateDependencyGraph", "generateProjectDependencyGraph", "app:generateProjectDependencyGraph", "-Dorg.gradle.jvmargs=-Xmx2048m", "--stacktrace")
      .build()

    result.tasks.filter { it.path.contains("DependencyGraph") }.forEach {
      assertEquals(TaskOutcome.SUCCESS, it?.outcome)
    }

    // We don't want to assert the content of the image, just that it exists.
    assertEquals(true, File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.svg").exists())

    assertEquals(
      // language=dot
      """
        digraph "G" {
        edge ["dir"="forward"]
        graph ["dpi"="100"]
        "$app" ["label"="app","shape"="rectangle"]
        "$lib1" ["label"="lib1","shape"="rectangle"]
        "$lib" ["label"="lib","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        "$lib2" ["label"="lib2","shape"="rectangle"]
        "$empty" ["label"="empty","shape"="rectangle"]
        {
        edge ["dir"="none"]
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
      File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.dot").readText(),
    )

    // We don't want to assert the content of the image, just that it exists.
    assertEquals(true, File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.svg").exists())

    @Language("dot")
    fun projectDependencyGraph(label: String) = """
        digraph {
        edge ["dir"="forward"]
        graph ["dpi"="100","label"="$label","labelloc"="t","fontsize"="35"]
        node ["style"="filled"]
        ":app" ["shape"="rectangle","fillcolor"="#FF7043"]
        ":lib1" ["fillcolor"="#FF7043"]
        ":lib" ["fillcolor"="#FF7043"]
        ":lib2" ["fillcolor"="#FF7043"]
        {
        edge ["dir"="none"]
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
