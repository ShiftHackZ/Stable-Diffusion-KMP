pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "SDAI"

val modules = listOf(
        ":app",
        ":core:common",
        ":core:imageprocessing",
        ":core:localization",
        ":core:notification",
        ":core:ui",
        ":core:validation",
        ":data",
        ":demo",
        ":domain",
        ":feature:auth",
        ":feature:openai-oauth",
        ":feature:benchmark",
        ":feature:bonsai",
        ":feature:coreml",
        ":feature:onnx",
        ":feature:mediapipe",
        ":feature:sdxl",
        ":feature:work",
        ":network",
        ":presentation",
        ":storage",
)

include(modules)

project(":app").projectDir = file("app/android")
