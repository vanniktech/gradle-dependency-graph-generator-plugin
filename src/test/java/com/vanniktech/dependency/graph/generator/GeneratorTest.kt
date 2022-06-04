package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import guru.nidi.graphviz.engine.Format.PNG
import guru.nidi.graphviz.engine.Format.SVG
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneratorTest {
  private val generatorFoo = Generator(name = "fooBar")

  @Test fun gradleTaskName() {
    assertEquals("generateDependencyGraph", ALL.gradleTaskName)
    assertEquals("generateDependencyGraphFooBar", generatorFoo.gradleTaskName)
  }

  @Test fun outputFileName() {
    assertEquals("dependency-graph", ALL.outputFileName)
    assertEquals("dependency-graph-foo-bar", generatorFoo.outputFileName)
  }

  @Test fun outputFileNameDot() {
    assertEquals("dependency-graph.dot", ALL.outputFileNameDot)
    assertEquals("dependency-graph-foo-bar.dot", generatorFoo.outputFileNameDot)
  }

  @Test fun outputFormats() {
    assertEquals(listOf(PNG, SVG), ALL.outputFormats)
  }
}
