package com.vanniktech.dependency.graph.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

open class DependencyGraphGeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("dependencyGraphGenerator", DependencyGraphGeneratorExtension::class.java, project)

    extension.generators.all { generator ->
      project.tasks.register(generator.gradleTaskName, DependencyGraphGeneratorTask::class.java) {
        it.generator = generator
        it.group = "reporting"
        it.description = "Generates a dependency graph${generator.name.nonEmptyPrepend(" for ")}"
        it.outputDirectory.set(project.layout.buildDirectory.dir("reports/dependency-graph/"))
      }
    }

    extension.projectGenerators.all { projectGenerator ->
      project.tasks.register(projectGenerator.gradleTaskName, ProjectDependencyGraphGeneratorTask::class.java) {
        it.projectGenerator = projectGenerator
        it.group = "reporting"
        it.description = "Generates a project dependency graph${projectGenerator.name.nonEmptyPrepend(" for ")}"
        it.outputDirectory.set(project.layout.buildDirectory.dir("reports/project-dependency-graph/"))
      }
    }
  }
}
