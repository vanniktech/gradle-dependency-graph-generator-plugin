package com.vanniktech.dependency.graph.generator

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator.Companion.ALL
import com.vanniktech.dependency.graph.generator.dot.Header
import com.vanniktech.dependency.graph.generator.dot.Color.Companion.MAX_COLOR_VALUE
import com.vanniktech.dependency.graph.generator.dot.Shape
import com.vanniktech.dependency.graph.generator.dot.Style
import com.vanniktech.dependency.graph.generator.dot.Color
import com.vanniktech.dependency.graph.generator.dot.GraphFormattingOptions
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

class DotGeneratorTest {
  private lateinit var singleEmpty: Project
  private lateinit var singleProject: Project
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
    manifestFile.writeText("""
        |<?xml version="1.0" encoding="utf-8"?>
        |<manifest package="com.foo.bar" xmlns:android="http://schemas.android.com/apk/res/android">
        |  <application/>
        |</manifest>""".trimMargin())
  }

  @Test fun singleProjectAllNoTestDependencies() {
    singleEmpty.dependencies.add("testImplementation", "junit:junit:4.12")

    assertThat(DotGenerator(singleEmpty, ALL).generateContent()).isEqualTo("""
        |digraph G {
        |  singleempty [label="singleempty", shape="box"];
        |}
        |""".trimMargin())
  }

  @Test fun singleProjectEmptyAll() {
    assertThat(DotGenerator(singleEmpty, ALL).generateContent()).isEqualTo("""
        |digraph G {
        |  singleempty [label="singleempty", shape="box"];
        |}
        |""".trimMargin())
  }

  @Test fun singleProjectEmptyAllHeader() {
    assertThat(DotGenerator(singleEmpty, ALL.copy(header = Header("my custom header"))).generateContent()).isEqualTo("""
        |digraph G {
        |  label="my custom header" fontsize="24" height="5" labelloc="t" labeljust="c";
        |  singleempty [label="singleempty", shape="box"];
        |}
        |""".trimMargin())
  }

  @Test fun singleProjectEmptyAllRootSuffix() {
    assertThat(DotGenerator(singleEmpty, ALL.copy(rootSuffix = "my suffix")).generateContent()).isEqualTo("""
        |digraph G {
        |  singleemptymysuffix [label="singleempty my suffix", shape="box"];
        |}
        |""".trimMargin())
  }

  @Test fun singleProjectEmptyAllRootFormatted() {
    assertThat(DotGenerator(singleEmpty, ALL.copy(rootFormattingOptions = GraphFormattingOptions(Shape.EGG, Style.DOTTED, Color.fromHex("#ff0099")))).generateContent()).isEqualTo("""
        |digraph G {
        |  singleempty [label="singleempty", shape="egg", style="dotted", color="#ff0099"];
        |}
        |""".trimMargin())
  }

  @Test fun singleProjectAll() {
    assertThat(DotGenerator(singleProject, ALL).generateContent()).isEqualTo("""
        |digraph G {
        |  single [label="single", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box"];
        |  single -> orgjetbrainskotlinkotlinstdlib;
        |  orgjetbrainsannotations [label="annotations", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib -> orgjetbrainsannotations;
        |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
        |  single -> ioreactivexrxjava2rxjava;
        |  orgreactivestreamsreactivestreams [label="reactive-streams", shape="box"];
        |  ioreactivexrxjava2rxjava -> orgreactivestreamsreactivestreams;
        |}
        |""".trimMargin())
  }

  @Test fun singleProjectAllDependencyFormattingOptions() {
    // Generate a color for each dependency.
    val dependencyFormattingOptions: (ResolvedDependency) -> GraphFormattingOptions = {
      val random = Random(it.hashCode().toLong())
      GraphFormattingOptions(color = Color.fromRgb(random.nextInt(MAX_COLOR_VALUE), random.nextInt(MAX_COLOR_VALUE), random.nextInt(MAX_COLOR_VALUE))
      )
    }

    assertThat(DotGenerator(singleProject, ALL.copy(dependencyFormattingOptions = dependencyFormattingOptions)).generateContent()).isEqualTo("""
        |digraph G {
        |  single [label="single", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box", color="#6ba46e"];
        |  single -> orgjetbrainskotlinkotlinstdlib;
        |  orgjetbrainsannotations [label="annotations", shape="box", color="#4a09b2"];
        |  orgjetbrainskotlinkotlinstdlib -> orgjetbrainsannotations;
        |  ioreactivexrxjava2rxjava [label="rxjava", shape="box", color="#cb660b"];
        |  single -> ioreactivexrxjava2rxjava;
        |  orgreactivestreamsreactivestreams [label="reactive-streams", shape="box", color="#7c70b6"];
        |  ioreactivexrxjava2rxjava -> orgreactivestreamsreactivestreams;
        |}
        |""".trimMargin())
  }

  @Suppress("Detekt.UnnecessaryParentheses") // https://github.com/arturbosch/detekt/issues/767
  @Test fun singleProjectNoChildren() {
    assertThat(DotGenerator(singleProject, ALL.copy(children = { false })).generateContent()).isEqualTo("""
        |digraph G {
        |  single [label="single", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box"];
        |  single -> orgjetbrainskotlinkotlinstdlib;
        |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
        |  single -> ioreactivexrxjava2rxjava;
        |}
        |""".trimMargin())
  }

  @Suppress("Detekt.UnnecessaryParentheses") // https://github.com/arturbosch/detekt/issues/767
  @Test fun singleProjectFilterRxJavaOut() {
    assertThat(DotGenerator(singleProject, ALL.copy(include = { it.moduleGroup != "io.reactivex.rxjava2" })).generateContent()).isEqualTo("""
        |digraph G {
        |  single [label="single", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box"];
        |  single -> orgjetbrainskotlinkotlinstdlib;
        |  orgjetbrainsannotations [label="annotations", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib -> orgjetbrainsannotations;
        |}
        |""".trimMargin())
  }

  @Test fun singleProjectNoDuplicateDependencyConnections() {
    // Both RxJava and RxAndroid point transitively on reactivestreams.
    singleProject.dependencies.add("implementation", "io.reactivex.rxjava2:rxandroid:2.0.2")

    assertThat(DotGenerator(singleProject, ALL).generateContent()).isEqualTo("""
        |digraph G {
        |  single [label="single", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box"];
        |  single -> orgjetbrainskotlinkotlinstdlib;
        |  orgjetbrainsannotations [label="annotations", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib -> orgjetbrainsannotations;
        |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
        |  single -> ioreactivexrxjava2rxjava;
        |  orgreactivestreamsreactivestreams [label="reactive-streams", shape="box"];
        |  ioreactivexrxjava2rxjava -> orgreactivestreamsreactivestreams;
        |  ioreactivexrxjava2rxandroid [label="rxandroid", shape="box"];
        |  single -> ioreactivexrxjava2rxandroid;
        |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
        |  ioreactivexrxjava2rxandroid -> ioreactivexrxjava2rxjava;
        |}
        |""".trimMargin())
  }

  @Test fun multiProjectAll() {
    assertThat(DotGenerator(multiProject, ALL).generateContent()).isEqualTo("""
        |digraph G {
        |  multi1 [label="multi1", shape="box"];
        |  multi2 [label="multi2", shape="box"];
        |  { rank = same; "multi1"; "multi2" };
        |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box"];
        |  multi1 -> orgjetbrainskotlinkotlinstdlib;
        |  orgjetbrainsannotations [label="annotations", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib -> orgjetbrainsannotations;
        |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
        |  multi1 -> ioreactivexrxjava2rxjava;
        |  orgreactivestreamsreactivestreams [label="reactive-streams", shape="box"];
        |  ioreactivexrxjava2rxjava -> orgreactivestreamsreactivestreams;
        |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
        |  multi2 -> ioreactivexrxjava2rxjava;
        |  ioreactivexrxjava2rxandroid [label="rxandroid", shape="box"];
        |  multi2 -> ioreactivexrxjava2rxandroid;
        |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
        |  ioreactivexrxjava2rxandroid -> ioreactivexrxjava2rxjava;
        |}
        |""".trimMargin())
  }

  @Test fun androidProjectArchitectureComponents() {
    androidProject.evaluate()

    androidProject.dependencies.add("implementation", "android.arch.persistence.room:runtime:1.0.0")

    assertThat(DotGenerator(androidProject, ALL).generateContent()).isEqualTo("""
        |digraph G {
        |  android [label="android", shape="box"];
        |  androidarchpersistenceroomruntime [label="persistence-room-runtime", shape="box"];
        |  android -> androidarchpersistenceroomruntime;
        |  androidarchpersistenceroomcommon [label="persistence-room-common", shape="box"];
        |  androidarchpersistenceroomruntime -> androidarchpersistenceroomcommon;
        |  comandroidsupportsupportannotations [label="support-annotations", shape="box"];
        |  androidarchpersistenceroomcommon -> comandroidsupportsupportannotations;
        |  androidarchpersistencedbframework [label="persistence-db-framework", shape="box"];
        |  androidarchpersistenceroomruntime -> androidarchpersistencedbframework;
        |  androidarchpersistencedb [label="persistence-db", shape="box"];
        |  androidarchpersistencedbframework -> androidarchpersistencedb;
        |  comandroidsupportsupportannotations [label="support-annotations", shape="box"];
        |  androidarchpersistencedb -> comandroidsupportsupportannotations;
        |  comandroidsupportsupportannotations [label="support-annotations", shape="box"];
        |  androidarchpersistencedbframework -> comandroidsupportsupportannotations;
        |  androidarchpersistencedb [label="persistence-db", shape="box"];
        |  androidarchpersistenceroomruntime -> androidarchpersistencedb;
        |  androidarchcoreruntime [label="core-runtime", shape="box"];
        |  androidarchpersistenceroomruntime -> androidarchcoreruntime;
        |  androidarchcorecommon [label="core-common", shape="box"];
        |  androidarchcoreruntime -> androidarchcorecommon;
        |  comandroidsupportsupportannotations [label="support-annotations", shape="box"];
        |  androidarchcorecommon -> comandroidsupportsupportannotations;
        |  comandroidsupportsupportannotations [label="support-annotations", shape="box"];
        |  androidarchcoreruntime -> comandroidsupportsupportannotations;
        |  comandroidsupportsupportcoreutils [label="support-core-utils", shape="box"];
        |  androidarchpersistenceroomruntime -> comandroidsupportsupportcoreutils;
        |  comandroidsupportsupportcompat [label="support-compat", shape="box"];
        |  comandroidsupportsupportcoreutils -> comandroidsupportsupportcompat;
        |  comandroidsupportsupportannotations [label="support-annotations", shape="box"];
        |  comandroidsupportsupportcompat -> comandroidsupportsupportannotations;
        |  comandroidsupportsupportannotations [label="support-annotations", shape="box"];
        |  comandroidsupportsupportcoreutils -> comandroidsupportsupportannotations;
        |}
        |""".trimMargin())
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

    assertThat(DotGenerator(androidProject, ALL).generateContent()).isEqualTo("""
      |digraph G {
      |  android [label="android", shape="box"];
      |  ioreactivexrxjava2rxandroid [label="rxandroid", shape="box"];
      |  android -> ioreactivexrxjava2rxandroid;
      |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
      |  ioreactivexrxjava2rxandroid -> ioreactivexrxjava2rxjava;
      |  orgreactivestreamsreactivestreams [label="reactive-streams", shape="box"];
      |  ioreactivexrxjava2rxjava -> orgreactivestreamsreactivestreams;
      |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
      |  android -> ioreactivexrxjava2rxjava;
      |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box"];
      |  android -> orgjetbrainskotlinkotlinstdlib;
      |  orgjetbrainsannotations [label="annotations", shape="box"];
      |  orgjetbrainskotlinkotlinstdlib -> orgjetbrainsannotations;
      |}
      |""".trimMargin())
  }

  @Test fun androidProjectIncludeAllBuildTypesByDefault() {
    androidProjectExtension.buildTypes {
      it.create("staging")
    }

    androidProject.evaluate()

    androidProject.dependencies.add("releaseImplementation", "io.reactivex.rxjava2:rxandroid:2.0.2")
    androidProject.dependencies.add("debugImplementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    androidProject.dependencies.add("stagingImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")

    assertThat(DotGenerator(androidProject, ALL).generateContent()).isEqualTo("""
      |digraph G {
      |  android [label="android", shape="box"];
      |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
      |  android -> ioreactivexrxjava2rxjava;
      |  orgreactivestreamsreactivestreams [label="reactive-streams", shape="box"];
      |  ioreactivexrxjava2rxjava -> orgreactivestreamsreactivestreams;
      |  ioreactivexrxjava2rxandroid [label="rxandroid", shape="box"];
      |  android -> ioreactivexrxjava2rxandroid;
      |  ioreactivexrxjava2rxjava [label="rxjava", shape="box"];
      |  ioreactivexrxjava2rxandroid -> ioreactivexrxjava2rxjava;
      |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box"];
      |  android -> orgjetbrainskotlinkotlinstdlib;
      |  orgjetbrainsannotations [label="annotations", shape="box"];
      |  orgjetbrainskotlinkotlinstdlib -> orgjetbrainsannotations;
      |}
      |""".trimMargin())
  }

  @Suppress("Detekt.UnnecessaryParentheses") // https://github.com/arturbosch/detekt/issues/767
  @Test fun androidProjectIncludeOnlyStagingCompileClasspath() {
    androidProjectExtension.buildTypes {
      it.create("staging")
    }

    androidProject.evaluate()

    androidProject.dependencies.add("releaseImplementation", "io.reactivex.rxjava2:rxandroid:2.0.2")
    androidProject.dependencies.add("debugImplementation", "io.reactivex.rxjava2:rxjava:2.1.10")
    androidProject.dependencies.add("stagingImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.2.30")

    assertThat(DotGenerator(androidProject, ALL.copy(includeConfiguration = { it.name == "stagingCompileClasspath" })).generateContent()).isEqualTo("""
        |digraph G {
        |  android [label="android", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib [label="kotlin-stdlib", shape="box"];
        |  android -> orgjetbrainskotlinkotlinstdlib;
        |  orgjetbrainsannotations [label="annotations", shape="box"];
        |  orgjetbrainskotlinkotlinstdlib -> orgjetbrainsannotations;
        |}
        |""".trimMargin())
  }

  @Test fun androidProjectDoNotIncludeTestDependency() {
    androidProject.evaluate()

    androidProject.dependencies.add("testImplementation", "junit:junit:4.12")

    assertThat(DotGenerator(androidProject, ALL).generateContent()).isEqualTo("""
        |digraph G {
        |  android [label="android", shape="box"];
        |}
        |""".trimMargin())
  }

  @Test fun androidProjectDoNotIncludeAndroidTestDependency() {
    androidProject.evaluate()

    androidProject.dependencies.add("androidTestImplementation", "junit:junit:4.12")

    assertThat(DotGenerator(androidProject, ALL).generateContent()).isEqualTo("""
        |digraph G {
        |  android [label="android", shape="box"];
        |}
        |""".trimMargin())
  }
}
