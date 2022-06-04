package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator.Companion.ALL
import guru.nidi.graphviz.engine.Format.PNG
import guru.nidi.graphviz.engine.Format.SVG
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectGeneratorTest {
  private val generatorFoo = ProjectGenerator(name = "fooBar")

  @Test fun gradleTaskName() {
    assertEquals("generateProjectDependencyGraph", ALL.gradleTaskName)
    assertEquals("generateProjectDependencyGraphFooBar", generatorFoo.gradleTaskName)
  }

  @Test fun outputFileName() {
    assertEquals("project-dependency-graph", ALL.outputFileName)
    assertEquals("project-dependency-graph-foo-bar", generatorFoo.outputFileName)
  }

  @Test fun outputFileNameDot() {
    assertEquals("project-dependency-graph.dot", ALL.outputFileNameDot)
    assertEquals("project-dependency-graph-foo-bar.dot", generatorFoo.outputFileNameDot)
  }

  @Test fun outputFormats() {
    assertEquals(listOf(PNG, SVG), ALL.outputFormats)
  }
}
