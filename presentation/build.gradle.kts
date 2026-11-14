plugins {
    alias(libs.plugins.generic.kmp.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.shifthackz.aisdv1.presentation"
    testOptions.unitTests{
        all { test ->
            test.jvmArgs(
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
            )
        }
        isReturnDefaultValues = true
        isIncludeAndroidResources = true
    }
}

compose.resources {
    packageOfResClass = "com.shifthackz.aisdv1.presentation.generated.resources"
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "AiSdPresentation"
            isStatic = true
            export(project(":feature:bonsai"))
            export(project(":feature:coreml"))
            export(project(":feature:openai-oauth"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:ui"))
            implementation(project(":core:validation"))
            implementation(project(":data"))
            implementation(project(":demo"))
            implementation(project(":domain"))
            implementation(project(":feature:auth"))
            implementation(project(":feature:benchmark"))
            implementation(project(":feature:work"))
            api(project(":feature:bonsai"))
            api(project(":feature:coreml"))
            api(project(":feature:openai-oauth"))
            implementation(project(":network"))
            implementation(compose.animation)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(project(":core:ui"))
            implementation(project(":core:common"))
            implementation(project(":core:imageprocessing"))
            implementation(project(":core:localization"))
            implementation(project(":core:notification"))
            implementation(project(":core:validation"))
            implementation(project(":domain"))

            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.runtime)
            implementation(libs.androidx.compose.material3)
            implementation(libs.androidx.compose.material.icons)
            implementation(libs.androidx.compose.ui.graphics)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.compose.activity)
            implementation(libs.androidx.compose.viewmodel)
            implementation(libs.androidx.compose.navigation)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)

            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.exif)

            implementation(libs.google.material)
            implementation(libs.apache.stringutils)

            implementation(libs.kotlinx.serialization.json)
        }

        iosMain.dependencies {
            implementation(project(":feature:sdxl"))
        }

        androidUnitTest.dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.koin)
            implementation(libs.test.koin.junit)
            implementation(libs.test.mockk)
            implementation(libs.test.coroutines)
            implementation(libs.test.turbine)
            implementation(libs.test.roboelectric)
            implementation(libs.test.compose.junit)
        }
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.test.compose.manifest)
}
