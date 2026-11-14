# SDAI

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.21442838.svg)](https://doi.org/10.5281/zenodo.21442838)
![Google Play](https://img.shields.io/endpoint?color=blue&logo=google-play&logoColor=white&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dcom.shifthackz.aisdv1.app%26l%3DGoogle%2520Play%26m%3D%24version)
![F-Droid](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Ff-droid.org%2Fapi%2Fv1%2Fpackages%2Fcom.shifthackz.aisdv1.app.foss&query=%24.packages%5B0%5D.versionName&label=F-Droid&link=https%3A%2F%2Ff-droid.org%2Fpackages%2Fcom.shifthackz.aisdv1.app.foss%2F)
[![App Store](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fitunes.apple.com%2Flookup%3Fid%3D6778314183%26country%3Dus&query=%24.results%5B0%5D.version&label=App%20Store&logo=appstore&logoColor=white&color=blue)](https://apps.apple.com/us/app/sdai-ai-image-generator/id6778314183)

[Website](https://sdai.moroz.cc) | [Google Play](https://play.google.com/store/apps/details?id=com.shifthackz.aisdv1.app) | [F-Droid](https://f-droid.org/packages/com.shifthackz.aisdv1.app.foss) | [App Store](https://apps.apple.com/us/app/sdai-ai-image-generator/id6778314183) | [Nightly Build](https://github.com/ShiftHackZ/SDAI-KMP/releases/download/nightly/sdai-full-nightly.apk) | [Telegram](https://t.me/sdai_app) | [Discord](https://discord.gg/jzdR9m8Ves) | [4PDA](https://4pda.to/forum/index.php?showtopic=1082639)

<p>
  <a href="https://play.google.com/store/apps/details?id=com.shifthackz.aisdv1.app"><img src="docs/assets/badge-google-play.svg" alt="Get it on Google Play" height="54"></a>
  <a href="https://f-droid.org/packages/com.shifthackz.aisdv1.app.foss"><img src="docs/assets/badge-fdroid.svg" alt="Get it on F-Droid" height="54"></a>
  <a href="https://apps.apple.com/us/app/sdai-ai-image-generator/id6778314183"><img src="docs/assets/badge-app-store-readme.png" alt="Download on the App Store" height="54"></a>
</p>

SDAI is an independent, open-source AI image generation client for Android and iOS. It brings self-hosted AUTOMATIC1111, SwarmUI, and OpenAI-compatible image servers, hosted image APIs, crowdsourced generation, and supported on-device generation into one mobile app.

**SDAI is not affiliated with, endorsed by, or sponsored by Stability AI or the Stable Diffusion project.** References to third-party models, software and services describe compatibility only.

No ads. No telemetry. No lock-in to a single provider.

## Project Documentation

Root-level Markdown documents:

- [Documentation](DOCUMENTATION.md)
- [Screenshot generation](SCREENSHOT_GENERATION.md)
- [Nightly Android builds](NIGHTLY_BUILDS.md)
- [Git workflow](GIT_WORKFLOW.md)
- [Code of conduct](CODE_OF_CONDUCT.md)

## Why SDAI

- Choose the backend that fits the moment: your own AUTOMATIC1111, SwarmUI, or OpenAI-compatible image server, AI Horde, Hugging Face, OpenAI, ChatGPT OAuth, Stability AI, Fal.ai, ArliAI, or local diffusion where the platform supports it.
- Generate with familiar controls: prompts, negative prompts where supported, seed, steps, CFG scale, image size, model selectors, LoRA, embeddings, and more.
- Use one shared mobile experience across Android and iOS for remote generation workflows.
- Work locally when privacy or connectivity matters with Android ONNX, MediaPipe, stable-diffusion.cpp SDXL, Android Local Diffusion PrismML Bonsai, iOS Silicon Diffusion Core ML, or iOS Silicon Diffusion PrismML Bonsai.
- Keep your creations in a local gallery with image details, zoom, sharing, native platform save flows, and zip export.
- Check local-device fit before heavy runs with the on-device benchmark, then review storage and network usage from Settings.
- Stay in control: the project is open source and the app does not include ads or telemetry.

## Screenshots

<img src="docs/screenshots/site/readme-row-1.png" alt="SDAI screenshots: generation result, prompt controls, and provider selection" width="100%">
<img src="docs/screenshots/site/readme-row-2.png" alt="SDAI screenshots: image gallery, app settings, and generation details" width="100%">

## Provider and Platform Matrix

Android builds are distributed in three flavors:

- `playstore`: Google Play build.
- `full`: full GitHub/release build.
- `foss`: F-Droid friendly build.

iOS uses the shared mobile experience with remote generation providers, Silicon Diffusion Core ML, and Silicon Diffusion PrismML Bonsai for on-device generation on supported devices. Android also exposes Local Diffusion PrismML Bonsai on builds where the native runtime is packaged, with device suitability checked through setup and benchmark flows.

| Provider / backend                         | What it connects to                                   | iOS    | Android `playstore` | Android `full` | Android `foss` | Notes                                                                                                                                                                                                                                |
| ------------------------------------------ | ----------------------------------------------------- | ------ | ------------------- | -------------- | -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| AUTOMATIC1111 WebUI                        | Your own AUTOMATIC1111 API server                     | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Own server, demo mode, txt2img, img2img, inpaint, models, LoRA, embeddings, hypernetworks.                                                                                                                                           |
| SwarmUI                                    | Your own SwarmUI server                               | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Own server with SwarmUI model, LoRA, and embeddings discovery.                                                                                                                                                                       |
| OpenAI-compatible                          | Any server or proxy implementing the OpenAI image API | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Stable. Full API prefix, optional bearer key, arbitrary model ID, txt2img and sequential batches. Server determines supported models and sizes.                                                                                      |
| AI Horde                                   | Crowdsourced Stable Horde workers                     | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Works with the default anonymous key or your own Horde API key.                                                                                                                                                                      |
| Hugging Face Inference API                 | Hosted Hugging Face image models                      | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Requires a Hugging Face API key and selected model.                                                                                                                                                                                  |
| OpenAI Images API                          | OpenAI image generation with GPT Image models         | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Requires an OpenAI API key.                                                                                                                                                                                                          |
| ChatGPT OAuth                              | Direct ChatGPT account image generation               | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Beta. Device-code login, gpt-image-2, txt2img and sequential batches. Unofficial integration; account/workspace device login must be enabled.                                                                                        |
| Stability AI                               | Stability AI / DreamStudio image API                  | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Requires a Stability AI API key and engine selection.                                                                                                                                                                                |
| Fal.ai                                     | Fal.ai hosted image generation endpoints              | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Requires a Fal.ai API key. Supports compatible FLUX txt2img/img2img endpoints through the shared generation form.                                                                                                                    |
| ArliAI                                     | ArliAI SDNext-compatible hosted image API             | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Alpha provider. Requires an ArliAI API key. Supports txt2img/img2img, checkpoint discovery, sampler selection, steps, CFG scale, negative prompt, and batch generation.                                                              |
| Local Diffusion: Microsoft ONNX Runtime    | On-device ONNX model inference                        | 🔴 No  | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Android-only txt2img. Custom local model paths are available outside the Play build.                                                                                                                                                 |
| Local Diffusion: Google AI MediaPipe       | On-device MediaPipe image generator                   | 🔴 No  | 🟢 Yes              | 🟢 Yes         | 🔴 No          | Android-only txt2img. Excluded from the FOSS flavor.                                                                                                                                                                                 |
| Local Diffusion: stable-diffusion.cpp SDXL | On-device SDXL-compatible model inference             | 🔴 No  | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | Android-only txt2img through stable-diffusion.cpp. Supports catalog GGUF/safetensors/ckpt models, CPU/OpenCL/Vulkan backend selection, and custom local model paths outside the Play build.                                          |
| Silicon Diffusion Core ML                  | On-device Core ML image generation runtime            | 🟢 Yes | 🔴 No               | 🔴 No          | 🔴 No          | iOS-only txt2img and img2img with explicit downloadable/imported Core ML model assets. SDXL catalog entries are disabled until device-gated QA is stable.                                                                            |
| Local / Silicon Diffusion PrismML Bonsai   | On-device PrismML Bonsai Image 4B runtime             | 🟢 Yes | 🟢 Yes              | 🟢 Yes         | 🟢 Yes         | iOS uses the Silicon Diffusion MLX runtime and is marked beta. Android uses the Local Diffusion NDK/ART runtime, is marked experimental, supports Auto/CPU/Vulkan backend selection, and should be device-checked before heavy runs. |

## AI Feature Matrix

| AI-specific feature                      | Supported providers                                                                                                                                                                                                                | Notes                                                                                                                                                                                                                                                                                   |
| ---------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Text to image                            | AUTOMATIC1111, SwarmUI, OpenAI-compatible, AI Horde, Hugging Face, OpenAI, ChatGPT OAuth, Stability AI, Fal.ai, ArliAI, Local ONNX, Local MediaPipe, Local SDXL, Local/Silicon Diffusion PrismML Bonsai, Silicon Diffusion Core ML | Core generation path exists for every provider exposed by the current platform/build.                                                                                                                                                                                                   |
| Image to image                           | AUTOMATIC1111, SwarmUI, AI Horde, Hugging Face, Stability AI, Fal.ai, ArliAI, Silicon Diffusion Core ML                                                                                                                            | OpenAI, ChatGPT OAuth, OpenAI-compatible, and Android local diffusion providers are txt2img-only in the app. Core ML img2img requires a compatible downloaded model archive.                                                                                                            |
| Inpaint mask controls                    | AUTOMATIC1111                                                                                                                                                                                                                      | Mask image, mask blur, mask mode, masked content, inpaint area, and only-masked padding are mapped to the A1111 img2img API.                                                                                                                                                            |
| Negative prompt                          | AUTOMATIC1111, SwarmUI, Hugging Face, Stability AI, ArliAI, Local ONNX, Local SDXL, Local/Silicon Diffusion PrismML Bonsai, Silicon Diffusion Core ML                                                                              | Horde, OpenAI, ChatGPT OAuth, OpenAI-compatible, and MediaPipe flows do not expose/send a negative prompt.                                                                                                                                                                              |
| Batch generation                         | AUTOMATIC1111, SwarmUI, OpenAI-compatible, AI Horde, Hugging Face, OpenAI, ChatGPT OAuth, Stability AI, Fal.ai, ArliAI                                                                                                             | OpenAI-compatible runs one image request at a time; Fal.ai uses native `num_images`; ArliAI uses the SDNext-compatible batch size field; local providers are treated as single-image generation flows.                                                                                  |
| Model or engine selection                | AUTOMATIC1111, SwarmUI, OpenAI-compatible, Hugging Face, OpenAI, Stability AI, Fal.ai, ArliAI, Local ONNX, Local MediaPipe, Local SDXL, Local/Silicon Diffusion PrismML Bonsai, Silicon Diffusion Core ML                          | Depending on provider, this selects an SD checkpoint, SwarmUI model, arbitrary OpenAI-compatible model ID, HF model, OpenAI model, Stability engine, Fal.ai endpoint, ArliAI checkpoint, or local model. Android SDXL and Bonsai also expose runtime backend selection where supported. |
| LoRA picker                              | AUTOMATIC1111, SwarmUI                                                                                                                                                                                                             | Remote LoRA lists are fetched from the active compatible server.                                                                                                                                                                                                                        |
| Textual inversion / embeddings picker    | AUTOMATIC1111, SwarmUI                                                                                                                                                                                                             | Remote embeddings are fetched from the active compatible server.                                                                                                                                                                                                                        |
| Hypernetwork picker                      | AUTOMATIC1111                                                                                                                                                                                                                      | Hypernetwork discovery is implemented for A1111.                                                                                                                                                                                                                                        |
| Sampler selection                        | AUTOMATIC1111, Stability AI, ArliAI, Local SDXL                                                                                                                                                                                    | A1111 samplers are fetched from the server; Stability AI and ArliAI use provider-specific sampler lists; Local SDXL maps compatible samplers to stable-diffusion.cpp.                                                                                                                   |
| Restore faces                            | AUTOMATIC1111                                                                                                                                                                                                                      | Exposed only for A1111 generation.                                                                                                                                                                                                                                                      |
| OpenAI model, size, and quality          | OpenAI                                                                                                                                                                                                                             | Uses current GPT Image model options exposed by the Images API.                                                                                                                                                                                                                         |
| OpenAI-compatible model and size         | OpenAI-compatible                                                                                                                                                                                                                  | Editable model ID and positive width/height; server compatibility determines accepted values. No quality, negative prompt, sampler, steps, CFG or img2img controls are sent.                                                                                                            |
| Stability style preset and clip guidance | Stability AI                                                                                                                                                                                                                       | Passed to Stability AI requests when selected.                                                                                                                                                                                                                                          |
| NSFW flag                                | AI Horde, Fal.ai, Silicon Diffusion Core ML                                                                                                                                                                                        | Exposed for Horde requests, mapped to Fal.ai safety-checker settings, and mapped to the local Core ML safety checker.                                                                                                                                                                   |
| Offline generation                       | Local ONNX, Local MediaPipe, Local SDXL, Local/Silicon Diffusion PrismML Bonsai, Silicon Diffusion Core ML                                                                                                                         | Runs after the selected local model is available on the current platform.                                                                                                                                                                                                               |
| On-device benchmark                      | Local ONNX, Local MediaPipe, Local SDXL, Local/Silicon Diffusion PrismML Bonsai, Silicon Diffusion Core ML                                                                                                                         | Runs a safe inference-like CPU and memory workload, includes Android Bonsai Vulkan compute probing, stores the latest local result, and recommends local provider settings without loading model files or starting AI runtimes.                                                         |
| Generation interrupt                     | AUTOMATIC1111, AI Horde, Local ONNX, Local SDXL, Local/Silicon Diffusion PrismML Bonsai, Silicon Diffusion Core ML                                                                                                                 | Other providers rely on request completion when no platform-level interrupt is exposed.                                                                                                                                                                                                 |

## Core Workflow

### Generate

- Txt2Img for all available providers.
- Img2Img for providers that support image input.
- Inpaint for A1111-powered Img2Img.
- Prompt tagging, optional advanced controls, seed handling, steps, CFG scale, size controls, and provider-specific model options.

### Save and Review

- Local in-app gallery for generated images.
- Detail screen with zoom, generation metadata, and sharing.
- Optional auto-save of results.
- Native platform save/share flows, including Android MediaStore and iOS Photos/share sheet where available.
- Export one image or the full gallery as a zip archive.

### Configure

- Provider setup from first launch or settings.
- Provider search, type/readiness/tag filters, and sorting for faster backend selection.
- Server URL and credentials for own-server providers.
- API keys for hosted providers.
- Local model selection and download flow for supported local diffusion providers.
- Runtime backend selectors for Android local providers where available, including SDXL Auto/CPU/OpenCL/Vulkan and Bonsai Auto/CPU/Vulkan.
- Hardware benchmark in AI Settings for local providers, including device score, acceleration availability, Android Bonsai Vulkan compute support, estimated generation time, and recommended local settings.
- Server availability monitoring for compatible own-server modes.
- Storage usage and network usage screens for gallery/cache/model files and provider traffic counters.

## Setup

### Option 1: AUTOMATIC1111 WebUI

Use this if you already run [AUTOMATIC1111 WebUI](https://github.com/AUTOMATIC1111/stable-diffusion-webui) locally, on a server, or in a notebook environment.

1. Follow the setup instructions in the WebUI repository.
2. Start WebUI with API access enabled, for example with `--api --listen`.
3. Copy the reachable server URL.
4. Open SDAI setup, choose AUTOMATIC1111, enter the URL, and connect.

Demo mode is available from the setup screen if you want to explore the app without connecting to a real generator.

### Option 2: SwarmUI

Use this if your generation environment is powered by [SwarmUI](https://github.com/mcmonkeyprojects/SwarmUI). Start SwarmUI in server mode, copy the reachable URL, then choose SwarmUI during SDAI setup.

### Option 3: OpenAI-compatible

Use this Stable provider on Android (all flavors) or iOS with an image server or separately running proxy that implements the OpenAI Images API.

1. Start the server and make its image API reachable from your phone.
2. Choose **OpenAI-compatible**, immediately after SwarmUI in the default provider list.
3. Enter the complete API prefix, for example `http://192.168.1.10:10531/v1` or `https://images.example.com/proxy/v1`. SDAI appends `models` and `images/generations`; include `/v1` only if your server uses it.
4. Enter an API key only when the server requires bearer authentication; otherwise leave it empty.
5. Connect, enter the server's image model ID, choose supported dimensions and a batch size, then generate.

For a server on another computer in the same LAN, use that computer's LAN IP and enable its LAN listener. The phone's `localhost` address refers to the phone. Prefer HTTPS for connections outside a trusted LAN.

SDAI supports text-to-image, sequential batches and image responses containing Base64 data or a downloadable URL. A missing `/models` route (404/405) produces an unverified notice and still permits manual model entry; a successful connection does not prove that a model can generate images. Img2img and SD-specific controls are unavailable for this source.

A separately hosted [openai-oauth](https://github.com/EvanZhouDev/openai-oauth) proxy is one example; its ChatGPT sign-in runs outside SDAI. This provider keeps its URL, optional key and model independent of the official OpenAI provider. See the [complete setup and troubleshooting guide](https://sdai.moroz.cc/openai-compatible.html) and [consumed API contract](network/openai-compatible.openapi.json).

### Option 4: AI Horde

[AI Horde](https://stablehorde.net/) is a crowdsourced distributed cluster of image generation workers. SDAI can use the default anonymous key (`0000000000`) or your own key from [stablehorde.net/register](https://stablehorde.net/register).

### Option 5: Hugging Face Inference API

[Hugging Face Inference API](https://huggingface.co/docs/api-inference/index) lets SDAI call hosted public or private image models. Create an API key in [Hugging Face account settings](https://huggingface.co/settings/tokens), then select the Hugging Face provider in SDAI.

### Option 6: OpenAI

OpenAI image generation in SDAI uses GPT Image models through the Images API. Create an API key in [OpenAI API key settings](https://platform.openai.com/api-keys), then select OpenAI in SDAI.

### ChatGPT OAuth (Beta)

Choose **ChatGPT OAuth** immediately after OpenAI in Configuration on Android or iOS. Enable device-code login in ChatGPT account security settings if needed, tap **Sign in with ChatGPT**, open the trusted sign-in page and enter the code shown by SDAI. Return to the app and tap **Connect** once signed in. Generate with `gpt-image-2`, positive width/height and sequential batches. No API key, external proxy or desktop credential file is required.

Sessions use Android Keystore-backed storage and iOS Keychain. Sign out in provider configuration to remove the local session. The integration uses an unofficial backend; model access, limits and service changes depend on OpenAI. Image editing, chat and Stable Diffusion controls are unsupported. See the [provider guide](https://sdai.moroz.cc/openai-oauth.html).

### Option 7: Stability AI

[Stability AI](https://platform.stability.ai/) support uses the Stability image API. Create an API key on the [Stability AI keys page](https://platform.stability.ai/account/keys), then select Stability AI and choose an engine in SDAI.

### Option 8: Fal.ai

[Fal.ai](https://fal.ai/) support uses the Fal.ai queue API for compatible FLUX text-to-image and image-to-image endpoints. Create an API key in the [Fal.ai dashboard](https://fal.ai/dashboard/keys), then select Fal.ai in SDAI and choose the endpoint from the shared generation form.

### Option 9: ArliAI

[ArliAI](https://www.arliai.com/quick-start) support uses the ArliAI SDNext-compatible image API. Create an ArliAI API key, select ArliAI in SDAI, connect, and choose the checkpoint from the shared generation form after model discovery completes.

### Option 10: Local Diffusion with Microsoft ONNX Runtime

Use this on Android for on-device txt2img generation. Download or provide a compatible ONNX local diffusion model, select it in setup, and generate without sending prompts to a remote service.

### Option 11: Local Diffusion with Google AI MediaPipe

Use this on Android for on-device txt2img generation through Google AI MediaPipe. This provider is available only in `playstore` and `full` Android flavors.

### Option 12: Local SDXL with stable-diffusion.cpp

Use this on Android for on-device SDXL-compatible txt2img generation through stable-diffusion.cpp. Download a supported model from the in-app SDXL catalog or provide a compatible local single-file model outside the Play build, then choose the runtime backend from Auto, CPU, OpenCL, or Vulkan. Mobile SDXL is memory-heavy, so the catalog starts with compact/quantized GGUF options such as SSD-1B / LCM-style models before larger desktop-class SDXL files.

### Option 13: Silicon Diffusion Core ML

Use this on iOS for on-device image generation through Core ML. Download a supported Core ML model from the in-app catalog or import a compatible local model package, select it during setup, and generate without sending prompts or source images to a remote service.

The first-party catalog intentionally starts with Apple/Hugging Face Stable Diffusion 1.x and 2.x palettized Core ML archives. SDXL archives are not exposed by default until memory and execution-plan compatibility are stable across a tested device matrix.

### Option 14: PrismML Bonsai Image 4B

Use this for on-device text-to-image generation with PrismML Bonsai Image 4B. Download a supported Bonsai Ternary or Binary model archive from the in-app catalog, select it during setup, and generate without sending prompts to a remote service.

On iOS the provider is displayed as Silicon Diffusion PrismML Bonsai and uses the MLX runtime. On Android it is displayed as Local Diffusion PrismML Bonsai, uses the custom NDK/ART runtime, and exposes Auto, CPU, and Vulkan backend selection from the generation form.

iOS support is beta. Android support is experimental and device-sensitive: run the benchmark/setup checks before long generations, especially on low-RAM devices.

## Build Flavor Notes

Android flavor availability is driven by the Gradle flavor configuration and runtime provider filtering. Most network providers are available everywhere; Google AI MediaPipe is intentionally unavailable in `foss`. Local SDXL through stable-diffusion.cpp and Local Diffusion PrismML Bonsai are available in `playstore`, `full`, and `foss`; their model catalogs are shared, and model files are downloaded or imported by the user rather than bundled into the app. The Play build avoids custom local model path selection for local diffusion models because broad file access is not generally accepted for Google Play distribution.

The iOS app is not split into Android-style flavors. It uses the shared mobile UI, remote-provider stack, Silicon Diffusion Core ML, and Silicon Diffusion PrismML Bonsai. Android ONNX, MediaPipe, SDXL, and Local Diffusion PrismML Bonsai local runtimes remain Android-specific.

For a historical overview of flavor policy, see the project wiki page: [Build flavor difference](https://github.com/ShiftHackZ/SDAI-KMP/wiki/Build-flavor-difference).

## Legacy Android 0.x.x Versions

The old Android-only `0.x.x` release line is no longer maintained. It remains available for archival and reference purposes in the
[`archive/version-0.x.x`](https://github.com/ShiftHackZ/SDAI-KMP/tree/archive/version-0.x.x) branch.

## Supported Languages

SDAI uses the language provided by the OS when a translation is available.

| Language             | Since version | Status     |
| -------------------- | ------------- | ---------- |
| English              | 0.1.0         | Translated |
| Ukrainian            | 0.1.0         | Translated |
| Turkish              | 0.4.1         | Translated |
| Russian              | 0.5.5         | Translated |
| Chinese (Simplified) | 0.6.2         | Translated |
| German               | 1.2.0         | Translated |

Translation contributions are welcome.

## Donate

SDAI is open source and provided with no warranty. You are welcome to use it for free.

If you find the project useful and want to support the work, please check the current donation status at
[sdai.moroz.cc/donate.html](https://sdai.moroz.cc/donate.html).

## Citation

Machine-readable citation metadata is available in [`CITATION.cff`](CITATION.cff).

If you mention SDAI in research, articles, benchmarks, tutorials, app collections, or public project documentation, please cite it as:

> SDAI, version 1.2.0, an open-source cross-platform AI image generation client by Dmytro Moroz (Moroz Inc.). https://github.com/ShiftHackZ/SDAI-KMP

BibTeX:

```bibtex
@software{Moroz_SDAI_2026,
  author = {Moroz, Dmytro},
  date = {2026-11-08},
  license = {AGPL-3.0-only},
  month = nov,
  title = {{SDAI}},
  url = {https://github.com/ShiftHackZ/SDAI-KMP},
  version = {1.2.0},
  year = {2026}
}
```

APA:

```
Moroz, D. (2026). SDAI (Version 1.2.0) [Computer software]. https://github.com/ShiftHackZ/SDAI-KMP
```
