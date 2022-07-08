package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import guru.nidi.graphviz.engine.Graphviz
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask open class ProjectDependencyGraphGeneratorTask : DefaultTask() {
  @get:Nested lateinit var projectGenerator: ProjectGenerator

  @OutputDirectory lateinit var outputDirectory: File

  @get:Internal val graph by lazy {
    ProjectDependencyGraphGenerator(project, projectGenerator).generateGraph()
  }

  @get:Input val dotFormatGraph by lazy {
    graph.toString()
  }

  @TaskAction fun run() {
    val dot = File(outputDirectory, projectGenerator.outputFileNameDot)
    dot.writeText(graph.toString())

    val graphviz = Graphviz.fromGraph(graph).run(projectGenerator.graphviz)

    val renders = projectGenerator.outputFormats.map {
      graphviz.render(it).toFile(File(outputDirectory, projectGenerator.outputFileName))
    }

    listOf(dot).plus(renders).distinct().forEach { logger.lifecycle(it.absolutePath) }
  }
}
