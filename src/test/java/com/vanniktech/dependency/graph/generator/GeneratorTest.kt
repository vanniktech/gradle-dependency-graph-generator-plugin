package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import guru.nidi.graphviz.engine.Format.PNG
import guru.nidi.graphviz.engine.Format.SVG
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class GeneratorTest {
  private val generatorFoo = Generator(name = "fooBar")

  @Test fun gradleTaskName() {
    assertThat(ALL.gradleTaskName).isEqualTo("generateDependencyGraph")
    assertThat(generatorFoo.gradleTaskName).isEqualTo("generateDependencyGraphFooBar")
  }

  @Test fun outputFileName() {
    assertThat(ALL.outputFileName).isEqualTo("dependency-graph")
    assertThat(generatorFoo.outputFileName).isEqualTo("dependency-graph-foo-bar")
  }

  @Test fun outputFileNameDot() {
    assertThat(ALL.outputFileNameDot).isEqualTo("dependency-graph.dot")
    assertThat(generatorFoo.outputFileNameDot).isEqualTo("dependency-graph-foo-bar.dot")
  }

  @Test fun outputFormats() {
    assertThat(ALL.outputFormats).containsExactly(PNG, SVG)
  }
}
