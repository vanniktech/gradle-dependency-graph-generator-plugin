package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

@CacheableTask
open class ProjectDependenciesTask : DefaultTask() {
  @get:Nested
  lateinit var projectGenerator: ProjectGenerator

  @TaskAction
  fun run() {
    project.getProjectDependencies(projectGenerator).first.minus(project)
      .map { projectGenerator.projectLabel(it) }.sorted()
      .forEach(::println)
  }
}
