import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.GroovyObject
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import kotlin.concurrent.thread

plugins {
    id("java-library")
    id("maven-publish")
    kotlin("jvm") version "1.9.0"
    id("com.jfrog.bintray")
    id("com.jfrog.artifactory")
    id("org.jetbrains.dokka")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("eclipse")
    id("com.gtnewhorizons.retrofuturagradle") version "1.3.16"
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

val branch = prop("branch") ?: "git rev-parse --abbrev-ref HEAD".execute(rootDir.absolutePath).lines().last()
logger.info("On branch $branch")

version = "${branch.replace('/', '-')}-".takeUnless { prop("mc_version")?.contains(branch) == true }.orEmpty() + prop("mod_version") + "." + prop("build_number")
description = "A library for the TeamWizardry mods"
base.archivesBaseName = "${prop("mod_name")}-${prop("mc_version")}"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.AZUL)
    }
    withSourcesJar()
}

minecraft {
    mcVersion.set("1.12.2")

    mcpMappingChannel.set("stable")
    mcpMappingVersion.set("39")

    username.set("Developer")

    val args = mutableListOf("-ea:${project.group}")
    args.add("-Dfml.coreMods.load=com.teamwizardry.librarianlib.asm.LibLibCorePlugin")

    extraRunJvmArguments.add("-ea:${project.group}")
}

// Generate a group.archives_base_name.Tags class
tasks.injectTags.configure {
    // Change Tags class' name here:
    outputClassName.set("${project.group}.${base.archivesBaseName}.Tags")
}

sourceSets["main"].allSource.srcDir("src/example/java")
sourceSets["main"].allSource.srcDir("src/api/java")
sourceSets["main"].resources.srcDir("src/example/resources")

repositories {
    mavenCentral()
    maven { url = uri("https://files.minecraftforge.net/maven") }
    maven {
        name = "Bluexin repo"
        url = uri("https://maven.bluexin.be/repository/snapshots/")
    }
    maven {
        name = "Jitpack.io"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "CleanroomMC Maven"
        url = uri("https://maven.cleanroommc.com")
    }
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
    }
    mavenLocal()
}

dependencies {
    api("net.shadowfacts:Forgelin:1.8.0")
    compileOnly("net.shadowfacts:Forgelin:1.8.0")
    implementation("curse.maven:forgelin-continuous-456403:4635770")

// shade("org.magicwerk:brownies-collections:0.9.13")
    implementation("org.magicwerk:brownies-collections:0.9.13")

// shade("com.ibm.icu:icu4j:63.1")
// shade("org.msgpack:msgpack-core:0.8.16")
// shade("com.github.thecodewarrior:bitfont:b8251e7ba0")
    implementation("com.ibm.icu:icu4j:63.1")
    implementation("org.msgpack:msgpack-core:0.8.16")
    implementation("com.github.thecodewarrior:bitfont:-SNAPSHOT")
}

val sourceJar = tasks.register("sourceJar", Jar::class) {
    from(
            tasks["sourceMainJava"],
            tasks["sourceMainKotlin"],
            tasks["sourceTestJava"],
            tasks["sourceTestKotlin"]
    )
    include("**/*.kt", "**/*.java", "**/*.scala")
    archiveClassifier.set("sources") // Use archiveClassifier to set the classifier
    includeEmptyDirs = false
}

tasks {
    getByName<Jar>("jar") {
        exclude("*/**/librarianlibtest/**", "*/**/librarianlib.test/**")
        archiveClassifier.set("fat")

        manifest {
            attributes("FMLCorePluginContainsFMLMod" to true)
        }
    }

    getByName<ProcessResources>("processResources") {
        val props = mapOf(
                "version" to project.version,
                "mcversion" to minecraft.version
        )

        inputs.properties(props)

        from(sourceSets["main"].resources.srcDirs) {
            include("mcmod.info")
            expand(props)
        }

        from(sourceSets["main"].resources.srcDirs) {
            exclude("mcmod.info")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
            freeCompilerArgs += "-Xjvm-default=enable"
            freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
        }
    }
}

val dokka by tasks.getting(DokkaTask::class) {
    outputDirectory = "$buildDir/docs"
    outputFormat = "javadoc"
    jdkVersion = 8
    sourceDirs =
            sourceSets["main"].allSource.srcDirs +
                    sourceSets["test"].allSource.srcDirs // Adjust the source sets as needed

    includes = listOf("src/dokka/kotlin-dsl.md")
    doFirst {
        file(outputDirectory).deleteRecursively()
    }
}

val javadocJar by tasks.creating(Jar::class) {
    from(dokka.outputs)
    archiveClassifier.set("javadoc")
}

val deobfJar by tasks.creating(Jar::class) {
    from(sourceSets["main"].output)
}

val reobfJar = tasks.named<com.gtnewhorizons.retrofuturagradle.mcp.ReobfuscatedJar>("reobfJar") {
    // Configure the 'reobfJar' task here
    // For example, specify the input and output files:

    from(sourceSets["main"].output)
    into("$buildDir/libs/") // Specify the output directory
    include("**/*.class")
}

fun String.execute(wd: String? = null, ignoreExitCode: Boolean = false): String =
        split(" ").execute(wd, ignoreExitCode)

fun List<String>.execute(wd: String? = null, ignoreExitCode: Boolean = false): String {
    val process = ProcessBuilder(this)
            .also { pb -> wd?.let { pb.directory(File(it)) } }
            .start()
    var result = ""
    val errReader = thread { process.errorStream.bufferedReader().forEachLine { logger.error(it) } }
    val outReader = thread {
        process.inputStream.bufferedReader().forEachLine { line ->
            logger.debug(line)
            result += line
        }
    }
    process.waitFor()
    outReader.join()
    errReader.join()
    if (process.exitValue() != 0 && !ignoreExitCode) error("Non-zero exit status for `$this`")
    return result
}

fun hasProp(name: String): Boolean = extra.has(name)

fun prop(name: String): String? = extra.properties[name] as? String

fun coroutine(module: String): Any =
        "org.jetbrains.kotlinx:kotlinx-coroutines-$module:${prop("coroutinesVersion")}"
