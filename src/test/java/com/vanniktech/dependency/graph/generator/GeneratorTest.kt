package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class GeneratorTest {
  private val generatorFoo = Generator(name = "fooBar", rootSuffix = "bar")

  @Test fun gradleTaskName() {
    assertThat(ALL.gradleTaskName).isEqualTo("generateDependencyGraph")
    assertThat(generatorFoo.gradleTaskName).isEqualTo("generateDependencyGraphFooBar")
  }

  @Test fun outputFileNameDot() {
    assertThat(ALL.outputFileNameDot).isEqualTo("dependency-graph.dot")
    assertThat(generatorFoo.outputFileNameDot).isEqualTo("dependency-graph-foo-bar.dot")
  }

  @Test fun outputFileNamePng() {
    assertThat(ALL.outputFileNamePng).isEqualTo("dependency-graph.png")
    assertThat(generatorFoo.outputFileNamePng).isEqualTo("dependency-graph-foo-bar.png")
  }
}
