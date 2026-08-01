plugins {
    // Auto-provisions the JDK requested by the toolchain block in build.gradle.kts
    // when it is not already installed, keeping the build reproducible on any machine.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ChessEngine"
