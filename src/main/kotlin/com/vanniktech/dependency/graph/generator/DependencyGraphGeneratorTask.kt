package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DependencyGraphGeneratorTask : DefaultTask() {
  lateinit var generator: Generator // TODO does this need to be an input? Quick testing shows no.
  @InputFile lateinit var inputFile: File

  @OutputFile lateinit var dotOutputFile: File
  @OutputFile lateinit var imageOutputFile: File

  @TaskAction fun run() {
    dotOutputFile.writeText(DotGenerator(project, generator).generateContent())

    project.exec {
      it.workingDir = dotOutputFile.parentFile
      it.executable = "dot"
      it.args("-Tpng", "-O", dotOutputFile.name)
    }

    project.copy {
      it.from(File(dotOutputFile.parentFile, "${dotOutputFile.name}.png").toString())
      it.into(project.projectDir.toString())
      it.rename("\\.dot", "")
    }
  }
}
