package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project as GradleProject
import org.gradle.api.artifacts.ResolvedDependency as GradleResolvedDependency

internal fun GradleProject.wrapped() = DependencyContainer.Project(this)
internal fun GradleResolvedDependency.wrapped() = DependencyContainer.ResolvedDependency(this)

sealed interface DependencyContainer {

  data class Project(
    internal val project: GradleProject,
  ) : DependencyContainer, GradleProject by project

  data class ResolvedDependency(
    internal val resolvedDependency: GradleResolvedDependency,
  ) : DependencyContainer, GradleResolvedDependency by resolvedDependency
}
