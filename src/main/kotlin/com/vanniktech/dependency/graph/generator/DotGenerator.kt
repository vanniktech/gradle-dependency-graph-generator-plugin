package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency

internal class DotGenerator(
  private val project: Project,
  private val generator: Generator
) {
  // We keep a map of an identifier to a parent identifier in order to not add unique dependencies more than once.
  // One scenario is A depending on B and B on C.
  // If a module depends on both A and B the connection from B to C could be wired twice.
  private val addedConnections = mutableSetOf<Pair<String, String>>()

  // Dependencies that we might have already added.
  // This becomes used when a project depends on another project.
  private val addedDependencies = mutableSetOf<String>()

  fun generateContent(): String {
    // Some general documentation about dot. http://www.graphviz.org/pdf/dotguide.pdf
    val content = StringBuilder("digraph G {\n")

    generator.header?.let {
      content.append("  $it;\n")
    }

    val projects = (if (project.subprojects.size > 0) project.subprojects else setOf(project))
        .filter { generator.includeProject(it) }

    // Generate top level projects.
    projects.forEach {
      val projectId = it.dotIdentifier
      val settings = generator.rootFormattingOptions.withLabel(it.name)
      content.append("  $projectId $settings;\n")
      addedDependencies.add(projectId)
    }

    if (projects.size > 1) {
      // All projects should be displayed on the same level.
      val ranking = projects.joinToString(separator = "; ") { "\"${it.dotIdentifier}\"" }
      content.append("  { rank = same; $ranking };\n")
    }

    // Let's gather everything and put it in the file.
    projects
        .flatMap { project ->
          project.configurations
              .filter { it.isCanBeResolved }
              .filter { generator.includeConfiguration.invoke(it) }
              .flatMap { it.resolvedConfiguration.firstLevelModuleDependencies }
              .map { project to it }
        }
        .forEach { (project, dependency) ->
          append(dependency, project.dotIdentifier, content)
        }

    return content.append("}\n").toString()
  }

  private fun append(dependency: ResolvedDependency, parentIdentifier: String, content: StringBuilder) {
    if (generator.include.invoke(dependency)) {
      val identifier = (dependency.moduleGroup + dependency.moduleName).dotIdentifier

      val pair = parentIdentifier to identifier
      if (!addedConnections.contains(pair)) { // We don't want to re-add the same dependencies.
        addedConnections.add(pair)

        // Special case since Android Architecture components have weird module names.
        val name = if (dependency.moduleGroup.startsWith("android.arch.")) {
          dependency.moduleGroup
              .replace("android.arch.", "")
              .replace('.', '-')
              .plus("-")
              .plus(dependency.moduleName)
        } else {
          dependency.moduleName
        }

        if (!addedDependencies.contains(identifier)) {
          content.append("  $identifier ${generator.dependencyFormattingOptions.invoke(dependency).withLabel(name)};\n")
        }

        content.append("  $parentIdentifier -> $identifier;\n")
      }

      if (generator.children.invoke(dependency)) {
        dependency.children.forEach { append(it, identifier, content) }
      }
    }
  }
}

private val Project.dotIdentifier get() = "$group$name".dotIdentifier
