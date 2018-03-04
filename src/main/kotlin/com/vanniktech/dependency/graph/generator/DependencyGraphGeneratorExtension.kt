package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import com.vanniktech.dependency.graph.generator.dot.GraphFormattingOptions
import com.vanniktech.dependency.graph.generator.dot.Header
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
    /** Optional suffix for the root - the project this generator is applied too. */
    val rootSuffix: String? = null,
    /** Return true when you want to include this dependency, false otherwise. */
    val include: (ResolvedDependency) -> Boolean = { true },
    /** Return true when you want to include the children of this dependency, false otherwise. */
    val children: (ResolvedDependency) -> Boolean = { true },
    /** Allows to tweak the formatting of a single dependency in the graph. */
    val dependencyFormattingOptions: (ResolvedDependency) -> GraphFormattingOptions = { GraphFormattingOptions() },
    /** The formatting options for the root - the project this generator is applied too. */
    val rootFormattingOptions: GraphFormattingOptions = GraphFormattingOptions(),
    /** Optional header that can be displayed wrapped around the graph. */
    val header: Header? = null
  ) {
    /** Gradle task name that is associated with this generator. */
    val gradleTaskName = "generateDependencyGraph${name.capitalize()}"
    private val outputFileName = "dependency-graph${name.toHyphenCase().nonEmptyPrepend("-")}"
    internal val outputFileNameDot = "$outputFileName.dot"

    /** Output file name of the genreated png which is stored on module level. */
    val outputFileNamePng = "$outputFileName.png"

    companion object {
      /** Default behavior which will include everything as is. */
      @JvmStatic val ALL = Generator()
    }
  }
}
