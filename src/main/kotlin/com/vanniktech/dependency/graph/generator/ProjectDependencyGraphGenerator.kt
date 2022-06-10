package com.vanniktech.dependency.graph.generator

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Rank.RankType
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Factory.graph
import guru.nidi.graphviz.model.Factory.mutGraph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

// Based on https://github.com/JakeWharton/SdkSearch/blob/766d612ed52cdf3af9cd0728b6afd87006746ae5/gradle/projectDependencyGraph.gradle
internal class ProjectDependencyGraphGenerator(
  private val project: Project,
  private val projectGenerator: DependencyGraphGeneratorExtension.ProjectGenerator
) {
  fun generateGraph(): MutableGraph {
    val projects = mutableSetOf<Project>()
    val dependencies = mutableListOf<ProjectDependencyContainer>()
    fun addProject(project: Project) {
      if (projectGenerator.includeProject(project) && projects.add(project)) {
        project.configurations
          .filter { projectGenerator.includeConfiguration.invoke(it) }
          .flatMap { configuration ->
            configuration.dependencies
              .withType(ProjectDependency::class.java)
              .map { ProjectDependencyContainer(project, it.dependencyProject, configuration.name.lowercase().endsWith("implementation")) }
          }
          .forEach {
            dependencies.add(it)
            addProject(it.to)
          }
      }
    }
    project.allprojects.filter { it.isDependingOnOtherProject() }.forEach { addProject(it) }

    val graph = mutGraph().setDirected(true).graphAttrs().add(GraphAttr.dpi(100))
    graph.graphAttrs().add(Label.of(project.name).locate(Label.Location.TOP), Font.size(DEFAULT_FONT_SIZE))
    graph.nodeAttrs().add(Style.FILLED)
    projects.forEach { addNode(it, dependencies, graph) }
    rankRootProjects(graph, projects, dependencies)
    addDependencies(dependencies, graph)

    return projectGenerator.graph(graph)
  }

  private fun addNode(project: Project, dependencies: List<ProjectDependencyContainer>, graph: MutableGraph) {
    val node = mutNode(project.path)

    if (dependencies.none { it.to == project }) {
      node.add(Shape.RECTANGLE)
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

  @Suppress("Detekt.SpreadOperator") private fun rankRootProjects(graph: MutableGraph, projects: MutableSet<Project>, dependencies: List<ProjectDependencyContainer>) {
    graph.add(
      graph()
        .graphAttr()
        .with(Rank.inSubgraph(RankType.SAME))
        .with(*projects.filter { project -> dependencies.none { it.to == project } }.map { mutNode(it.path) }.toTypedArray())
    )
  }

  private fun addDependencies(dependencies: MutableList<ProjectDependencyContainer>, graph: MutableGraph) {
    val rootNodes = graph.rootNodes().filter { it.links().isEmpty() }
    dependencies
      .filterNot { (from, to, _) -> from == to }
      .distinctBy { (from, to, _) -> from to to }
      .forEach { (from, to, isImplementation) ->
        val fromNode = rootNodes.single { it.name().toString() == from.path }
        val toNode = rootNodes.single { it.name().toString() == to.path }

        if (fromNode != null && toNode != null) {
          val link = Link.to(toNode)
          graph.add(fromNode.addLink(if (isImplementation) link.with(Style.DOTTED) else link))
        }
      }
  }

  internal data class ProjectDependencyContainer(
    val from: Project,
    val to: Project,
    val isImplementation: Boolean
  )

  internal companion object {
    const val DEFAULT_FONT_SIZE = 35
  }
}
