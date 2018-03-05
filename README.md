# gradle-dependency-graph-generator-plugin

[![Build Status](https://travis-ci.org/vanniktech/gradle-dependency-graph-generator-plugin.svg?branch=master)](https://travis-ci.org/vanniktech/gradle-dependency-graph-generator-plugin?branch=master)
[![Codecov](https://codecov.io/github/vanniktech/gradle-dependency-graph-generator-plugin/coverage.svg?branch=master)](https://codecov.io/github/vanniktech/gradle-dependency-graph-generator-plugin?branch=master)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Gradle plugin that lets you visualize your dependencies in a graph.

# Set up

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath "com.vanniktech:gradle-dependency-graph-generator-plugin:0.1.0"
  }
}

apply plugin: "com.vanniktech.dependency.graph.generator"
```

Note that this plugin can be applied at the root of the project or at a specific project. Both cases will just work.

This plugin is using the `dot` command line tool for generating the graphs hence you need to install it. Mac users can use `brew install graphviz`. Ubuntu users can `sudo apt-get install graphviz`.

Information: [This plugin is also available on Gradle plugins](https://plugins.gradle.org/plugin/com.vanniktech.dependency.graph.generator)

### Snapshot

```groovy
buildscript {
  repositories {
    mavenCentral()
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
  }
  dependencies {
    classpath "com.vanniktech:gradle-dependency-graph-generator-plugin:0.2.0-SNAPSHOT"
  }
}

apply plugin: "com.vanniktech.dependency.graph.generator"
```

## Usage

By default this plugin will generate a `generateDependencyGraph` task that can be used to generate a dependency graph that could look like this. This graph was generated from my [chess clock app](https://play.google.com/store/apps/details?id=com.vanniktech.chessclock).

![Example graph.](example.png)

There are extension points to be able to generate graphs which only include some dependencies and their transitive ones. The trick is to hook a [Generator](./src/main/kotlin/com/vanniktech/dependency/graph/generator/DependencyGraphGeneratorExtension.kt) in over the `dependencyGraphGenerator` extension. Note that this is extremely experimental and will likely change between releases. It's still fun though.

### Generator Example

We only want to show which Firebase libraries we're using and give them the typical Firebase orange.

```groovy
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorPlugin
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import com.vanniktech.dependency.graph.generator.dot.GraphFormattingOptions
import com.vanniktech.dependency.graph.generator.dot.Color
import com.vanniktech.dependency.graph.generator.dot.Shape
import com.vanniktech.dependency.graph.generator.dot.Style

plugins.apply(DependencyGraphGeneratorPlugin)

def firebaseGenerator = new Generator(
  "firebaseLibraries", // Suffix for our Gradle task.
  "", // Root suffix that we don't want in this case.
  { dependency -> dependency.getModuleGroup().startsWith("com.google.firebase") }, // Only want Firebase.
  { dependency -> true }, // Include transitive dependencies.
  { dependency -> new GraphFormattingOptions(Shape.BOX, Style.FILLED, Color.fromRgb(255, 203, 43)) }, // Give them some color.
)

dependencyGraphGenerator {
  generators = [ firebaseGenerator ]
}
```

This will generate a new task `generateDependencyGraphFirebaseLibraries` which when run will yield this graph:

![Example Firebase graph.](example-firebase.png)

Note that when using the `dependencyGraphGenerator` extension with custom generators you lose the default one, to add it back simply use the `Generator.ALL` instance:

```groovy
dependencyGraphGenerator {
  generators = [ Generator.ALL, firebaseGenerator ]
}
```
# License

Copyright (C) 2018 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0
