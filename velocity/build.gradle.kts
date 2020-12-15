import com.proximyst.ban.CLOUD_VER
import com.proximyst.ban.ban

dependencies {
    api(project(":common"))

    compileOnly("com.velocitypowered:velocity-api:1.1.2")
    annotationProcessor("com.velocitypowered:velocity-api:1.1.2")

    implementation("cloud.commandframework:cloud-velocity:$CLOUD_VER")

    // allprojects dependencies that need to be shaded in on this platform:
    implementation("org.apache.commons:commons-lang3:3.11")
}

ban {
    relocations = setOf("org.apache.commons.lang3")
    javadocLinks = setOf("https://jd.velocitypowered.com/1.1.0/")
}