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
import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
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

    assertThat(DependencyGraphGenerator(singleEmpty, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "singleempty" ["shape"="rectangle","label"="singleempty"]
        {
        graph ["rank"="same"]
        "singleempty"
        }
        }
        """.trimIndent())
  }

  @Test fun singleProjectEmptyAllNoProjects() {
    assertThat(DependencyGraphGenerator(singleEmpty, ALL.copy(includeProject = { false })).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        }
        """.trimIndent())
  }

  @Test fun singleProjectEmptyAll() {
    assertThat(DependencyGraphGenerator(singleEmpty, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "singleempty" ["shape"="rectangle","label"="singleempty"]
        {
        graph ["rank"="same"]
        "singleempty"
        }
        }
        """.trimIndent())
  }

  @Test fun singleProjectEmptyAllMutateGraph() {
    assertThat(DependencyGraphGenerator(singleEmpty, ALL.copy(graph = { it.setDirected(false) })).generateGraph()).hasToString("""
        graph "G" {
        node ["fontname"="Times New Roman"]
        "singleempty" ["shape"="rectangle","label"="singleempty"]
        {
        graph ["rank"="same"]
        "singleempty"
        }
        }
        """.trimIndent())
  }

  @Test fun singleProjectEmptyAllHeader() {
    assertThat(DependencyGraphGenerator(singleEmpty, ALL.copy(label = Label.of("my custom header").locate(TOP).justify(LEFT))).generateGraph()).hasToString("""
        digraph "G" {
        graph ["labeljust"="l","labelloc"="t","label"="my custom header"]
        node ["fontname"="Times New Roman"]
        "singleempty" ["shape"="rectangle","label"="singleempty"]
        {
        graph ["rank"="same"]
        "singleempty"
        }
        }
        """.trimIndent())
  }

  @Test fun singleProjectEmptyAllRootFormatted() {
    assertThat(DependencyGraphGenerator(singleEmpty, ALL.copy(projectNode = { node, _ -> node.add(Shape.EGG, Style.DOTTED, Color.rgb("ff0099")) })).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "singleempty" ["shape"="egg","color"="#ff0099","style"="dotted","label"="singleempty"]
        {
        graph ["rank"="same"]
        "singleempty"
        }
        }
        """.trimIndent())
  }

  @Test fun singleProjectAll() {
    assertThat(DependencyGraphGenerator(singleProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "single" ["shape"="rectangle","label"="single"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        {
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "single" -> "ioreactivexrxjava2rxjava"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        }
        """.trimIndent())
  }

  @Test fun singleProjectAllDependencyFormattingOptions() {
    // Generate a color for each dependency.
    val dependencyNode: (MutableNode, ResolvedDependency) -> MutableNode = { node, project ->
      val random = Random(project.hashCode().toLong())
      node.add(Style.FILLED, Color.hsv(random.nextDouble(), random.nextDouble(), random.nextDouble())
      )
    }

    assertThat(DependencyGraphGenerator(singleProject, ALL.copy(dependencyNode = dependencyNode)).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "single" ["shape"="rectangle","label"="single"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","color"="0.0640501426300134 0.8237627867666534 0.01589038828058631","style"="filled","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","color"="0.5597323057578714 0.5443799058850397 0.1501148203700079","style"="filled","label"="jetbrains-annotations"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","color"="0.20985086076534065 0.9479919750755385 0.8980077536791101","style"="filled","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","color"="0.012912074006375618 0.7892530392440779 0.007857780626616906","style"="filled","label"="reactive-streams"]
        {
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "single" -> "ioreactivexrxjava2rxjava"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        }
        """.trimIndent())
  }

  @Test fun singleProjectNoChildren() {
    assertThat(DependencyGraphGenerator(singleProject, ALL.copy(children = { false })).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "single" ["shape"="rectangle","label"="single"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        {
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "single" -> "ioreactivexrxjava2rxjava"
        }
        """.trimIndent())
  }

  @Test fun singleProjectFilterRxJavaOut() {
    assertThat(DependencyGraphGenerator(singleProject, ALL.copy(include = { it.moduleGroup != "io.reactivex.rxjava2" })).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "single" ["shape"="rectangle","label"="single"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        {
        graph ["rank"="same"]
        "single"
        }
        "single" -> "orgjetbrainskotlinkotlinstdlib"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        }
        """.trimIndent())
  }

  @Test fun recursiveDependencies() {
      singleEmpty.dependencies.add("implementation", "org.apache.xmlgraphics:batik-gvt:1.7")

      assertThat(DependencyGraphGenerator(singleEmpty, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "singleempty" ["shape"="rectangle","label"="singleempty"]
        "orgapachexmlgraphicsbatikgvt" ["shape"="rectangle","label"="batik-gvt"]
        "orgapachexmlgraphicsbatikbridge" ["shape"="rectangle","label"="batik-bridge"]
        "orgapachexmlgraphicsbatikscript" ["shape"="rectangle","label"="batik-script"]
        "orgapachexmlgraphicsbatikawtutil" ["shape"="rectangle","label"="batik-awt-util"]
        "orgapachexmlgraphicsbatikutil" ["shape"="rectangle","label"="batik-util"]
        "xmlapisxmlapis" ["shape"="rectangle","label"="xml-apis"]
        "orgapachexmlgraphicsbatiksvgdom" ["shape"="rectangle","label"="batik-svg-dom"]
        "orgapachexmlgraphicsbatikanim" ["shape"="rectangle","label"="batik-anim"]
        "orgapachexmlgraphicsbatikparser" ["shape"="rectangle","label"="batik-parser"]
        "orgapachexmlgraphicsbatikxml" ["shape"="rectangle","label"="batik-xml"]
        "orgapachexmlgraphicsbatikdom" ["shape"="rectangle","label"="batik-dom"]
        "orgapachexmlgraphicsbatikcss" ["shape"="rectangle","label"="batik-css"]
        "orgapachexmlgraphicsbatikext" ["shape"="rectangle","label"="batik-ext"]
        "xmlapisxmlapisext" ["shape"="rectangle","label"="xml-apis-ext"]
        "xalanxalan" ["shape"="rectangle","label"="xalan"]
        "orgapachexmlgraphicsbatikjs" ["shape"="rectangle","label"="batik-js"]
        {
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
        """.trimIndent())
  }

  @Test fun singleProjectNoDuplicateDependencyConnections() {
    // Both RxJava and RxAndroid point transitively on reactivestreams.
    singleProject.dependencies.add("implementation", "io.reactivex.rxjava2:rxandroid:2.0.2")

    assertThat(DependencyGraphGenerator(singleProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "single" ["shape"="rectangle","label"="single"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        "ioreactivexrxjava2rxandroid" ["shape"="rectangle","label"="rxandroid"]
        {
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
        """.trimIndent())
  }

  @Test fun multiProjectAll() {
    assertThat(DependencyGraphGenerator(multiProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "multimulti1" ["shape"="rectangle","label"="multi1"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        "multimulti2" ["shape"="rectangle","label"="multi2"]
        "ioreactivexrxjava2rxandroid" ["shape"="rectangle","label"="rxandroid"]
        {
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
        """.trimIndent())
  }

  @Test fun androidProjectArchitectureComponents() {
    androidProject.evaluate()

    androidProject.dependencies.add("implementation", "android.arch.persistence.room:runtime:1.0.0")

    assertThat(DependencyGraphGenerator(androidProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "android" ["shape"="rectangle","label"="android"]
        "androidarchpersistenceroomruntime" ["shape"="rectangle","label"="persistence-room-runtime"]
        "androidarchpersistenceroomcommon" ["shape"="rectangle","label"="persistence-room-common"]
        "comandroidsupportsupportannotations" ["shape"="rectangle","label"="support-annotations"]
        "androidarchpersistencedbframework" ["shape"="rectangle","label"="persistence-db-framework"]
        "androidarchpersistencedb" ["shape"="rectangle","label"="persistence-db"]
        "androidarchcoreruntime" ["shape"="rectangle","label"="core-runtime"]
        "androidarchcorecommon" ["shape"="rectangle","label"="core-common"]
        "comandroidsupportsupportcoreutils" ["shape"="rectangle","label"="support-core-utils"]
        "comandroidsupportsupportcompat" ["shape"="rectangle","label"="support-compat"]
        {
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
        """.trimIndent())
  }

  @Test fun androidProjectSqlDelight() {
    androidProject.evaluate()

    androidProject.dependencies.add("implementation", "com.squareup.sqldelight:runtime:0.6.1")

    assertThat(DependencyGraphGenerator(androidProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "android" ["shape"="rectangle","label"="android"]
        "comsquareupsqldelightruntime" ["shape"="rectangle","label"="sqldelight-runtime"]
        "comandroidsupportsupportannotations" ["shape"="rectangle","label"="support-annotations"]
        {
        graph ["rank"="same"]
        "android"
        }
        "android" -> "comsquareupsqldelightruntime"
        "comsquareupsqldelightruntime" -> "comandroidsupportsupportannotations"
        }
        """.trimIndent())
  }

  @Test fun androidProjectIncludeAllFlavorsByDefault() {
    androidProjectExtension.flavorDimensions("test")
    androidProjectExtension.productFlavors {
      it.create("flavor1").dimension = "test"
      it.create("flavor2").dimension = "test"
    }

    androidProject.evaluate()

    androidProject.dependencies.add("flavor1Implementation", "io.reactivex.rxjava2:rxandroid:2.0.2")
    androidProject.dependencies.add("flavor2DebugImplementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    androidProject.dependencies.add("flavor2ReleaseImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")

    assertThat(DependencyGraphGenerator(androidProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "android" ["shape"="rectangle","label"="android"]
        "ioreactivexrxjava2rxandroid" ["shape"="rectangle","label"="rxandroid"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        {
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
        """.trimIndent())
  }

  @Test fun androidProjectIncludeAllBuildTypesByDefault() {
    androidProjectExtension.buildTypes {
      it.create("staging")
    }

    androidProject.evaluate()

    androidProject.dependencies.add("releaseImplementation", "io.reactivex.rxjava2:rxandroid:2.0.2")
    androidProject.dependencies.add("debugImplementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    androidProject.dependencies.add("stagingImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")

    assertThat(DependencyGraphGenerator(androidProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "android" ["shape"="rectangle","label"="android"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        "ioreactivexrxjava2rxandroid" ["shape"="rectangle","label"="rxandroid"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        {
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
        """.trimIndent())
  }

  @Test fun androidProjectIncludeOnlyStagingCompileClasspath() {
    androidProjectExtension.buildTypes {
      it.create("staging")
    }

    androidProject.evaluate()

    androidProject.dependencies.add("releaseImplementation", "io.reactivex.rxjava2:rxandroid:2.0.2")
    androidProject.dependencies.add("debugImplementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    androidProject.dependencies.add("stagingImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")

    assertThat(DependencyGraphGenerator(androidProject, ALL.copy(includeConfiguration = { it.name == "stagingCompileClasspath" })).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "android" ["shape"="rectangle","label"="android"]
        "orgjetbrainskotlinkotlinstdlib" ["shape"="rectangle","label"="kotlin-stdlib"]
        "orgjetbrainsannotations" ["shape"="rectangle","label"="jetbrains-annotations"]
        {
        graph ["rank"="same"]
        "android"
        }
        "android" -> "orgjetbrainskotlinkotlinstdlib"
        "orgjetbrainskotlinkotlinstdlib" -> "orgjetbrainsannotations"
        }
        """.trimIndent())
  }

  @Test fun androidProjectDoNotIncludeTestDependency() {
    androidProject.evaluate()

    androidProject.dependencies.add("testImplementation", "junit:junit:4.12")

    assertThat(DependencyGraphGenerator(androidProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "android" ["shape"="rectangle","label"="android"]
        {
        graph ["rank"="same"]
        "android"
        }
        }
        """.trimIndent())
  }

  @Test fun androidProjectDoNotIncludeAndroidTestDependency() {
    androidProject.evaluate()

    androidProject.dependencies.add("androidTestImplementation", "junit:junit:4.12")

    assertThat(DependencyGraphGenerator(androidProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "android" ["shape"="rectangle","label"="android"]
        {
        graph ["rank"="same"]
        "android"
        }
        }
        """.trimIndent())
  }

  @Test fun projectNamedLikeDependencyName() {
    assertThat(DependencyGraphGenerator(rxjavaProject, ALL).generateGraph()).hasToString("""
        digraph "G" {
        node ["fontname"="Times New Roman"]
        "rxjava" ["shape"="rectangle","label"="rxjava"]
        "ioreactivexrxjava2rxjava" ["shape"="rectangle","label"="rxjava"]
        "orgreactivestreamsreactivestreams" ["shape"="rectangle","label"="reactive-streams"]
        {
        graph ["rank"="same"]
        "rxjava"
        }
        "rxjava" -> "ioreactivexrxjava2rxjava"
        "ioreactivexrxjava2rxjava" -> "orgreactivestreamsreactivestreams"
        }
        """.trimIndent())
  }
}
