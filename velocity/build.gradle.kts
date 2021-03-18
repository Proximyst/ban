import com.proximyst.ban.CLOUD_VER
import com.proximyst.ban.MOONSHINE_VER
import com.proximyst.ban.COMMONS_LANG_VER
import com.proximyst.ban.ban

dependencies {
    api(project(":common"))

    compileOnly("com.velocitypowered:velocity-api:1.1.3")
    annotationProcessor("com.velocitypowered:velocity-api:1.1.3")

    implementation("cloud.commandframework:cloud-velocity:$CLOUD_VER")

    // allprojects dependencies that need to be shaded in on this platform:
    implementation("org.apache.commons:commons-lang3:$COMMONS_LANG_VER")
    implementation("com.proximyst.moonshine:core:$MOONSHINE_VER") {
        exclude("com.google.guava", "guava")
    }
}

ban {
    relocations = setOf("org.apache.commons.lang3")
    javadocLinks = setOf("https://jd.velocitypowered.com/1.1.0/")
}