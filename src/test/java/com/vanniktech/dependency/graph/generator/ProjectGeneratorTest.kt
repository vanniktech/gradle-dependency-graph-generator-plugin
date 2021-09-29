package com.vanniktech.dependency.graph.generator

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator.Companion.ALL
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import guru.nidi.graphviz.engine.Format.PNG
import guru.nidi.graphviz.engine.Format.SVG
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class ProjectGeneratorTest {
  private val generatorFoo = ProjectGenerator(name = "fooBar")

  @Test fun gradleTaskName() {
    assertThat(ALL.gradleTaskName).isEqualTo("generateProjectDependencyGraph")
    assertThat(generatorFoo.gradleTaskName).isEqualTo("generateProjectDependencyGraphFooBar")
  }

  @Test fun projectDependenciesTaskName() {
    assertThat(ALL.projectDependenciesTaskName).isEqualTo("projectDependencies")
    assertThat(generatorFoo.projectDependenciesTaskName).isEqualTo("projectDependenciesFooBar")
  }

  @Test fun outputFileName() {
    assertThat(ALL.outputFileName).isEqualTo("project-dependency-graph")
    assertThat(generatorFoo.outputFileName).isEqualTo("project-dependency-graph-foo-bar")
  }

  @Test fun outputFileNameDot() {
    assertThat(ALL.outputFileNameDot).isEqualTo("project-dependency-graph.dot")
    assertThat(generatorFoo.outputFileNameDot).isEqualTo("project-dependency-graph-foo-bar.dot")
  }

  @Test fun outputFormats() {
    assertThat(ALL.outputFormats).containsExactly(PNG, SVG)
  }
}
