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
import org.gradle.api.artifacts.ProjectDependency
import java.util.Locale.US

// Based on https://github.com/JakeWharton/SdkSearch/blob/766d612ed52cdf3af9cd0728b6afd87006746ae5/gradle/projectDependencyGraph.gradle
internal class ProjectDependencyGraphGenerator(
  private val project: Project,
  private val projectGenerator: DependencyGraphGeneratorExtension.ProjectGenerator
) {
  fun generateGraph(): MutableGraph {
    val graph = mutGraph().setDirected(true)
    graph.graphAttrs().add(Label.of(project.name).locate(Label.Location.TOP), Font.size(DEFAULT_FONT_SIZE))
    graph.nodeAttrs().add(Font.name("Times New Roman"), Style.FILLED)

    val rootProjects = project.allprojects.toMutableList()
    val projects = mutableSetOf<Project>()
    val dependencies = mutableListOf<ProjectDependencyContainer>()

    addProjects(projects, rootProjects, dependencies, graph)
    rankRootProjects(graph, projects, rootProjects)
    addDependencies(dependencies, graph)

    return projectGenerator.graph(graph)
  }

  @Suppress("Detekt.ComplexMethod") private fun addProjects(projects: MutableSet<Project>, rootProjects: MutableList<Project>, dependencies: MutableList<ProjectDependencyContainer>, graph: MutableGraph) {
    project.allprojects
        .filter { projectGenerator.includeProject(it) }
        .flatMap { project -> project.configurations.map { project to it } }
        .flatMap { (project, configuration) ->
          configuration.dependencies
              .withType(ProjectDependency::class.java)
              .map { it.dependencyProject }
              .flatMap { projectDependency ->
                projects.add(project)
                projects.add(projectDependency)

                rootProjects.remove(projectDependency)
                dependencies.add(ProjectDependencyContainer(project, projectDependency, configuration.name.toLowerCase(US).endsWith("implementation")))
                listOf(project, projectDependency)
              }
        }
        .forEach { project ->
          val node = mutNode(project.path)

          if (rootProjects.contains(project)) {
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
  }

  @Suppress("Detekt.SpreadOperator") private fun rankRootProjects(graph: MutableGraph, projects: MutableSet<Project>, rootProjects: MutableList<Project>) {
    graph.add(graph()
        .graphAttr()
        .with(Rank.SAME)
        .with(*projects.filter { rootProjects.contains(it) }.map { mutNode(it.path) }.toTypedArray()))
  }

  private fun addDependencies(dependencies: MutableList<ProjectDependencyContainer>, graph: MutableGraph) {
    dependencies
        .filterNot { (from, to, _) -> !from.isCommonsProject() && to.isCommonsProject() }
        .distinctBy { (from, to, _) -> from to to }
        .forEach { (from, to, isImplementation) ->
          val fromNode = graph.nodes().find { it.name().toString() == from.path }
          val toNode = graph.nodes().find { it.name().toString() == to.path }

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
