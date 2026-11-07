import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.dokka)
}

val documentedProjects = listOf(
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

dokka {
    moduleName.set("SDAI")
    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("docs/docs"))
    }
}

dependencies {
    documentedProjects.forEach { dokka(project(it)) }
}

subprojects {
    if (path in documentedProjects) {
        pluginManager.apply("org.jetbrains.dokka")

        dokka {
            moduleName.set(path.removePrefix(":").replace(':', '/'))
            modulePath.set(path.removePrefix(":").replace(':', '/'))
            dokkaSourceSets.configureEach {
                documentedVisibilities.set(
                    setOf(
                        VisibilityModifier.Public,
                        VisibilityModifier.Protected,
                        VisibilityModifier.Internal,
                        VisibilityModifier.Private,
                    )
                )
                sourceLink {
                    localDirectory.set(projectDir.resolve("src"))
                    remoteUrl.set(
                        URI(
                            "https://github.com/ShiftHackZ/SDAI-KMP/tree/master/" +
                                projectDir.relativeTo(rootProject.projectDir).path +
                                "/src"
                        )
                    )
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}

val decorateDokkaHtml by tasks.registering {
    group = "documentation"
    description = "Adds SDAI navigation and SEO metadata to generated Dokka HTML pages."

    doLast {
        val docsDir = layout.projectDirectory.dir("docs/docs").asFile
        if (!docsDir.exists()) return@doLast

        val brandingStyle = """
            <style id="sdai-docs-branding">
              .sdai-docs-links {
                display: inline-flex;
                align-items: center;
                gap: 10px;
                margin-left: auto;
              }
              .sdai-docs-links a {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                min-height: 34px;
                padding: 0 10px;
                border-radius: 8px;
                color: #fff;
                font-weight: 700;
                text-decoration: none;
              }
              .sdai-docs-links a:hover {
                background: rgba(255, 255, 255, 0.12);
              }
              .sdai-docs-links svg {
                width: 18px;
                height: 18px;
              }
              @media (max-width: 760px) {
                .sdai-docs-links {
                  width: 100%;
                  justify-content: flex-start;
                  margin-left: 0;
                  padding: 6px 0;
                }
                .sdai-docs-links a {
                  padding: 0 6px;
                }
              }
            </style>
        """.trimIndent()

        val headerLinks = """
            <nav class="sdai-docs-links" aria-label="SDAI documentation links">
              <a href="https://sdai.moroz.cc/">Main site</a>
              <a href="https://t.me/sdai_app" target="_blank" rel="noopener noreferrer" aria-label="Telegram">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M21.86 4.58 18.62 19.84c-.24 1.08-.88 1.34-1.78.83l-4.92-3.63-2.37 2.28c-.26.26-.48.48-.98.48l.35-5.02 9.14-8.26c.4-.35-.09-.55-.61-.2L6.15 13.43l-4.87-1.52c-1.06-.33-1.08-1.06.22-1.57L20.55 3c.88-.33 1.65.2 1.31 1.58Z"/></svg>
                <span>Telegram</span>
              </a>
              <a href="https://discord.gg/jzdR9m8Ves" target="_blank" rel="noopener noreferrer" aria-label="Discord">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M19.54 5.26A18.1 18.1 0 0 0 15.04 3.86c-.19.34-.42.8-.57 1.16a16.7 16.7 0 0 0-4.99 0 12.3 12.3 0 0 0-.58-1.16 18 18 0 0 0-4.5 1.4C1.55 9.49.78 13.61 1.16 17.68a18.3 18.3 0 0 0 5.51 2.79c.44-.6.84-1.24 1.17-1.91-.64-.24-1.25-.54-1.82-.89.15-.11.3-.23.44-.35a12.96 12.96 0 0 0 11.08 0l.44.35c-.58.35-1.19.65-1.83.89.34.67.73 1.31 1.17 1.91a18.2 18.2 0 0 0 5.52-2.79c.46-4.72-.78-8.81-3.3-12.42ZM8.68 15.18c-1.07 0-1.95-.99-1.95-2.2 0-1.2.86-2.19 1.95-2.19 1.08 0 1.96.99 1.95 2.19 0 1.21-.87 2.2-1.95 2.2Zm6.64 0c-1.07 0-1.95-.99-1.95-2.2 0-1.2.86-2.19 1.95-2.19 1.09 0 1.96.99 1.95 2.19 0 1.21-.86 2.2-1.95 2.2Z"/></svg>
                <span>Discord</span>
              </a>
            </nav>
        """.trimIndent()

        docsDir.walkTopDown()
            .filter { it.isFile && it.extension == "html" }
            .forEach { file ->
                val relativePath = docsDir.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/')
                val canonicalPath = if (relativePath == "index.html") "" else relativePath
                val canonicalUrl = "https://sdai.moroz.cc/docs/$canonicalPath"
                var html = file.readText()

                if (relativePath == "index.html") {
                    html = html.replace(
                        "<title>All modules</title>",
                        "<title>SDAI Documentation - Kotlin API Reference</title>"
                    )
                }

                if (!html.contains("sdai-docs-seo")) {
                    val seo = """
                        <meta id="sdai-docs-seo" name="description" content="Dokka API documentation for SDAI Kotlin Multiplatform, Android, iOS, and shared modules.">
                        <link rel="canonical" href="$canonicalUrl">
                        <meta property="og:title" content="SDAI Documentation">
                        <meta property="og:description" content="Kotlin API documentation for SDAI Android, iOS, and shared modules.">
                        <meta property="og:url" content="$canonicalUrl">
                        <meta property="og:site_name" content="SDAI">
                        <meta property="og:type" content="website">
                    """.trimIndent()
                    html = html.replace("</head>", "$seo\n$brandingStyle\n</head>")
                }

                if (!html.contains("SDAI documentation links")) {
                    html = html.replace(
                        Regex("""(<div class="library-version" id="library-version">\s*</div>)"""),
                        "$1\n$headerLinks"
                    )
                }

                file.writeText(html)
            }
    }
}

tasks.named("dokkaGeneratePublicationHtml") {
    finalizedBy(decorateDokkaHtml)
}
