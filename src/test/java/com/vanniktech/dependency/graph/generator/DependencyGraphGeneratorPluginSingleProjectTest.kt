package com.vanniktech.dependency.graph.generator

import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DependencyGraphGeneratorPluginSingleProjectTest {
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
    assertThat(task.generator).isSameAs(DependencyGraphGeneratorExtension.Generator.ALL)
    assertThat(task.group).isEqualTo("reporting")
    assertThat(task.description).isEqualTo("Generates a dependency graph")
    assertThat(task.outputDirectory).hasToString(File(singleProject.buildDir, "reports/dependency-graph/").toString())
  }

  @Test fun taskPropertiesProject() {
    singleProject.plugins.apply(DependencyGraphGeneratorPlugin::class.java)

    singleProject.evaluate() // Need to call this for afterEvaluate() to pick up.

    val task = singleProject.tasks.getByName("generateProjectDependencyGraph") as ProjectDependencyGraphGeneratorTask
    assertThat(task.projectGenerator).isSameAs(DependencyGraphGeneratorExtension.ProjectGenerator.ALL)
    assertThat(task.group).isEqualTo("reporting")
    assertThat(task.description).isEqualTo("Generates a project dependency graph")
    assertThat(task.outputDirectory).hasToString(File(singleProject.buildDir, "reports/project-dependency-graph/").toString())
  }

  @Test fun integrationTestGradle50() {
    integrationTest("5.0")
  }

  @Suppress("Detekt.LongMethod") private fun integrationTest(gradleVersion: String) {
    val buildFile = testProjectDir.newFile("build.gradle")
    buildFile.writeText("""
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
        |  compile "org.jetbrains.kotlin:kotlin-stdlib:1.2.30"
        |  compile "io.reactivex.rxjava2:rxjava:2.1.10"
        |}
        |""".trimMargin())

    fun runBuild(): BuildResult {
      return GradleRunner.create()
        .withPluginClasspath()
        .withGradleVersion(gradleVersion)
        .withProjectDir(testProjectDir.root)
        .withArguments("generateDependencyGraph", "generateProjectDependencyGraph")
        .build()
    }

    val result = runBuild()
    assertThat(result.task(":generateDependencyGraph")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(result.task(":generateProjectDependencyGraph")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    // We don't want to assert the content of the images, just that they exist.
    assertThat(File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.png")).exists()
    assertThat(File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.svg")).exists()

    assertThat(File(testProjectDir.root, "build/reports/dependency-graph/dependency-graph.dot")).hasContent("""
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
        }""".trimIndent())

    // We don't want to assert the content of the images, just that they exist.
    assertThat(File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.png")).exists()
    assertThat(File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.svg")).exists()

    assertThat(File(testProjectDir.root, "build/reports/project-dependency-graph/project-dependency-graph.dot")).hasContent("""
        digraph {
        graph ["fontsize"="35","label"="${testProjectDir.root.name}","labelloc"="t"]
        node ["fontname"="Times New Roman","style"="filled"]
        {
        graph ["rank"="same"]
        }
        }""".trimIndent())

    val secondResult = runBuild()
    assertThat(secondResult.task(":generateDependencyGraph")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
    assertThat(secondResult.task(":generateProjectDependencyGraph")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)

    buildFile.appendText("""
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
      |""".trimMargin())

    val thirdResult = runBuild()
    assertThat(thirdResult.task(":generateDependencyGraph")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(thirdResult.task(":generateProjectDependencyGraph")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }
}
