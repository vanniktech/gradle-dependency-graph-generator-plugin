package com.vanniktech.dependency.graph.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

open class DependencyGraphGeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("dependencyGraphGenerator", DependencyGraphGeneratorExtension::class.java, project)
    val group = "reporting"

    extension.generators.all { generator ->
      project.tasks.register(generator.gradleTaskName, DependencyGraphGeneratorTask::class.java) {
        it.generator = generator
        it.group = group
        it.description = "Generates a dependency graph${generator.name.nonEmptyPrepend(" for ")}"
        it.outputDirectory = File(project.buildDir, "reports/dependency-graph/")
      }
    }

    extension.projectGenerators.all { projectGenerator ->
      project.tasks.register(projectGenerator.gradleTaskName, ProjectDependencyGraphGeneratorTask::class.java) {
        it.projectGenerator = projectGenerator
        it.group = group
        it.description = "Generates a project dependency graph${projectGenerator.name.nonEmptyPrepend(" for ")}"
        it.outputDirectory = File(project.buildDir, "reports/project-dependency-graph/")
      }
      project.tasks.register(projectGenerator.projectDependenciesTaskName, ProjectDependenciesTask::class.java) {
        it.projectGenerator = projectGenerator
        it.group = group
        it.description = "Displays all project dependencies declared in project${projectGenerator.name.nonEmptyPrepend(" for ")}"
      }
    }
  }
}
