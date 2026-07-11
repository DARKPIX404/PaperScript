plugins {
    java
}

group = "dev.paperscript"
version = "0.2.0"

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.release.set(8)
    options.encoding = "UTF-8"
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("paperscript-legacy")
}
