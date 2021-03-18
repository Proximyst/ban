import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.proximyst.ban.*
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
    jacoco
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
        plugin<JacocoPlugin>()
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

        maven {
            name = "proxi-nexus"
            url = uri("https://nexus.proximyst.com/repository/maven-any/")

            content {
                includeGroup("com.proximyst.moonshine")
            }
        }

        jcenter()
        mavenCentral()
    }

    dependencies {
        runtimeOnly("com.github.ben-manes.caffeine:caffeine:2.8.8") {
            exclude("com.google.errorprone", "error_prone_annotations")
        }
        compileOnlyApi("org.slf4j:slf4j-api:$SLF4J_VER") // The API shouldn't change too drastically...
        testImplementation("org.slf4j:slf4j-api:$SLF4J_VER") // The API shouldn't change too drastically...
        implementation("org.jdbi:jdbi3-core:$JDBI_VER") {
            exclude("org.slf4j")
            exclude("com.github.ben-manes.caffeine")
        }
        implementation("org.jdbi:jdbi3-postgres:$JDBI_VER") {
            exclude("org.slf4j")
            exclude("com.github.ben-manes.caffeine")
        }
        implementation("org.postgresql:postgresql:42.2.19")
        implementation("com.zaxxer:HikariCP:4.0.3") {
            exclude("org.slf4j")
        }
        implementation("org.flywaydb:flyway-core:7.7.0")

        compileOnlyApi("net.kyori:adventure-api:$ADVENTURE_VER")
        testImplementation("net.kyori:adventure-api:$ADVENTURE_VER")
        implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") {
            // We already get adventure elsewhere.
            exclude("net.kyori", "adventure-api")
        }

        // TODO: Include apache commons lang per-platform; see what Bukkit uses here...
        compileOnly("org.apache.commons:commons-lang3:$COMMONS_LANG_VER")
        testImplementation("org.apache.commons:commons-lang3:$COMMONS_LANG_VER")

        compileOnly("com.google.code.gson:gson:$GSON_VER")
        testImplementation("com.google.code.gson:gson:$GSON_VER")

        implementation("cloud.commandframework:cloud-core:$CLOUD_VER")

        implementation("io.github.openfeign:feign-core:11.0")
        implementation("io.github.openfeign:feign-java11:10.12")
        implementation("io.github.openfeign:feign-gson:11.0") {
            exclude("com.google.code.gson")
        }

        compileOnlyApi("com.google.inject:guice:$GUICE_VER")
        testImplementation("com.google.inject:guice:$GUICE_VER")
        implementation("com.google.inject.extensions:guice-assistedinject:$GUICE_VER") {
            // Only the deps provided by the artifact directly are wanted;
            // Guice will be provided as shown above. Its shadowing state is set by the platform.
            isTransitive = false
        }

        compileOnlyApi("com.proximyst.moonshine:core:$MOONSHINE_VER")
        testImplementation("com.proximyst.moonshine:core:$MOONSHINE_VER")

        compileOnlyApi("org.spongepowered:configurate-core:$CONFIGURATE_VER")
        compileOnlyApi("org.spongepowered:configurate-hocon:$CONFIGURATE_VER")
        testImplementation("org.spongepowered:configurate-core:$CONFIGURATE_VER")
        testImplementation("org.spongepowered:configurate-hocon:$CONFIGURATE_VER")

        testImplementation("org.junit.jupiter:junit-jupiter:5.+")
        testImplementation("org.mockito:mockito-core:3.+")
        testImplementation("org.mockito:mockito-junit-jupiter:3.+")
        testImplementation("org.assertj:assertj-core:3.+")
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
                "org.postgresql",
                "net.kyori.adventure.text.minimessage",
                "org.jdbi",
                "com.github.benmanes.caffeine",
                "org.antlr",
                "io.leangen.geantyref",
                "cloud.commandframework",
                "com.google.inject.extensions.assistedinject",
                "com.google.inject.assistedinject",
                "org.flyway",
                "feign",
                "com.proximyst.moonshine"
            )
            doFirst {
                reloc(*ban.relocations.toTypedArray())
            }
            mergeServiceFiles()
            minimize {
                exclude(dependency("com.github.ben-manes.caffeine:caffeine"))
            }
        }

        named("build").get().dependsOn(withType<ShadowJar>())

        compileJava {
            this.options.apply {
                isFork = true
                compilerArgs.add("-Xlint:all")
                compilerArgs.add("-parameters")
            }
        }

        compileTestJava {
            this.options.apply {
                isFork = true
                compilerArgs.add("-Xlint:all")
                compilerArgs.add("-parameters")
            }
        }

        val jacocoTestReport by getting(JacocoReport::class)
        test {
            useJUnitPlatform()
            finalizedBy(jacocoTestReport)
        }

        jacocoTestReport {
            dependsOn(test)
            reports {
                xml.isEnabled = true
                html.isEnabled = false
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
                    "https://docs.oracle.com/en/java/javase/11/docs/api/",
                    "http://www.slf4j.org/apidocs/",
                    "https://google.github.io/guava/releases/25.1-jre/api/docs/",
                    "https://google.github.io/guice/api-docs/4.2/javadoc/",
                    "https://jd.adventure.kyori.net/api/4.3.0/",
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
        disableAutoTargetJvm()
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

    jacoco {
        reportsDir = rootProject.buildDir.resolve("reports").resolve("jacoco")
    }
}

repositories {
    mavenCentral()
}
