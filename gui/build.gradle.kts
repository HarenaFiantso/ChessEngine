plugins {
    application
    checkstyle
    jacoco
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
    id("com.github.spotbugs")
    id("org.openjfx.javafxplugin")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

application {
    mainClass = "org.saitama.gui.SaitamaGui"
}

javafx {
    version = "26"
    modules = listOf("javafx.controls")
}

tasks.named<JavaExec>("run") {
    jvmArgs("--enable-native-access=javafx.graphics")
}

checkstyle {
    toolVersion = "13.9.0"
    maxWarnings = 0
}

jacoco {
    toolVersion = "0.8.15"
}

spotbugs {
    toolVersion = "4.10.3"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":engine"))
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    testImplementation(platform("org.junit:junit-bom:6.1.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

spotless {
    java {
        googleJavaFormat("1.36.1")
        formatAnnotations()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Werror")
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") {
        required = true
    }
}

tasks.spotbugsTest {
    excludeFilter = rootProject.file("config/spotbugs/test-exclude.xml")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "org/saitama/gui/SaitamaGui*.class",
                    "org/saitama/gui/BoardView*.class"
                )
            }
        }
    )
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
