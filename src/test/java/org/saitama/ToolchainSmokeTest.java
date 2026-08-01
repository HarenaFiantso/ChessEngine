package org.saitama;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Guards the build's central promise: code compiles and runs on Java 26.
///
/// The project relies on language and library features that only exist on recent JDKs, so a build
/// silently falling back to an older JVM would fail in confusing ways far from the root cause. This
/// test turns the toolchain declaration in `build.gradle.kts` into an executable assertion.
class ToolchainSmokeTest {

  @Test
  @DisplayName("tests execute on the Java 26 toolchain declared in the build")
  void testsExecuteOnTheDeclaredToolchain() {
    assertEquals(
        26,
        Runtime.version().feature(),
        "Tests must run on the JDK pinned by the Gradle toolchain");
  }
}
