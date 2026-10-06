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

val captureSeed = providers.gradleProperty("captureSeed").getOrElse("3")
val captureEvery = providers.gradleProperty("captureEvery").getOrElse("5")
val framesDir = layout.buildDirectory.dir("capture/frames")

val renderFrames by tasks.registering(JavaExec::class) {
    group = "showcase"
    description = "Renders simulation frames headlessly into build/capture/frames."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "ants.capture.CaptureMain"
    jvmArgs("-Djava.awt.headless=true")
    args("--seed", captureSeed, "--ticks", "3000", "--every", captureEvery,
        "--out", framesDir.get().asFile.path)
}

tasks.register("capture") {
    group = "showcase"
    description = "Renders frames, then builds media/colony.{mp4,gif} and media/colony-poster.png with ffmpeg."
    dependsOn(renderFrames)
    doLast {
        val frames = framesDir.get().asFile
        val ffmpeg = System.getenv("PATH").orEmpty().split(File.pathSeparator)
            .map { File(it, "ffmpeg") }.firstOrNull { it.canExecute() }
            ?: throw GradleException(
                "ffmpeg not found on PATH. Install it (macOS: brew install ffmpeg) and re-run " +
                "./gradlew capture. Rendered frames are kept in $frames")
        val media = file("media").apply { mkdirs() }
        val input = File(frames, "frame_%05d.png").path

        fun run(vararg command: String) {
            val process = ProcessBuilder(*command).inheritIO().start()
            if (process.waitFor() != 0) throw GradleException("Command failed: ${command.joinToString(" ")}")
        }

        run(ffmpeg.path, "-y", "-loglevel", "error", "-framerate", "30", "-i", input,
            "-c:v", "libx264", "-pix_fmt", "yuv420p", "-crf", "20", File(media, "colony.mp4").path)
        run(ffmpeg.path, "-y", "-loglevel", "error", "-framerate", "30", "-i", input,
            "-vf", "fps=15,scale=640:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=bayer:bayer_scale=4",
            File(media, "colony.gif").path)

        val last = frames.listFiles()!!.filter { it.name.matches(Regex("frame_\\d{5}\\.png")) }.maxBy { it.name }
        last.copyTo(File(media, "colony-poster.png"), overwrite = true)
        logger.lifecycle("Media written to ${media.path}")
    }
}
