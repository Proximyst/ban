import com.proximyst.ban.ban

ban {
    relocations = setOf("org.springframework")
    javadocLinks = setOf("https://jd.velocitypowered.com/")
}

dependencies {
    implementation("org.springframework:spring-core:5.3.1")
}