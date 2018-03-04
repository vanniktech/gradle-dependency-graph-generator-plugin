package com.vanniktech.dependency.graph.generator

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class ExtensionsTest {
  @Test fun dotIdentifier() {
    assertThat("java.inject".dotIdentifier).isEqualTo("javainject")
    assertThat("firebase-core".dotIdentifier).isEqualTo("firebasecore")
    assertThat("firebase core".dotIdentifier).isEqualTo("firebasecore")
  }

  @Test fun nonEmptyPrepend() {
    assertThat("".nonEmptyPrepend("-")).isEmpty()
    assertThat("foo".nonEmptyPrepend("-")).isEqualTo("-foo")
  }

  @Test fun toHyphenCase() {
    assertThat("".toHyphenCase()).isEqualTo("")
    assertThat("Something".toHyphenCase()).isEqualTo("something")
    assertThat("SomethingBig".toHyphenCase()).isEqualTo("something-big")
  }
}
