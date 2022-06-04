package com.vanniktech.dependency.graph.generator

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtensionsTest {
  @Test fun dotIdentifier() {
    assertEquals("javainject", "java.inject".dotIdentifier)
    assertEquals("firebasecore", "firebase-core".dotIdentifier)
    assertEquals("firebasecore", "firebase core".dotIdentifier)
  }

  @Test fun nonEmptyPrepend() {
    assertEquals("", "".nonEmptyPrepend("-"))
    assertEquals("-foo", "foo".nonEmptyPrepend("-"))
  }

  @Test fun toHyphenCase() {
    assertEquals("", "".toHyphenCase())
    assertEquals("something", "Something".toHyphenCase())
    assertEquals("something-big", "SomethingBig".toHyphenCase())
  }
}
