package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.testfixtures.ProjectBuilder

internal fun buildTestProject(
  name: String,
  parent: Project? = null,
  pluginId: String? = null,
  config: Project.() -> Unit = {},
): DefaultProject = ProjectBuilder
  .builder()
  .withParent(parent)
  .withName(name)
  .build()
  .also { if (pluginId != null) it.plugins.apply(pluginId) }
  .also { it.config() }
  as DefaultProject
