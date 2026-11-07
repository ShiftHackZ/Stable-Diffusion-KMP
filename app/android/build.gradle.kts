import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.generic.application)
    alias(libs.plugins.generic.baseline.profm)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.shifthackz.aisdv1.app"

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    defaultConfig {
        applicationId = "com.shifthackz.aisdv1.app"
        versionName = libs.versions.versionName.get()
        versionCode = libs.versions.versionCode.get().toInt()

        buildConfigField("String", "IMAGE_CDN_URL", "\"https://random.imagecdn.app/\"")
        buildConfigField("String", "HUGGING_FACE_URL", "\"https://huggingface.co/\"")
        buildConfigField("String", "HUGGING_FACE_INFERENCE_URL", "\"https://router.huggingface.co/hf-inference/\"")
        buildConfigField("String", "HORDE_AI_URL", "\"https://stablehorde.net/\"")
        buildConfigField("String", "OPEN_AI_URL", "\"https://api.openai.com/\"")
        buildConfigField("String", "STABILITY_AI_URL", "\"https://api.stability.ai/\"")

        buildConfigField("String", "HORDE_AI_SIGN_UP_URL", "\"https://stablehorde.net/register\"")
        buildConfigField("String", "HUGGING_FACE_INFO_URL", "\"https://huggingface.co/docs/inference-providers/providers/hf-inference\"")
        buildConfigField("String", "OPEN_AI_INFO_URL", "\"https://platform.openai.com/api-keys\"")
        buildConfigField("String", "STABILITY_AI_INFO_URL", "\"https://platform.stability.ai/\"")
        buildConfigField("String", "FAL_AI_INFO_URL", "\"https://fal.ai/dashboard/keys\"")
        buildConfigField("String", "ARLI_AI_INFO_URL", "\"https://www.arliai.com/quick-start\"")
        buildConfigField("String", "UPDATE_API_URL", "\"https://sdai.moroz.cc\"")
        buildConfigField("String", "REPORT_API_URL", "\"https://sdai-report.moroz.cc\"")
        buildConfigField("String", "DEMO_MODE_API_URL", "\"https://sdai.moroz.cc\"")
        buildConfigField("String", "POLICY_URL", "\"https://sdai.moroz.cc/policy.html\"")
        buildConfigField("String", "DONATE_URL", "\"https://sdai.moroz.cc/donate.html\"")
        buildConfigField("String", "PROJECT_WEBSITE_URL", "\"https://sdai.moroz.cc\"")
        buildConfigField("String", "DEVELOPER_WEBSITE_URL", "\"https://moroz.cc\"")
        buildConfigField("String", "GITHUB_SOURCE_URL", "\"https://github.com/ShiftHackZ/SDAI-KMP\"")
        buildConfigField("String", "LICENSE_URL", "\"https://github.com/ShiftHackZ/SDAI-KMP/blob/master/LICENSE\"")
        buildConfigField("String", "SETUP_INSTRUCTIONS_URL", "\"https://github.com/AUTOMATIC1111/stable-diffusion-webui/wiki\"")
        buildConfigField("String", "SWARM_UI_INFO_URL", "\"https://github.com/mcmonkeyprojects/SwarmUI/tree/master/docs\"")

        resourceConfigurations += listOf("en", "ru", "uk", "tr", "zh", "de")
        manifestPlaceholders["excludePermissions"] = "true"
    }

    productFlavors {
        getByName("foss") {
            ndk {
                //noinspection ChromeOsAbiSupport
                abiFilters += "arm64-v8a"
            }
        }
    }

    val signingPropertiesFile = rootProject.file("app/keystore/signing.properties")
    if (signingPropertiesFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(signingPropertiesFile))
        val alias = props["keystore.alias"] as String
        val keystorePath = props["keystore"] as String
        signingConfigs {
            create("release") {
                storeFile = rootProject.file("app/$keystorePath")
                storePassword = props["keystore.password"] as String
                keyAlias = alias
                keyPassword = props["keystore.password"] as String
            }
        }
        println("[Signature] -> Build will be signed with signature: $alias")
        buildTypes.getByName("release").signingConfig = signingConfigs.getByName("release")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:imageprocessing"))
    implementation(project(":core:notification"))
    implementation(project(":core:validation"))
    implementation(project(":presentation"))
    implementation(project(":network"))
    implementation(project(":storage"))
    implementation(project(":domain"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:benchmark"))
    implementation(project(":feature:coreml"))
    implementation(project(":feature:onnx"))
    implementation(project(":feature:mediapipe"))
    implementation(project(":feature:sdxl"))
    implementation(project(":feature:work"))
    implementation(project(":data"))
    implementation(project(":demo"))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.timber)
    implementation(libs.shifthackz.catppuccin.splash)
    implementation(libs.shifthackz.catppuccin.legacy)
    implementation(libs.androidx.work.runtime)
}
