plugins {
    java
    id("com.gradleup.shadow") version "9.0.2"
}

group = "dev.paperscript"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val graalVersion = "24.2.2"
val paperVersion = "1.21.1-R0.1-SNAPSHOT"

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")

    // Embed GraalJS on a stock JDK. Depend on the concrete jars: the
    // org.graalvm.js:js / :js-community artifacts are POM-only aggregators that
    // break fat-jar expansion and pull the GraalVM-EE truffle-enterprise runtime.
    implementation("org.graalvm.polyglot:polyglot:$graalVersion")
    implementation("org.graalvm.js:js-language:$graalVersion")
    implementation("org.graalvm.truffle:truffle-runtime:$graalVersion")
}

configurations.all {
    // GraalVM Enterprise-only runtime; not needed and breaks stock-JDK embedding.
    exclude(group = "org.graalvm.truffle", module = "truffle-enterprise")
}

tasks.withType<JavaCompile> {
    options.release.set(21)
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveBaseName.set("paperscript-host")
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
