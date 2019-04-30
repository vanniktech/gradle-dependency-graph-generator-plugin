package com.vanniktech.dependency.graph.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion
import java.io.File

open class DependencyGraphGeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("dependencyGraphGenerator", DependencyGraphGeneratorExtension::class.java)

    if (GradleVersion.current() >= GradleVersion.version("4.9")) {
      extension.generators.forEach {
        project.tasks.register(it.gradleTaskName, DependencyGraphGeneratorTask::class.java, it.configureTask(project))
      }

      extension.projectGenerators.forEach {
        project.tasks.register(it.gradleTaskName, ProjectDependencyGraphGeneratorTask::class.java, it.configureTask(project))
      }
    } else {
      project.afterEvaluate { _ ->
        extension.generators.forEach {
          project.tasks.create(it.gradleTaskName, DependencyGraphGeneratorTask::class.java, it.configureTask(project))
        }

        extension.projectGenerators.forEach {
          project.tasks.create(it.gradleTaskName, ProjectDependencyGraphGeneratorTask::class.java, it.configureTask(project))
        }
      }
    }
  }

  private fun DependencyGraphGeneratorExtension.Generator.configureTask(project: Project): (DependencyGraphGeneratorTask) -> Unit {
    val name = name.nonEmptyPrepend(" for ")

    return {
      it.generator = this
      it.group = "reporting"
      it.description = "Generates a dependency graph$name"
      it.outputDirectory = File(project.buildDir, "reports/dependency-graph/")
    }
  }

  private fun DependencyGraphGeneratorExtension.ProjectGenerator.configureTask(project: Project): (ProjectDependencyGraphGeneratorTask) -> Unit {
    val name = name.nonEmptyPrepend(" for ")

    return {
      it.projectGenerator = this
      it.group = "reporting"
      it.description = "Generates a project dependency graph$name"
      it.outputDirectory = File(project.buildDir, "reports/project-dependency-graph/")
    }
  }
}
