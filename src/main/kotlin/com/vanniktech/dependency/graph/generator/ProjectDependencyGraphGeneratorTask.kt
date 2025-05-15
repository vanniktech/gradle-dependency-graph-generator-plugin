package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import guru.nidi.graphviz.engine.Graphviz
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@CacheableTask
open class ProjectDependencyGraphGeneratorTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
  @get:Nested lateinit var projectGenerator: ProjectGenerator

  @OutputDirectory val outputDirectory: DirectoryProperty = objects.directoryProperty()

  @get:Internal val graph by lazy {
    ProjectDependencyGraphGenerator(project, projectGenerator).generateGraph()
  }

  @get:Input val dotFormatGraph by lazy {
    graph.toString()
  }

  @TaskAction fun run() {
    val outputDirectory = outputDirectory.get().asFile
    val dot = File(outputDirectory, projectGenerator.outputFileNameDot)
    dot.writeText(graph.toString())

    val graphviz = Graphviz.fromGraph(graph).run(projectGenerator.graphviz)

    val renders =
      projectGenerator.outputFormats.map {
        graphviz.render(it).toFile(File(outputDirectory, projectGenerator.outputFileName))
      }

    listOf(dot).plus(renders).distinct().forEach { logger.lifecycle(it.absolutePath) }
  }
}
