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
import org.gradle.api.tasks.Optional

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
    /** Return true when you want to include this dependency, false otherwise. */
    @get:Input var include: (ResolvedDependency) -> Boolean = { true },
    /** Return true when you want to include the children of this dependency, false otherwise. */
    @get:Input var children: (ResolvedDependency) -> Boolean = { true },
    /** Allows to change the node for the given dependency. */
    @get:Input var dependencyNode: (MutableNode, ResolvedDependency) -> MutableNode = { node, _ -> node },
    /** Allows to change the node for the given project. */
    @get:Input var projectNode: (MutableNode, Project) -> MutableNode = { node, _ -> node },
    /** Optional label that can be displayed wrapped around the graph. */
    @get:Internal var label: Label? = null, // Not serializable making it unusable as an Input
    /** Return true when you want to include this configuration, false otherwise. */
    @get:Input var includeConfiguration: (Configuration) -> Boolean = {
      // By default we'll include everything that's on the compileClassPath except test, UnitTest and AndroidTest configurations.
      val raw = it.name.replace("compileClasspath", "", ignoreCase = true)
      it.name.contains("compileClassPath", ignoreCase = true) && listOf("test", "AndroidTest", "UnitTest").none { raw.contains(it) }
    },
    /** Return true when you want to include this project, false otherwise. */
    @get:Input var includeProject: (Project) -> Boolean = { true },
    /** Return the output formats you'd like to be generated. */
    @get:Input var outputFormats: List<Format> = listOf(PNG, SVG),
    /** Allows you to mutate the graph and add things as needed. */
    @get:Input var graph: (MutableGraph) -> MutableGraph = { it }
  ) {
    /** Gradle task name that is associated with this generator. */
    @get:Internal val gradleTaskName = "generateDependencyGraph${name.capitalize()}"
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
    /** Allows to change the label for the given project. */
    @get:Input var projectLabel: (Project) -> String = { it.path },
    /** Allows to change the node for the given project. */
    @get:Input var projectNode: (MutableNode, Project) -> MutableNode = { node, _ -> node },
    /** Return true when you want to include this project, false otherwise. */
    @get:Input var includeProject: (Project) -> Boolean = { true },
    /** Return the output formats you'd like to be generated. */
    @get:Input var outputFormats: List<Format> = listOf(PNG, SVG),
    /** Allows you to mutate the graph and add things as needed. */
    @get:Input var graph: (MutableGraph) -> MutableGraph = { it }
  ) {
    /** Gradle task name that is associated with this generator. */
    @get:Internal val gradleTaskName = "generateProjectDependencyGraph${name.capitalize()}"
    @get:Internal val projectDependenciesTaskName = "projectDependencies${name.capitalize()}"
    @get:Internal internal val outputFileName = "project-dependency-graph${name.toHyphenCase().nonEmptyPrepend("-")}"
    @get:Internal internal val outputFileNameDot = "$outputFileName.dot"

    companion object {
      /** Default behavior which will include everything as is. */
      @JvmStatic val ALL = ProjectGenerator()
    }
  }
}
