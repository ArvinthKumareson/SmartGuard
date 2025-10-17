pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.9.22"
    }

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "SmartGuard"
include(":app")