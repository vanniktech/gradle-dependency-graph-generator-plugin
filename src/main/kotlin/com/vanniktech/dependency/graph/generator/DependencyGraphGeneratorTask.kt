package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.OutputStream

open class DependencyGraphGeneratorTask : DefaultTask() {
  lateinit var generator: Generator // TODO does this need to be an input? Quick testing shows no.
  @InputFile lateinit var inputFile: File

  @OutputFile lateinit var outputFileDot: File
  @OutputFile lateinit var outputFileImage: File

  @TaskAction fun run() {
    val result = project.exec {
      it.standardOutput = NullOutputStream // Don't print the output.
      if (Os.isFamily(Os.FAMILY_WINDOWS)) {
        it.executable = "cmd"
      } else {
        it.executable = "command"
      }
      it.args("-v", "dot")
      it.isIgnoreExitValue = true
    }

    if (result.exitValue != 0) {
      val message = when {
        Os.isFamily(Os.FAMILY_MAC) -> "Please install via: brew install graphviz"
        Os.isFamily(Os.FAMILY_UNIX) -> "Please install via: sudo apt-get install graphviz"
        Os.isFamily(Os.FAMILY_WINDOWS) -> "Please install via: choco install graphviz"
        else -> "Please find a way to install it."
      }

      throw GradleException("This task requires dot from graphviz. $message")
    }

    outputFileDot.writeText(DotGenerator(project, generator).generateContent())

    project.exec {
      it.executable = "dot"
      it.args("-o", outputFileImage.toString(), "-Tpng", outputFileDot.toString())
    }
  }

  internal object NullOutputStream : OutputStream() {
    override fun write(b: Int) {
      // No-op.
    }
  }
}
