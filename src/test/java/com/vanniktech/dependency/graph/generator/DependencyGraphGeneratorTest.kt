package com.vanniktech.dependency.graph.generator

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Label.Justification.LEFT
import guru.nidi.graphviz.attribute.Label.Location.TOP
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.MutableNode
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Random

class DependencyGraphGeneratorTest {
  private lateinit var singleEmpty: Project
  private lateinit var singleProject: Project
  private lateinit var rxjavaProject: Project
  private lateinit var multiProject: Project
  private lateinit var androidProject: DefaultProject // We always need to call evaluate() for Android Projects.
  private lateinit var androidProjectExtension: AppExtension

  @Before @Suppress("Detekt.LongMethod") fun setUp() {
    singleEmpty = ProjectBuilder.builder().withName("singleempty").build()
    singleEmpty.plugins.apply(JavaLibraryPlugin::class.java)
    singleEmpty.repositories.run { add(mavenCentral()) }

    singleProject = ProjectBuilder.builder().withName("single").build()
    singleProject.plugins.apply(JavaLibraryPlugin::class.java)
    singleProject.repositories.run { add(mavenCentral()) }
    singleProject.dependencies.add("api", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")
    singleProject.dependencies.add("implementation", "io.reactivex.rxjava2:rxjava:2.1.10")

    rxjavaProject = ProjectBuilder.builder().withName("rxjava").build()
    rxjavaProject.plugins.apply(JavaLibraryPlugin::class.java)
    rxjavaProject.repositories.run { add(mavenCentral()) }
    rxjavaProject.dependencies.add("implementation", "io.reactivex.rxjava2:rxjava:2.1.10")

    multiProject = ProjectBuilder.builder().withName("multi").build()

    val multiProject1 = ProjectBuilder.builder().withParent(multiProject).withName("multi1").build()
    multiProject1.plugins.apply(JavaLibraryPlugin::class.java)
    multiProject1.repositories.run { add(mavenCentral()) }
    multiProject1.dependencies.add("api", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")
    multiProject1.dependencies.add("implementation", "io.reactivex.rxjava2:rxjava:2.1.10")

    val multiProject2 = ProjectBuilder.builder().withParent(multiProject).withName("multi2").build()
    multiProject2.plugins.apply(JavaLibraryPlugin::class.java)
    multiProject2.repositories.run { add(mavenCentral()) }
    multiProject2.dependencies.add("implementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    multiProject2.dependencies.add("implementation", "io.reactivex.rxjava2:rxandroid:2.0.2")

    androidProject = ProjectBuilder.builder().withName("android").build() as DefaultProject
    androidProject.plugins.apply(AppPlugin::class.java)
    androidProject.repositories.run {
      add(mavenCentral())
      add(google())
    }

    androidProjectExtension = androidProject.extensions.getByType(AppExtension::class.java)
    androidProjectExtension.compileSdkVersion(27)
    val manifestFile = File(androidProject.projectDir, "src/main/AndroidManifest.xml")
    manifestFile.parentFile.mkdirs()
    manifestFile.writeText("""<manifest package="com.foo.bar"/>""")
  }

  @Test fun singleProjectAllNoTestDependencies() {
    singleEmpty.dependencies.add("testImplementation", "junit:junit:4.12")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "singleempty" ["label"="singleempty","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "singleempty"
        }
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleEmpty, ALL).generateGraph().toString()
    )
  }

  @Test fun singleProjectEmptyAllNoProjects() {
    assertEquals(
      """
        digraph "G" {
        node ["fontname"="Times New Roman"]
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleEmpty, ALL.copy(includeProject = { false })).generateGraph().toString()
    )
  }

  @Test fun singleProjectEmptyAll() {
    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "singleempty" ["label"="singleempty","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "singleempty"
        }
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleEmpty, ALL).generateGraph().toString()
    )
  }

  @Test fun singleProjectEmptyAllMutateGraph() {
    assertEquals(
      """
        graph "G" {
        node ["fontname"="Times New Roman"]
        "singleempty" ["label"="singleempty","shape"="rectangle"]
        {
        graph ["rank"="same"]
        "singleempty"
        }
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleEmpty, ALL.copy(graph = { it.setDirected(false) })).generateGraph().toString()
    )
  }

  @Test fun singleProjectEmptyAllHeader() {
    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        graph ["label"="my custom header","labeljust"="l","labelloc"="t"]
        node ["fontname"="Times New Roman"]
        "singleempty" ["label"="singleempty","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "singleempty"
        }
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleEmpty, ALL.copy(label = Label.of("my custom header").locate(TOP).justify(LEFT))).generateGraph().toString()
    )
  }

  @Test fun singleProjectEmptyAllRootFormatted() {
    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "singleempty" ["label"="singleempty","shape"="egg","style"="dotted","color"="#ff0099"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "singleempty"
        }
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleEmpty, ALL.copy(projectNode = { node, _ -> node.add(Shape.EGG, Style.DOTTED, Color.rgb("ff0099")) })).generateGraph().toString()
    )
  }

  @Test fun singleProjectAll() {
    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "single" ["label"="single","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "single" -> "ioreactivexrxjava2rxjava"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleProject, ALL).generateGraph().toString()
    )
  }

  @Test fun singleProjectAllDependencyFormattingOptions() {
    // Generate a color for each dependency.
    val dependencyNode: (MutableNode, ResolvedDependency) -> MutableNode = { node, project ->
      val random = Random(project.name.hashCode().toLong())
      node.add(
        Style.FILLED, Color.hsv(random.nextDouble(), random.nextDouble(), random.nextDouble())
      )
    }

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "single" ["label"="single","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle","style"="filled","color"="0.833904937402929 0.4047932090555708 0.5440948801677342"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle","style"="filled","color"="0.44658757938141413 0.25639393293458856 0.2315484830185478"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle","style"="filled","color"="0.37947834890750454 0.21008099121996504 0.6969226044909884"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle","style"="filled","color"="0.3100028267238165 0.7876064423347447 0.6784992909440705"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "single" -> "ioreactivexrxjava2rxjava"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleProject, ALL.copy(dependencyNode = dependencyNode)).generateGraph().toString()
    )
  }

  @Test fun singleProjectNoChildren() {
    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "single" ["label"="single","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "single" -> "ioreactivexrxjava2rxjava"
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleProject, ALL.copy(children = { false })).generateGraph().toString()
    )
  }

  @Test fun singleProjectFilterRxJavaOut() {
    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "single" ["label"="single","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleProject, ALL.copy(include = { it.moduleGroup != "io.reactivex.rxjava2" })).generateGraph().toString()
    )
  }

  @Test fun recursiveDependencies() {
    singleEmpty.dependencies.add("implementation", "org.apache.xmlgraphics:batik-gvt:1.7")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "singleempty" ["label"="singleempty","shape"="rectangle"]
        "orgapachexmlgraphicsbatikgvt" ["label"="batik-gvt","shape"="rectangle"]
        "orgapachexmlgraphicsbatikbridge" ["label"="batik-bridge","shape"="rectangle"]
        "orgapachexmlgraphicsbatikscript" ["label"="batik-script","shape"="rectangle"]
        "orgapachexmlgraphicsbatikawtutil" ["label"="batik-awt-util","shape"="rectangle"]
        "orgapachexmlgraphicsbatikutil" ["label"="batik-util","shape"="rectangle"]
        "xmlapisxmlapis" ["label"="xml-apis","shape"="rectangle"]
        "orgapachexmlgraphicsbatiksvgdom" ["label"="batik-svg-dom","shape"="rectangle"]
        "orgapachexmlgraphicsbatikanim" ["label"="batik-anim","shape"="rectangle"]
        "orgapachexmlgraphicsbatikparser" ["label"="batik-parser","shape"="rectangle"]
        "orgapachexmlgraphicsbatikxml" ["label"="batik-xml","shape"="rectangle"]
        "orgapachexmlgraphicsbatikdom" ["label"="batik-dom","shape"="rectangle"]
        "orgapachexmlgraphicsbatikcss" ["label"="batik-css","shape"="rectangle"]
        "orgapachexmlgraphicsbatikext" ["label"="batik-ext","shape"="rectangle"]
        "xmlapisxmlapisext" ["label"="xml-apis-ext","shape"="rectangle"]
        "xalanxalan" ["label"="xalan","shape"="rectangle"]
        "orgapachexmlgraphicsbatikjs" ["label"="batik-js","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "singleempty"
        }
        "singleempty" -> "orgapachexmlgraphicsbatikgvt"
        "orgapachexmlgraphicsbatikgvt" -> "orgapachexmlgraphicsbatikawtutil"
        "orgapachexmlgraphicsbatikgvt" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatikgvt" -> "xmlapisxmlapis"
        "orgapachexmlgraphicsbatikgvt" -> "orgapachexmlgraphicsbatikbridge"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikgvt"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatiksvgdom"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikanim"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikparser"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikawtutil"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikdom"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikcss"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikxml"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikext"
        "orgapachexmlgraphicsbatikbridge" -> "xalanxalan"
        "orgapachexmlgraphicsbatikbridge" -> "xmlapisxmlapis"
        "orgapachexmlgraphicsbatikbridge" -> "xmlapisxmlapisext"
        "orgapachexmlgraphicsbatikbridge" -> "orgapachexmlgraphicsbatikscript"
        "orgapachexmlgraphicsbatikscript" -> "orgapachexmlgraphicsbatikbridge"
        "orgapachexmlgraphicsbatikscript" -> "orgapachexmlgraphicsbatiksvgdom"
        "orgapachexmlgraphicsbatikscript" -> "orgapachexmlgraphicsbatikdom"
        "orgapachexmlgraphicsbatikscript" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatikscript" -> "orgapachexmlgraphicsbatikext"
        "orgapachexmlgraphicsbatikscript" -> "orgapachexmlgraphicsbatikjs"
        "orgapachexmlgraphicsbatikscript" -> "xmlapisxmlapis"
        "orgapachexmlgraphicsbatikawtutil" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatiksvgdom" -> "orgapachexmlgraphicsbatikparser"
        "orgapachexmlgraphicsbatiksvgdom" -> "orgapachexmlgraphicsbatikawtutil"
        "orgapachexmlgraphicsbatiksvgdom" -> "orgapachexmlgraphicsbatikdom"
        "orgapachexmlgraphicsbatiksvgdom" -> "orgapachexmlgraphicsbatikcss"
        "orgapachexmlgraphicsbatiksvgdom" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatiksvgdom" -> "orgapachexmlgraphicsbatikext"
        "orgapachexmlgraphicsbatiksvgdom" -> "xmlapisxmlapis"
        "orgapachexmlgraphicsbatiksvgdom" -> "xmlapisxmlapisext"
        "orgapachexmlgraphicsbatiksvgdom" -> "orgapachexmlgraphicsbatikanim"
        "orgapachexmlgraphicsbatikanim" -> "orgapachexmlgraphicsbatiksvgdom"
        "orgapachexmlgraphicsbatikanim" -> "orgapachexmlgraphicsbatikparser"
        "orgapachexmlgraphicsbatikanim" -> "orgapachexmlgraphicsbatikawtutil"
        "orgapachexmlgraphicsbatikanim" -> "orgapachexmlgraphicsbatikdom"
        "orgapachexmlgraphicsbatikanim" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatikanim" -> "orgapachexmlgraphicsbatikext"
        "orgapachexmlgraphicsbatikanim" -> "xmlapisxmlapis"
        "orgapachexmlgraphicsbatikanim" -> "xmlapisxmlapisext"
        "orgapachexmlgraphicsbatikparser" -> "orgapachexmlgraphicsbatikawtutil"
        "orgapachexmlgraphicsbatikparser" -> "orgapachexmlgraphicsbatikxml"
        "orgapachexmlgraphicsbatikparser" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatikxml" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatikdom" -> "orgapachexmlgraphicsbatikcss"
        "orgapachexmlgraphicsbatikdom" -> "orgapachexmlgraphicsbatikxml"
        "orgapachexmlgraphicsbatikdom" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatikdom" -> "orgapachexmlgraphicsbatikext"
        "orgapachexmlgraphicsbatikdom" -> "xalanxalan"
        "orgapachexmlgraphicsbatikdom" -> "xmlapisxmlapis"
        "orgapachexmlgraphicsbatikdom" -> "xmlapisxmlapisext"
        "orgapachexmlgraphicsbatikcss" -> "orgapachexmlgraphicsbatikutil"
        "orgapachexmlgraphicsbatikcss" -> "orgapachexmlgraphicsbatikext"
        "orgapachexmlgraphicsbatikcss" -> "xmlapisxmlapis"
        "orgapachexmlgraphicsbatikcss" -> "xmlapisxmlapisext"
        "orgapachexmlgraphicsbatikext" -> "xmlapisxmlapis"
        "xalanxalan" -> "xmlapisxmlapis"
        "orgapachexmlgraphicsbatikjs" -> "xmlapisxmlapis"
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleEmpty, ALL).generateGraph().toString()
    )
  }

  @Test fun singleProjectNoDuplicateDependencyConnections() {
    // Both RxJava and RxAndroid point transitively on reactivestreams.
    singleProject.dependencies.add("implementation", "io.reactivex.rxjava2:rxandroid:2.0.2")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "single" ["label"="single","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
        "ioreactivexrxjava2rxandroid" ["label"="rxandroid","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "single" -> "ioreactivexrxjava2rxjava"
        "single" -> "ioreactivexrxjava2rxandroid"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        "ioreactivexrxjava2rxandroid" -> "ioreactivexrxjava2rxjava"
        }
      """.trimIndent(),
      DependencyGraphGenerator(singleProject, ALL).generateGraph().toString()
    )
  }

  @Test fun multiProjectAll() {
    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "multimulti1" ["label"="multi1","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
        "multimulti2" ["label"="multi2","shape"="rectangle"]
        "ioreactivexrxjava2rxandroid" ["label"="rxandroid","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "multimulti1"
        "multimulti2"
        }
        "multimulti1" -> "orgjetbrainskotlinkotlinstdlib"
        "multimulti1" -> "ioreactivexrxjava2rxjava"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        "multimulti2" -> "ioreactivexrxjava2rxjava"
        "multimulti2" -> "ioreactivexrxjava2rxandroid"
        "ioreactivexrxjava2rxandroid" -> "ioreactivexrxjava2rxjava"
        }
      """.trimIndent(),
      DependencyGraphGenerator(multiProject, ALL).generateGraph().toString()
    )
  }

  @Test fun androidProjectArchitectureComponents() {
    androidProject.evaluate()

    androidProject.dependencies.add("implementation", "android.arch.persistence.room:runtime:1.0.0")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "android" ["label"="android","shape"="rectangle"]
        "androidarchpersistenceroomruntime" ["label"="persistence-room-runtime","shape"="rectangle"]
        "androidarchpersistenceroomcommon" ["label"="persistence-room-common","shape"="rectangle"]
        "comandroidsupportsupportannotations" ["label"="support-annotations","shape"="rectangle"]
        "androidarchpersistencedbframework" ["label"="persistence-db-framework","shape"="rectangle"]
        "androidarchpersistencedb" ["label"="persistence-db","shape"="rectangle"]
        "androidarchcoreruntime" ["label"="core-runtime","shape"="rectangle"]
        "androidarchcorecommon" ["label"="core-common","shape"="rectangle"]
        "comandroidsupportsupportcoreutils" ["label"="support-core-utils","shape"="rectangle"]
        "comandroidsupportsupportcompat" ["label"="support-compat","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "android"
        }
        "android" -> "androidarchpersistenceroomruntime"
        "androidarchpersistenceroomruntime" -> "androidarchpersistenceroomcommon"
        "androidarchpersistenceroomruntime" -> "androidarchpersistencedbframework"
        "androidarchpersistenceroomruntime" -> "androidarchpersistencedb"
        "androidarchpersistenceroomruntime" -> "androidarchcoreruntime"
        "androidarchpersistenceroomruntime" -> "comandroidsupportsupportcoreutils"
        "androidarchpersistenceroomcommon" -> "comandroidsupportsupportannotations"
        "androidarchpersistencedbframework" -> "androidarchpersistencedb"
        "androidarchpersistencedbframework" -> "comandroidsupportsupportannotations"
        "androidarchpersistencedb" -> "comandroidsupportsupportannotations"
        "androidarchcoreruntime" -> "androidarchcorecommon"
        "androidarchcoreruntime" -> "comandroidsupportsupportannotations"
        "androidarchcorecommon" -> "comandroidsupportsupportannotations"
        "comandroidsupportsupportcoreutils" -> "comandroidsupportsupportcompat"
        "comandroidsupportsupportcoreutils" -> "comandroidsupportsupportannotations"
        "comandroidsupportsupportcompat" -> "comandroidsupportsupportannotations"
        }
      """.trimIndent(),
      DependencyGraphGenerator(androidProject, ALL).generateGraph().toString()
    )
  }

  @Test fun androidProjectSqlDelight() {
    androidProject.evaluate()

    androidProject.dependencies.add("implementation", "com.squareup.sqldelight:runtime:0.6.1")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "android" ["label"="android","shape"="rectangle"]
        "comsquareupsqldelightruntime" ["label"="sqldelight-runtime","shape"="rectangle"]
        "comandroidsupportsupportannotations" ["label"="support-annotations","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "android"
        }
        "android" -> "comsquareupsqldelightruntime"
        "comsquareupsqldelightruntime" -> "comandroidsupportsupportannotations"
        }
      """.trimIndent(),
      DependencyGraphGenerator(androidProject, ALL).generateGraph().toString()
    )
  }

  @Test fun androidProjectIncludeAllFlavorsByDefault() {
    androidProjectExtension.flavorDimensions("test")
    androidProjectExtension.productFlavors { container ->
      container.create("flavor1").dimension = "test"
      container.create("flavor2").dimension = "test"
    }

    androidProject.evaluate()

    androidProject.dependencies.add("flavor1Implementation", "io.reactivex.rxjava2:rxandroid:2.0.2")
    androidProject.dependencies.add("flavor2DebugImplementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    androidProject.dependencies.add("flavor2ReleaseImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "android" ["label"="android","shape"="rectangle"]
        "ioreactivexrxjava2rxandroid" ["label"="rxandroid","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "android"
        }
        "android" -> "ioreactivexrxjava2rxandroid"
        "android" -> "ioreactivexrxjava2rxjava"
        "android" -> "orgjetbrainskotlinkotlinstdlib"
        "ioreactivexrxjava2rxandroid" -> "ioreactivexrxjava2rxjava"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        }
      """.trimIndent(),
      DependencyGraphGenerator(androidProject, ALL).generateGraph().toString()
    )
  }

  @Test fun androidProjectIncludeAllBuildTypesByDefault() {
    androidProjectExtension.buildTypes { container ->
      container.create("staging")
    }

    androidProject.evaluate()

    androidProject.dependencies.add("releaseImplementation", "io.reactivex.rxjava2:rxandroid:2.0.2")
    androidProject.dependencies.add("debugImplementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    androidProject.dependencies.add("stagingImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "android" ["label"="android","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
        "ioreactivexrxjava2rxandroid" ["label"="rxandroid","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "android"
        }
        "android" -> "ioreactivexrxjava2rxjava"
        "android" -> "ioreactivexrxjava2rxandroid"
        "android" -> "orgjetbrainskotlinkotlinstdlib"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        "ioreactivexrxjava2rxandroid" -> "ioreactivexrxjava2rxjava"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        }
      """.trimIndent(),
      DependencyGraphGenerator(androidProject, ALL).generateGraph().toString()
    )
  }

  @Test fun androidProjectIncludeOnlyStagingCompileClasspath() {
    androidProjectExtension.buildTypes { container ->
      container.create("staging")
    }

    androidProject.evaluate()

    androidProject.dependencies.add("releaseImplementation", "io.reactivex.rxjava2:rxandroid:2.0.2")
    androidProject.dependencies.add("debugImplementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    androidProject.dependencies.add("stagingImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "android" ["label"="android","shape"="rectangle"]
        "orgjetbrainskotlinkotlinstdlib" ["label"="kotlin-stdlib","shape"="rectangle"]
        "orgjetbrainsannotations" ["label"="jetbrains-annotations","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "android"
        }
        "android" -> "orgjetbrainskotlinkotlinstdlib"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        }
      """.trimIndent(),
      DependencyGraphGenerator(androidProject, ALL.copy(includeConfiguration = { it.name == "stagingCompileClasspath" })).generateGraph().toString()
    )
  }

  @Test fun androidProjectDoNotIncludeTestDependency() {
    androidProject.evaluate()

    androidProject.dependencies.add("testImplementation", "junit:junit:4.12")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "android" ["label"="android","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "android"
        }
        }
      """.trimIndent(),
      DependencyGraphGenerator(androidProject, ALL).generateGraph().toString()
    )
  }

  @Test fun androidProjectDoNotIncludeAndroidTestDependency() {
    androidProject.evaluate()

    androidProject.dependencies.add("androidTestImplementation", "junit:junit:4.12")

    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "android" ["label"="android","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "android"
        }
        }
      """.trimIndent(),
      DependencyGraphGenerator(androidProject, ALL).generateGraph().toString()
    )
  }

  @Test fun projectNamedLikeDependencyName() {
    assertEquals(
      """
        digraph "G" {
        edge ["dir"="forward"]
        node ["fontname"="Times New Roman"]
        "rxjava" ["label"="rxjava","shape"="rectangle"]
        "ioreactivexrxjava2rxjava" ["label"="rxjava","shape"="rectangle"]
        "orgreactivestreamsreactivestreams" ["label"="reactive-streams","shape"="rectangle"]
        {
        edge ["dir"="none"]
        graph ["rank"="same"]
        "rxjava"
        }
        "rxjava" -> "ioreactivexrxjava2rxjava"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        }
      """.trimIndent(),
      DependencyGraphGenerator(rxjavaProject, ALL).generateGraph().toString()
    )
  }
}
