val dokka_version: String by settings
val forgegradle_version: String by settings
val bintray_version: String by settings
val artifactory_version: String by settings
val abc_version: String by settings

pluginManagement {
    repositories {
        maven {
            // RetroFuturaGradle
            name = "GTNH Maven"
            url = uri("http://jenkins.usrv.eu:8081/nexus/content/groups/public/")
            isAllowInsecureProtocol = true
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroup("com.gtnewhorizons.retrofuturagradle")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) useVersion("1.9.0")
            else when (requested.id.id) {
                "net.minecraftforge.gradle" -> useModule("net.minecraftforge.gradle:ForgeGradle:6.+")
                "org.jetbrains.dokka" -> useVersion("0.9.17")
                "com.jfrog.bintray" -> useVersion("1.8.4")
                "com.jfrog.artifactory" -> useVersion("4.7.5")
            }
        }
    }
}

rootProject.name = "LibrarianLib"