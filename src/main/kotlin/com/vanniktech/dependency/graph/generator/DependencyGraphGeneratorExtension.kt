package com.vanniktech.dependency.graph.generator

import groovy.lang.Closure
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Format.PNG
import guru.nidi.graphviz.engine.Format.SVG
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.util.Locale

/**
 * Extension for dependency graph generation.
 * @since 0.1.0
 */
open class DependencyGraphGeneratorExtension(project: Project) {
  /**
   * Generator extensions. By default this will yield a graph showing every project and library dependencies.
   * @since 0.1.0
   */
  var generators: NamedDomainObjectContainer<Generator> = project.container(Generator::class.java) { Generator(it) }.apply {
    add(Generator.ALL)
  }

  fun generators(closure: Closure<*>) {
    generators.configure(closure)
  }

  /**
   * ProjectGenerator extensions. By default this will yield a graph showing every project and it's project dependencies.
   */
  var projectGenerators: NamedDomainObjectContainer<ProjectGenerator> = project.container(ProjectGenerator::class.java) { ProjectGenerator(it) }.apply {
    add(ProjectGenerator.ALL)
  }

  fun projectGenerators(closure: Closure<*>) {
    projectGenerators.configure(closure)
  }

  /**
   * Generator allows you to filter and tweak between projects- as well as library dependencies.
   * @since 0.1.0
   */
  data class Generator @JvmOverloads constructor(
    /**
     * The name of this type of generator that should be in lowerCamelCase.
     * The task name as well as the output files will use this name.
     */
    @get:Input var name: String = "",
    /** Return true when you want to include this [ResolvedDependency], false otherwise. */
    @get:Nested var include: (ResolvedDependency) -> Boolean = { true },
    /** Return true when you want to include the children of this [ResolvedDependency], false otherwise. */
    @get:Nested var children: (ResolvedDependency) -> Boolean = { true },
    /** Allows to change the [MutableNode] for the given [ResolvedDependency]. */
    @get:Nested var dependencyNode: (MutableNode, ResolvedDependency) -> MutableNode = { node, _ -> node },
    /** Allows to change the [MutableNode] for the given [Project]. */
    @get:Nested var projectNode: (MutableNode, Project) -> MutableNode = { node, _ -> node },
    /** Optional label that can be displayed wrapped around the graph. */
    @get:Internal var label: Label? = null, // Not serializable making it unusable as an Input.
    /** Return true when you want to include this [Configuration], false otherwise. */
    @get:Nested var includeConfiguration: (Configuration) -> Boolean = {
      // By default, we'll include everything that's on the compileClassPath except test, UnitTest and AndroidTest configurations.
      val raw = it.name.replace("compileClasspath", "", ignoreCase = true)
      it.name.contains("compileClassPath", ignoreCase = true) && listOf("test", "AndroidTest", "UnitTest").none { raw.contains(it) }
    },
    /** Return true when you want to include this [Project], false otherwise. */
    @get:Nested var includeProject: (Project) -> Boolean = { true },
    /** Return the output [Format]s you'd like to have generated. */
    @get:Nested var outputFormats: List<Format> = listOf(PNG, SVG),
    /** Allows you to mutate the [MutableGraph] and add things as needed. */
    @get:Nested var graph: (MutableGraph) -> MutableGraph = { it },
  ) {
    /** Gradle task name that is associated with this generator. */
    @get:Internal val gradleTaskName = "generateDependencyGraph${
    name.replaceFirstChar {
      when {
        it.isLowerCase() -> it.titlecase(Locale.getDefault())
        else -> it.toString()
      }
    }
    }"
    @get:Internal internal val outputFileName = "dependency-graph${name.toHyphenCase().nonEmptyPrepend("-")}"
    @get:Internal internal val outputFileNameDot = "$outputFileName.dot"

    @get:[Optional Input] internal val rawLabel: String?
      get() = label?.toString()

    companion object {
      /** Default behavior which will include everything as is. */
      @JvmStatic val ALL = Generator()
    }
  }

  /**
   * ProjectGenerator allows you to filter and tweak between projects dependencies.
   * @since 0.6.0
   */
  data class ProjectGenerator @JvmOverloads constructor(
    /**
     * The name of this type of generator that should be in lowerCamelCase.
     * The task name as well as the output files will use this name.
     */
    @get:Input var name: String = "",
    /** Allows to change the [MutableNode] for the given [Project]. */
    @get:Nested var projectNode: (MutableNode, Project) -> MutableNode = { node, _ -> node },
    /** Return true when you want to include this [Project], false otherwise. */
    @get:Nested var includeProject: (Project) -> Boolean = { true },
    /** Return true when you want to include this [Configuration], false otherwise. */
    @get:Nested var includeConfiguration: (Configuration) -> Boolean = {
      // Filter out test configurations by default. Similar to how it's done in Generator.
      // We also filter out any configuration that starts with ios.
      // This is convenient when using Kotlin Multiplatform, and exporting more modules since transitive dependencies are not included by default.
      // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#export-dependencies-to-binaries
      !it.name.contains("test", ignoreCase = true) && !it.name.startsWith("ios")
    },
    /** Return the output [Format]s you'd like to have generated. */
    @get:Nested var outputFormats: List<Format> = listOf(PNG, SVG),
    /** Allows you to mutate the [MutableGraph] and add things as needed. */
    @get:Nested var graph: (MutableGraph) -> MutableGraph = { it },
  ) {
    /** Gradle task name that is associated with this generator. */
    @get:Internal val gradleTaskName = "generateProjectDependencyGraph${
    name.replaceFirstChar {
      when {
        it.isLowerCase() -> it.titlecase(Locale.getDefault())
        else -> it.toString()
      }
    }
    }"
    @get:Internal internal val outputFileName = "project-dependency-graph${name.toHyphenCase().nonEmptyPrepend("-")}"
    @get:Internal internal val outputFileNameDot = "$outputFileName.dot"

    companion object {
      /** Default behavior which will include everything as is. */
      @JvmStatic val ALL = ProjectGenerator()
    }
  }
}
