package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import guru.nidi.graphviz.engine.Graphviz
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ProjectDependencyGraphGeneratorTask : DefaultTask() {
  lateinit var projectGenerator: ProjectGenerator // TODO does this need to be an input? Quick testing shows no.
  @InputFile lateinit var inputFile: File

  @OutputDirectory lateinit var outputDirectory: File

  @TaskAction fun run() {
    val graph = ProjectDependencyGraphGenerator(project, projectGenerator).generateGraph()
    File(outputDirectory, projectGenerator.outputFileNameDot).writeText(graph.toString())

    val graphviz = Graphviz.fromGraph(graph)

    projectGenerator.outputFormats.forEach {
      graphviz.render(it).toFile(File(outputDirectory, projectGenerator.outputFileName))
    }
  }
}
