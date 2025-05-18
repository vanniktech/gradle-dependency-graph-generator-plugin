package com.vanniktech.dependency.graph.generator

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

internal fun buildTestProject(
  name: String,
  parent: Project? = null,
  pluginId: String? = null,
  config: Project.() -> Unit = {},
): Project = ProjectBuilder
  .builder()
  .withParent(parent)
  .withName(name)
  .build()
  .also { if (pluginId != null) it.plugins.apply(pluginId) }
  .also { it.config() }
