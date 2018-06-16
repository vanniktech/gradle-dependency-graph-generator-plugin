package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import guru.nidi.graphviz.engine.Format.PNG
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class DependencyGraphGeneratorExtensionTest {
  @Test fun defaults() {
    val defaults = DependencyGraphGeneratorExtension()
    assertThat(defaults.generators).containsExactly(ALL)
  }
}
