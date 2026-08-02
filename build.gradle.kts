plugins {
    id("com.diffplug.spotless") version "8.9.0" apply false
    id("me.champeau.jmh") version "0.7.3" apply false
    id("net.ltgt.errorprone") version "5.1.0" apply false
    id("com.github.spotbugs") version "6.5.9" apply false
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
}

allprojects {
    group = "org.saitama"
    version = "0.2.0-SNAPSHOT"
}
