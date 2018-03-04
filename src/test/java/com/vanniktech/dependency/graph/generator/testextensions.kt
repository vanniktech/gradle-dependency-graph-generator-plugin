package com.vanniktech.dependency.graph.generator

import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Java6Assertions.assertThat

@Suppress("Detekt.TooGenericExceptionCaught", "Detekt.InstanceOfCheckForException", "Detekt.RethrowCaughtException") // https://github.com/arturbosch/detekt/issues/766
inline fun <reified T> assertThrows(block: () -> Unit): AbstractThrowableAssert<*, out Throwable> {
  try {
    block()
  } catch (e: Throwable) {
    if (e is T) {
      return assertThat(e)
    } else {
      throw e
    }
  }

  throw AssertionError("Expected ${T::class.simpleName}")
}
