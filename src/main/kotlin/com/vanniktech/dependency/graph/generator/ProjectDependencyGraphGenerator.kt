package com.vanniktech.dependency.graph.generator

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Factory.graph
import guru.nidi.graphviz.model.Factory.mutGraph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import org.gradle.api.Project

// Based on https://github.com/JakeWharton/SdkSearch/blob/766d612ed52cdf3af9cd0728b6afd87006746ae5/gradle/projectDependencyGraph.gradle
internal class ProjectDependencyGraphGenerator(
  private val project: Project,
  private val projectGenerator: DependencyGraphGeneratorExtension.ProjectGenerator
) {
  fun generateGraph(): MutableGraph {
    val (projects, dependencies) = project.getProjectDependencies(projectGenerator)

    val graph = mutGraph().setDirected(true)
    graph.graphAttrs().add(Label.of(project.name).locate(Label.Location.TOP), Font.size(DEFAULT_FONT_SIZE))
    graph.nodeAttrs().add(Font.name("Times New Roman"), Style.FILLED)
    projects.forEach { addNode(it, dependencies, graph) }
    rankRootProjects(graph, projects, dependencies)
    addDependencies(dependencies, graph)

    return projectGenerator.graph(graph)
  }

  private fun addNode(project: Project, dependencies: List<Connection>, graph: MutableGraph) {
    val node = mutNode(projectGenerator.projectLabel(project))

    if (dependencies.none { it.to == project }) {
      node.add(Shape.RECTANGLE)
    } else if (project.isCommonsProject()) {
      node.add(Style.DASHED, Color.BLACK)
    }

    when {
      project.isJsProject() -> node.add(Color.rgb("#fff176").fill())
      project.isAndroidProject() -> node.add(Color.rgb("#81c784").fill())
      project.isKotlinProject() -> node.add(Color.rgb("#ffb74d").fill())
      project.isJavaProject() -> node.add(Color.rgb("#ff8a65").fill())
      else -> node.add(Color.rgb("#e0e0e0").fill())
    }

    graph.add(projectGenerator.projectNode(node, project))
  }

  @Suppress("Detekt.SpreadOperator") private fun rankRootProjects(graph: MutableGraph, projects: Set<Project>, dependencies: List<Connection>) {
    graph.add(graph()
        .graphAttr()
        .with(Rank.SAME)
        .with(*projects.filter { project -> dependencies.none { it.to == project } }.map { mutNode(projectGenerator.projectLabel(it)) }.toTypedArray()))
  }

  private fun addDependencies(dependencies: List<Connection>, graph: MutableGraph) {
    dependencies
        .filterNot { (from, to, _) -> !from.isCommonsProject() && to.isCommonsProject() }
        .distinctBy { (from, to, _) -> from to to }
        .forEach { (from, to, isImplementation) ->
          val fromNode = graph.nodes().find { it.name().toString() == projectGenerator.projectLabel(from) }
          val toNode = graph.nodes().find { it.name().toString() == projectGenerator.projectLabel(to) }

          if (fromNode != null && toNode != null) {
            val link = Link.to(toNode)
            graph.add(fromNode.addLink(if (isImplementation) link.with(Style.DOTTED) else link))
          }
        }
  }

  internal data class Connection(
    val from: Project,
    val to: Project,
    val isImplementation: Boolean
  )

  internal companion object {
    const val DEFAULT_FONT_SIZE = 35
  }
}
