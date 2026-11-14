# Documentation Workflow

This project uses KDoc as the source of truth and Dokka as the API reference
generator. Treat documentation changes as part of the code change: when a class,
method, constructor, property, or behavior contract changes, update the KDoc in
the same commit.

## Provider Guides

The README contains the provider/platform and capability matrices. The website
setup guide for [OpenAI-compatible image servers](docs/openai-compatible.html)
covers complete API prefixes, optional authentication, LAN access, manual model
IDs and troubleshooting. Its consumed API contract lives in
[network/openai-compatible.openapi.json](network/openai-compatible.openapi.json).

The bundled [ChatGPT OAuth guide](docs/openai-oauth.html) describes device-code login, protected sessions and direct image generation on Android/iOS. The independent feature is `feature/openai-oauth`; app hosts select its DI registration. Its consumed contracts live in [network/openai-oauth.openapi.json](network/openai-oauth.openapi.json). Static contract gates run in Project Tests.

When changing provider behavior, keep the README, website and FAQ, privacy
description, localized store metadata and screenshot caption sources aligned
with the implementation.

## What To Document

Document declarations that explain module boundaries, public contracts, shared
KMP behavior, platform-specific `actual` behavior, side effects, and non-obvious
state transitions.

Use KDoc for:

- Classes, interfaces, objects, and data classes that are part of a module API.
- ViewModels, routers, interactors, gateways, repositories, data sources, and
  platform services.
- Top-level composables and helpers that are reused across screens.
- `expect` declarations in `commonMain` and `actual` declarations when Android
  and iOS behavior differs.
- Parameters or return values that are not obvious from the type.
- Error, cancellation, threading, persistence, or network behavior that callers
  must know about.

Avoid noisy KDoc for trivial private code whose behavior is already obvious from
the implementation. Prefer a short implementation comment inside the function
when the explanation is local and not part of the API reference.

## File-Level Guidance

Dokka renders declaration pages, not arbitrary file headers. When a file needs a
high-level explanation, document the main declaration that represents the file:

- A screen file should document the screen composable or screen state holder.
- A DI file should document the module/register function.
- A mapper file should document the mapper functions when they encode API
  quirks or compatibility rules.
- A platform file should document the `actual` implementation or factory.

If a file contains only private helpers, add KDoc only to the helpers whose
behavior would otherwise be hard to recover from the code.

## KDoc Style

Keep the first sentence short and useful. It becomes the summary in generated
Dokka pages.

Recommended shape:

```kotlin
/**
 * Loads generation history items for the gallery flow.
 *
 * The returned page is sorted from newest to oldest and contains only records
 * that have a persisted image reference.
 *
 * @param limit maximum number of records to load.
 * @param offset number of newest records to skip.
 * @return a page of gallery-ready generation results.
 *
 * @author Dmitriy Moroz
 */
class LoadGalleryPageUseCase(...)
```

Use links when the related declaration is in the codebase:

```kotlin
/**
 * Saves generated images and notifies [GalleryRouter] when the operation
 * should open gallery details.
 */
```

For KMP code:

```kotlin
/**
 * Opens an external URL using the current platform browser integration.
 *
 * Android uses an `Intent`; iOS uses the UIKit application URL opener.
 */
expect class ExternalUrlRouter
```

## Before Changing Signatures

When changing a signature or class shape:

1. Update the declaration KDoc.
2. Update `@param`, `@property`, and `@return` tags.
3. Remove documentation for deleted parameters or obsolete behavior.
4. Add platform notes when an `expect` or shared API gains different Android or
   iOS semantics.
5. Regenerate Dokka before publishing or merging release work.

## Generating API Documentation

Run Dokka from the repository root:

```bash
./gradlew dokkaGeneratePublicationHtml
```

The root Gradle build aggregates these documented modules:

- `:app`
- `:core:common`
- `:core:imageprocessing`
- `:core:localization`
- `:core:notification`
- `:core:ui`
- `:core:validation`
- `:data`
- `:demo`
- `:domain`
- `:feature:auth`
- `:feature:benchmark`
- `:feature:onnx`
- `:feature:mediapipe`
- `:feature:work`
- `:network`
- `:presentation`
- `:storage`

The generated HTML is written to:

```text
docs/docs/
```

The `decorateDokkaHtml` Gradle task runs automatically after
`dokkaGeneratePublicationHtml`. It adds SDAI navigation links, SEO metadata, and
canonical URLs to the generated pages.

Open the generated index locally from:

```text
docs/docs/index.html
```

## Publishing Notes

The `docs/` directory is the static website root. The generated API reference is
served below `/docs/`, so `docs/docs/index.html` becomes:

```text
https://sdai.moroz.cc/docs/
```

Do not hand-edit generated files under `docs/docs/` for API text changes. Update
KDoc in source code and regenerate Dokka instead. Hand edits in generated HTML
will be overwritten by the next Dokka run.

When adding a new Gradle module that should appear in the API reference, add it
to `documentedProjects` in the root `build.gradle.kts`.

## Pre-Release Checklist

Before a release or store upload:

- Run Optimize Imports in Android Studio:
  `Project right click -> Refactor -> Optimize Imports`.
- Run `./gradlew dokkaGeneratePublicationHtml` after KDoc changes.
- Review `git diff -- docs/docs` to make sure generated documentation reflects
  the intended API changes.
- Keep unrelated generated website or screenshot changes out of documentation
  commits unless they are intentional.
