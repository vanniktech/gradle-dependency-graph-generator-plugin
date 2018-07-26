package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.model.Factory.mutGraph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
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

  private val nodes = mutableMapOf<String, MutableNode>()

  fun generateGraph(): MutableGraph {
    val graph = mutGraph("G").setDirected(true)

    generator.label?.let {
      graph.graphAttrs().add(it)
    }

    graph.nodeAttrs().add(Font.name("Times New Roman"))

    val projects = (if (project.subprojects.size > 0) project.subprojects else setOf(project))
        .filter { generator.includeProject(it) }

    // Generate top level projects.
    projects.forEach {
      val projectId = it.dotIdentifier
      val node = mutNode(projectId)
          .add(Label.of(it.name))
          .add(Shape.RECTANGLE)
      val tunedNode = generator.projectNode.invoke(node, it)
      nodes[projectId] = tunedNode
      graph.add(tunedNode)
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
          append(dependency, project.dotIdentifier, graph)
        }

    return generator.mutateGraph(graph)
  }

  private fun append(dependency: ResolvedDependency, parentIdentifier: String, graph: MutableGraph) {
    val identifier = (dependency.moduleGroup + dependency.moduleName).dotIdentifier
    val pair = parentIdentifier to identifier

    if (generator.include.invoke(dependency) && !addedConnections.contains(pair)) {
      addedConnections.add(pair)

      val node = mutNode(identifier)
          .add(Label.of(dependency.getDisplayName()))
          .add(Shape.RECTANGLE)

      val tunedNode = generator.dependencyNode.invoke(node, dependency)
      nodes[identifier] = tunedNode
      graph.add(tunedNode)

      nodes[parentIdentifier]?.addLink(nodes[identifier])

      if (generator.children.invoke(dependency)) {
        dependency.children.forEach { append(it, identifier, graph) }
      }
    }
  }

  private fun ResolvedDependency.getDisplayName() = when {
    moduleGroup.startsWith("android.arch.") -> moduleGroup
        .removePrefix("android.arch.")
        .replace('.', '-')
        .plus("-")
        .plus(moduleName)
    moduleGroup == "com.squareup.sqldelight" -> "sqldelight-$moduleName"
    moduleGroup == "org.jetbrains" && moduleName == "annotations" -> "jetbrains-annotations"
    else -> moduleName
  }
}

private val Project.dotIdentifier get() = "$group$name".dotIdentifier
