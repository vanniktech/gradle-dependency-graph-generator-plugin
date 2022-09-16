package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Rank.RankType
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.model.Factory.graph
import guru.nidi.graphviz.model.Factory.mutGraph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency

internal class DependencyGraphGenerator(
  private val project: Project,
  private val generator: Generator,
) {
  // We keep a map of an identifier to a parent identifier in order to not add unique dependencies more than once.
  // One scenario is A depending on B and B on C.
  // If a module depends on both A and B the connection from B to C could be wired twice.
  private val addedConnections = mutableSetOf<Pair<DotIdentifier, DotIdentifier>>()

  private val nodes = mutableMapOf<DotIdentifier, MutableNode>()

  @Suppress("Detekt.SpreadOperator") fun generateGraph(): MutableGraph {
    val graph = mutGraph("G").setDirected(true).graphAttrs().add(GraphAttr.dpi(100))
    generator.label?.let {
      graph.graphAttrs().add(it)
    }

    val projects = (if (project.subprojects.size > 0) project.subprojects else setOf(project))
      .filter { generator.includeProject(it) }

    val rootNodes = mutableSetOf<DotIdentifier>()

    // Generate top level projects.
    projects.forEach {
      val projectId = it.dotIdentifier
      val node = mutNode(projectId.value)
        .add(Label.of(it.name))
        .add(Shape.RECTANGLE)
      val tunedNode = generator.projectNode.invoke(node, it)
      nodes[projectId] = tunedNode
      graph.add(tunedNode)
      rootNodes.add(projectId)
    }

    // Let's gather everything and put it in the file.
    projects
      .flatMap { project ->
        project.configurations
          .filter { it.isCanBeResolved }
          .filter { generator.includeConfiguration.invoke(it) }
          .flatMap { it.resolvedConfiguration.firstLevelModuleDependencies }
          .map { DependencyContainer(project) to it }
      }
      .forEach { (project, dependency) -> append(dependency, project, graph, rootNodes) }

    if (rootNodes.isNotEmpty()) {
      graph.add(
        graph()
          .graphAttr()
          .with(Rank.inSubgraph(RankType.SAME))
          .with(*rootNodes.map { mutNode(it.value) }.toTypedArray()),
      )
    }

    return generator.graph(graph)
  }

  private fun append(
    dependency: ResolvedDependency,
    parent: DependencyContainer,
    graph: MutableGraph,
    rootNodes: MutableSet<DotIdentifier>,
  ) {
    val identifier = dependency.dotIdentifier
    val pair = parent.dotIdentifier to identifier

    if (pair in addedConnections) return
    if (!generator.include(dependency)) return

    addedConnections.add(pair)

    val node = mutNode(identifier.value)
      .add(Label.of(dependency.getDisplayName()))
      .add(Shape.RECTANGLE)

    val mutated = generator.dependencyNode.invoke(node, dependency)
    nodes[identifier] = mutated
    graph.add(mutated)

    rootNodes.remove(identifier)

    nodes[parent.dotIdentifier]?.apply {
      addLink(generator.link(linkTo(mutated), parent, DependencyContainer(dependency)))
    }

    if (generator.children.invoke(dependency)) {
      dependency.children.forEach { append(it, DependencyContainer(dependency), graph, rootNodes) }
    }
  }

  private fun ResolvedDependency.getDisplayName() = when {
    moduleGroup.startsWith("android.arch.") ->
      moduleGroup
        .removePrefix("android.arch.")
        .replace('.', '-')
        .plus("-")
        .plus(moduleName)
    moduleGroup == "com.squareup.sqldelight" -> "sqldelight-$moduleName"
    moduleGroup == "org.jetbrains" && moduleName == "annotations" -> "jetbrains-annotations"
    else -> moduleName
  }
}
