plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("ban") {
            id = "ban"
            implementationClass = "com.proximyst.ban.BanGradlePlugin"
        }
    }
}