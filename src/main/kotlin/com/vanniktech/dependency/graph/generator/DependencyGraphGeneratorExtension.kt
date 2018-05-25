package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Format.PNG
import guru.nidi.graphviz.model.MutableNode
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency

/**
 * Extension for dependency graph generation.
 * @since 0.1.0
 */
open class DependencyGraphGeneratorExtension {
  var generators: List<Generator> = listOf(ALL)

  /**
   * Generator allows you to customize which dependencies you want to select. Further you can tweak some of the formatting.
   * @since 0.1.0
   */
  data class Generator @JvmOverloads constructor(
    /**
     * The name of this type of generator that should be in lowerCamelCase.
     * The task name as well as the output files will use this name.
     */
    val name: String = "",
    /** Return true when you want to include this dependency, false otherwise. */
    val include: (ResolvedDependency) -> Boolean = { true },
    /** Return true when you want to include the children of this dependency, false otherwise. */
    val children: (ResolvedDependency) -> Boolean = { true },
    /** Allows to change the node for the given dependency. */
    val dependencyNode: (MutableNode, ResolvedDependency) -> MutableNode = { node, _ -> node },
    /** Allows to change the node for the given project. */
    val projectNode: (MutableNode, Project) -> MutableNode = { node, _ -> node },
    /** Optional label  that can be displayed wrapped around the graph. */
    val label: Label? = null,
    /** Return true when you want to include this configuration, false otherwise. */
    val includeConfiguration: (Configuration) -> Boolean = {
      // By default we'll include everything that's on the compileClassPath except test, UnitTest and AndroidTest configurations.
      val raw = it.name.replace("compileClasspath", "", ignoreCase = true)
      it.name.contains("compileClassPath", ignoreCase = true) && listOf("test", "AndroidTest", "UnitTest").none { raw.contains(it) }
    },
    /** Return true when you want to include this project, false otherwise. */
    val includeProject: (Project) -> Boolean = { true },
    /** Return the output formats you'd like to be generated. */
    val outputFormats: List<Format> = listOf(PNG)
  ) {
    /** Gradle task name that is associated with this generator. */
    val gradleTaskName = "generateDependencyGraph${name.capitalize()}"
    internal val outputFileName = "dependency-graph${name.toHyphenCase().nonEmptyPrepend("-")}"
    internal val outputFileNameDot = "$outputFileName.dot"

    companion object {
      /** Default behavior which will include everything as is. */
      @JvmStatic val ALL = Generator()
    }
  }
}
