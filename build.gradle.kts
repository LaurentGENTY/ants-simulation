plugins {
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("io.jbotsim:jbotsim-all:1.2.0")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "ants.AntHillMain"
}

tasks.test {
    useJUnitPlatform()
    systemProperty("java.awt.headless", "true")
}
