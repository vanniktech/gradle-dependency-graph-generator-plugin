package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class DependencyGraphGeneratorExtensionTest {
  @Test fun defaults() {
    val defaults = DependencyGraphGeneratorExtension()
    assertThat(defaults.generators).containsExactly(Generator.ALL)
    assertThat(defaults.projectGenerators).containsExactly(ProjectGenerator.ALL)
  }
}
