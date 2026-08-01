plugins {
    // `application` implies the `java` plugin and adds the `run` task,
    // so every iteration of the engine stays runnable from the command line.
    application
}

group = "org.saitama"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        // The toolchain decouples the JDK that runs Gradle from the JDK that
        // compiles and runs the project: the build targets Java 26 regardless
        // of which JVM the developer happens to have on the PATH.
        languageVersion = JavaLanguageVersion.of(26)
    }
}

application {
    mainClass = "org.saitama.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
