package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import guru.nidi.graphviz.engine.Graphviz
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DependencyGraphGeneratorTask : DefaultTask() {
  lateinit var generator: Generator // TODO does this need to be an input? Quick testing shows no.
  @InputFile lateinit var inputFile: File

  @OutputDirectory lateinit var outputDirectory: File

  @TaskAction fun run() {
    val graph = DependencyGraphGenerator(project, generator).generateGraph()
    File(outputDirectory, generator.outputFileNameDot).writeText(graph.toString())

    val graphviz = Graphviz.fromGraph(graph)

    generator.outputFormats.forEach {
      graphviz.render(it).toFile(File(outputDirectory, generator.outputFileName))
    }
  }
}
