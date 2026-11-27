import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "com.pandacubgames"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Only platform APIs are used, so compiling against IDEA Community keeps the
        // plugin compatible with every JetBrains IDE, Rider included.
        create("IC", "2024.3")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
        }
        changeNotes = "<ul><li>Initial scaffold.</li></ul>"
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdea, "2024.3")
            create(IntelliJPlatformType.Rider, "2024.3")
        }
    }
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

kotlin {
    jvmToolchain(21)
}
