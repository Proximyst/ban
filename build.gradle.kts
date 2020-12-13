import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.proximyst.ban.BanGradlePlugin
import com.proximyst.ban.CLOUD_VER
import com.proximyst.ban.GUICE_VER
import com.proximyst.ban.ban
import java.util.Calendar
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.checkerframework.gradle.plugin.CheckerFrameworkPlugin

plugins {
    java
    checkstyle
    `java-library`
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("com.github.hierynomus.license") version "0.15.0"
    id("org.checkerframework") version "0.5.12"
    ban
}

allprojects {
    group = "com.proximyst.ban"
    version = "0.1.0"
}

subprojects {
    apply {
        plugin<JavaPlugin>()
        plugin<JavaLibraryPlugin>()
        plugin<CheckstylePlugin>()
        plugin<ShadowPlugin>()
        plugin<LicensePlugin>()
        plugin<CheckerFrameworkPlugin>()
        plugin<BanGradlePlugin>()
    }

    repositories {
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")

            content {
                includeGroup("net.kyori")
            }
        }

        maven {
            name = "velocity"
            url = uri("https://repo.velocitypowered.com/snapshots/")

            content {
                includeGroup("com.velocitypowered")
            }
        }

        maven {
            name = "mojang"
            url = uri("https://libraries.minecraft.net")

            content {
                includeGroup("com.mojang")
            }
        }

        jcenter()
        mavenCentral()
    }

    dependencies {
        compileOnlyApi("org.slf4j:slf4j-api:1.7.30") // The API shouldn't change too drastically...
        implementation("org.jdbi:jdbi3-core:3.17.0") {
            exclude("org.slf4j")
        }
        implementation("org.mariadb.jdbc:mariadb-java-client:2.7.0")
        implementation("com.zaxxer:HikariCP:3.4.5") {
            exclude("org.slf4j")
        }
        implementation("org.flywaydb:flyway-core:7.1.1")

        compileOnlyApi("net.kyori:adventure-api:4.3.0")
        implementation("net.kyori:adventure-text-minimessage:4.0.0-SNAPSHOT") {
            // We get adventure through velocity-api
            isTransitive = false
        }

        // TODO: Include apache commons lang per-platform; see what Bukkit uses here...
        compileOnly("org.apache.commons:commons-lang3:3.11")

        compileOnly("com.google.code.gson:gson:2.8.6")

        implementation("cloud.commandframework:cloud-core:$CLOUD_VER")

        compileOnlyApi("com.google.inject:guice:$GUICE_VER")
        implementation("com.google.inject.extensions:guice-assistedinject:$GUICE_VER") {
            // Only the deps provided by the artifact directly are wanted;
            // Guice will be provided as shown above. Its shadowing state is set by the platform.
            isTransitive = false
        }

        compileOnlyApi("org.spongepowered:configurate-core:3.7.1")
    }

    tasks {
        withType<ShadowJar> {
            // Make this the default jar
            this.archiveClassifier.set(null as String?)

            // Make the jar on the form of: ban-velocity-0.1.0.jar
            this.archiveBaseName.set("${rootProject.name}-${project.name}")

            if (System.getenv("DISABLE_VERSION_JAR") != null) {
                // The user does not want us to have a version attached to the jar name.
                this.archiveVersion.set(null as String?)
            }

            dependencies {
                exclude(dependency("org.checkerframework:checker-qual"))
            }

            fun reloc(vararg dependencies: String) {
                // We want every dependency under the same package.
                // This is just to ensure they're always there.
                dependencies.forEach { relocate(it, "${rootProject.group}.dependencies.$it") }
            }

            // Some relocations are just always going to be applied, so apply those first.
            reloc(
                "com.zaxxer.hikari",
                "org.mariadb.jdbc",
                "net.kyori.adventure.text.minimessage",
                "org.jdbi",
                "com.github.benmanes.caffeine",
                "org.antlr",
                "io.leangen.geantyref",
                "cloud.commandframework",
                "com.google.inject.extensions.assistedinject",
                "com.google.inject.assistedinject",
                "org.flyway"
            )
            doFirst {
                reloc(*ban.relocations.toTypedArray())
            }
            mergeServiceFiles()
        }

        named("build").get().dependsOn(withType<ShadowJar>())

        compileJava {
            this.options.apply {
                isFork = true
                compilerArgs.add("-Xlint:all")
                compilerArgs.add("-parameters")
            }
        }

        javadoc {
            val opt = options as StandardJavadocDocletOptions
            opt.addStringOption("Xdoclint:none", "-quiet")

            opt.encoding("UTF-8")
            opt.charSet("UTF-8")
            opt.source("11")
            doFirst {
                opt.links(
                    "https://docs.oracle.com/javase/8/docs/api/",
                    "http://www.slf4j.org/apidocs/",
                    "https://google.github.io/guava/releases/25.1-jre/api/docs/",
                    "https://google.github.io/guice/api-docs/4.2/javadoc/",
                    "https://jd.adventure.kyori.net/api/4.1.1/",
                    *ban.javadocLinks.toTypedArray()
                )
            }
        }
    }
}

// These are some options that either won't apply to rootProject, or
// will be nice to have to disable potential warnings and errors.
allprojects {
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
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
        val configRoot = rootProject.projectDir
        configDirectory.set(configRoot)
        configProperties["basedir"] = configRoot.absolutePath
    }
}

repositories {
    jcenter() // Gradle plugins.
}
