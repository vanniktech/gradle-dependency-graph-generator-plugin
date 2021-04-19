package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import guru.nidi.graphviz.engine.Graphviz
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask open class DependencyGraphGeneratorTask : DefaultTask() {
  @get:Nested lateinit var generator: Generator

  @OutputDirectory lateinit var outputDirectory: File

  @get:Internal val graph by lazy {
    DependencyGraphGenerator(project, generator).generateGraph()
  }

  @get:Input val dotFormatGraph by lazy {
    graph.toString()
  }

  @TaskAction fun run() {
    File(outputDirectory, generator.outputFileNameDot).writeText(graph.toString())

    val graphviz = Graphviz.fromGraph(graph)

    generator.outputFormats.forEach {
      graphviz.render(it).toFile(File(outputDirectory, generator.outputFileName))
    }
  }
}
