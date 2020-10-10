import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Calendar

plugins {
    java
    checkstyle
    `java-library`
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("com.github.hierynomus.license") version "0.15.0"
}

group = "com.proximyst.ban"
version = "0.1.0"

repositories {
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")

        content {
            includeGroup("net.kyori")
            includeGroup("cloud.commandframework")
        }
    }

    maven {
        name = "velocity"
        url = uri("https://repo.velocitypowered.com/snapshots/")
    }

    jcenter()
    mavenCentral()
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:1.1.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:1.1.0-SNAPSHOT")

    implementation("org.jdbi:jdbi3-core:3.14.1") {
        exclude("org.slf4j")
    }
    implementation("org.mariadb.jdbc:mariadb-java-client:2.6.1")
    implementation("com.zaxxer:HikariCP:3.4.5") {
        exclude("org.slf4j")
    }

    implementation("net.kyori:adventure-text-minimessage:4.0.0-SNAPSHOT") {
        // We get adventure through velocity-api
        isTransitive = false
    }

    implementation("org.apache.commons:commons-lang3:3.11")

    implementation("cloud.commandframework:cloud-velocity:1.0.0-SNAPSHOT")

    implementation("com.google.inject.extensions:guice-assistedinject:4.2.3") { // Velocity uses Guice 4.2.3
        // Only the deps provided by the artifact directly are wanted;
        // Guice already exists in Velocity.
        isTransitive = false
    }
}

tasks {
    withType<ShadowJar> {
        dependencies {
            exclude(dependency("org.checkerframework:checker-qual"))
        }

        fun reloc(vararg dependencies: String) =
            dependencies.forEach { relocate(it, "com.proximyst.ban.dependencies.$it") }

        reloc(
            "com.zaxxer.hikari",
            "org.mariadb.jdbc",
            "net.kyori.adventure.text.minimessage",
            "org.jdbi",
            "org.apache.commons.lang3",
            "com.github.benmanes.caffeine",
            "org.antlr",
            "io.leangen.geantyref",
            "cloud.commandframework",
            "com.google.inject.extensions.assistedinject"
        )
        mergeServiceFiles()
    }

    named("build").get().dependsOn(withType<ShadowJar>())
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
}

license {
    header = rootProject.file("LICENCE-HEADER")
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    include("**/*.java")

    mapping("java", "DOUBLESLASH_STYLE")
}

checkstyle {
    toolVersion = "8.36.2"
    val configRoot = rootProject.projectDir.resolve(".checkstyle")
    configDirectory.set(configRoot)
    configProperties["basedir"] = configRoot.absolutePath
}
