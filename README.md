# gradle-dependency-graph-generator-plugin

Gradle plugin that lets you visualize your dependencies in a graph.

# Set up

[Gradle 3.3](https://docs.gradle.org/3.3/release-notes.html) or higher is required.

```gradle
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath "com.vanniktech:gradle-dependency-graph-generator-plugin:0.8.0"
  }
}

apply plugin: "com.vanniktech.dependency.graph.generator"
```

Note that this plugin can be applied at the root of the project or at a specific project. Both cases will just work.

### Snapshot

```gradle
buildscript {
  repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
  }
  dependencies {
    classpath "com.vanniktech:gradle-dependency-graph-generator-plugin:0.9.0-SNAPSHOT"
  }
}

apply plugin: "com.vanniktech.dependency.graph.generator"
```

## Usage

By default, this plugin provides two reporting tasks:

| `generateDependencyGraph`                                                       | `generateProjectDependencyGraph`                                                                     |
|:--------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------|
| ![Dependency Graph](art/dependency-graph.svg)                                   | ![Project Dependency Graph](art/project-dependency-graph.svg)                                        |
| Generates a **complete** dependency graph in `build/reports/dependency-graph/`  | Generates a dependency graph of the **project modules** in `build/reports/project-dependency-graph/` |

_Source project: [vanniktech/Emoji](https://github.com/vanniktech/Emoji)_

The graphs are generated in `.png`, `.svg` & `.dot` format.

There are extension points to be able to generate graphs which only include some dependencies and their transitive ones. The trick is to hook a [Generator](./src/main/kotlin/com/vanniktech/dependency/graph/generator/DependencyGraphGeneratorExtension.kt) in over the `dependencyGraphGenerator` extension. Note that this is extremely experimental and will likely change between releases. It's still fun though.

### Generator Example

We only want to show which Jetbrains libraries we're using.

```groovy
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorPlugin
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Style

plugins.apply(DependencyGraphGeneratorPlugin)

dependencyGraphGenerator {
  generators {
    jetbrainsLibraries {
      include = { dependency -> dependency.getModuleGroup().startsWith("org.jetbrains") } // Only want Jetbrains.
      children = { true } // Include transitive dependencies.
      dependencyNode = { node, dependency -> node.add(Style.FILLED, Color.rgb("#AF1DF5")) } // Give them some color.
    }
  }
}
```

<details>
<summary>The same can be done using Kotlin</summary>

```kotlin
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorPlugin
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Style

plugins.apply(DependencyGraphGeneratorPlugin::class.java)

configure<DependencyGraphGeneratorExtension> {
  generators.create("jetbrainsLibraries") {
    include = { dependency -> dependency.moduleGroup.startsWith("org.jetbrains") } // Only want Jetbrains.
    children = { true } // Include transitive dependencies.
    dependencyNode = { node, dependency -> node.add(Style.FILLED, Color.rgb("#AF1DF5")) } // Give them some color.
  }
}
```

</details>

This will generate a new task `generateDependencyGraphJetbrainsLibraries` which when run will yield this graph:

![Example Jetbrains graph](art/dependency-graph-jetbrains-libraries.svg)


# License

Copyright (C) 2018 Vanniktech - Niklas Baudy

Licensed under the Apache License, Version 2.0
