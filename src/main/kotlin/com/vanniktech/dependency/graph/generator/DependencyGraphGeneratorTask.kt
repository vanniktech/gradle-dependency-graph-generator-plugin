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

  @OutputFile lateinit var dotOutputFile: File
  @OutputFile lateinit var imageOutputFile: File

  @TaskAction fun run() {
    val result = project.exec {
      it.standardOutput = NullOutputStream // Don't print the output.
      it.executable = "command"
      it.args("-v", "dot")
      it.isIgnoreExitValue = true
    }

    if (result.exitValue != 0) {
      val message = when {
        Os.isFamily(Os.FAMILY_MAC) -> "Please install via: brew install graphviz"
        Os.isFamily(Os.FAMILY_UNIX) -> "Please install via: sudo apt-get install graphviz"
        else -> "Please find a way to install it."
      }

      throw GradleException("This task requires dot from graphviz. $message")
    }

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

  internal object NullOutputStream : OutputStream() {
    override fun write(b: Int) {
      // No-op.
    }
  }
}
