package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project as GradleProject
import org.gradle.api.artifacts.ResolvedDependency as GradleResolvedDependency

sealed interface DependencyContainer {

  companion object {
    internal operator fun invoke(project: GradleProject) = Project(project)
    internal operator fun invoke(dependency: GradleResolvedDependency) = ResolvedDependency(dependency)
  }

  data class Project(
    internal val project: GradleProject,
  ) : DependencyContainer, GradleProject by project

  data class ResolvedDependency(
    internal val resolvedDependency: GradleResolvedDependency,
  ) : DependencyContainer, GradleResolvedDependency by resolvedDependency
}
