package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import guru.nidi.graphviz.engine.Format.PNG
import guru.nidi.graphviz.engine.Graphviz
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DependencyGraphGeneratorTask : DefaultTask() {
  lateinit var generator: Generator // TODO does this need to be an input? Quick testing shows no.
  @InputFile lateinit var inputFile: File

  @OutputFile lateinit var outputFileDot: File
  @OutputFile lateinit var outputFileImage: File

  @TaskAction fun run() {
    val graph = DotGenerator(project, generator).generateGraph()
    outputFileDot.writeText(graph.toString())
    Graphviz.fromGraph(graph).render(PNG).toFile(outputFileImage)
  }
}
