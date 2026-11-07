#!/usr/bin/env node

import { execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, "..");
const deckFile = path.join(root, "docs", "screenshots", "deck.json");
const svgFontFamily =
  '"Helvetica Neue", "Avenir Next", Helvetica, Arial, sans-serif';
const storeHeadlineFontFamily =
  '"Helvetica Neue", Helvetica, Arial, sans-serif';
const galleryTargetImageCount = 6;

const platformDefaults = {
  android: {
    defaultDevice: "emulator-5554",
    packageByFlavor: {
      playstore: "com.shifthackz.aisdv1.app",
      full: "com.shifthackz.aisdv1.app.full",
      foss: "com.shifthackz.aisdv1.app.foss",
    },
  },
  ios: {
    defaultDevice: "booted",
    bundleId: "com.shifthackz.aisdv1.app",
  },
};

const localeFolders = {
  "en-US": "en-US",
  ru: "ru",
  uk: "uk",
};

const localeTags = {
  "en-US": {
    android: "en",
    iosLanguage: "en",
    iosLocale: "en_US",
  },
  ru: {
    android: "ru",
    iosLanguage: "ru",
    iosLocale: "ru_RU",
  },
  uk: {
    android: "uk",
    iosLanguage: "uk",
    iosLocale: "uk_UA",
  },
};

const outputSpecs = {
  fastlane: {
    label: "F-Droid fastlane phone screenshots",
    width: 778,
    height: 1440,
    type: "store",
    platform: "android",
    dir: (locale) =>
      path.join(
        root,
        "fastlane",
        "metadata",
        "android",
        localeFolders[locale],
        "images",
        "phoneScreenshots"
      ),
    file: (_slide, index) => `${index + 1}.png`,
  },
  googleplay: {
    label: "Google Play phone screenshots",
    width: 1080,
    height: 1920,
    type: "store",
    platform: "android",
    dir: (locale) =>
      path.join(
        root,
        "docs",
        "screenshots",
        "googleplay",
        localeFolders[locale],
        "phoneScreenshots"
      ),
    file: (slide, index) =>
      `${String(index + 1).padStart(2, "0")}-${slide.id}.png`,
  },
  appstore: {
    label: "App Store iPhone 6.9 screenshots",
    width: 1320,
    height: 2868,
    type: "store",
    platform: "ios",
    dir: (locale) =>
      path.join(
        root,
        "docs",
        "screenshots",
        "appstore",
        localeFolders[locale],
        "iphone-6.9"
      ),
    file: (slide, index) =>
      `${String(index + 1).padStart(2, "0")}-${slide.id}.png`,
  },
  site: {
    label: "Website banners",
    width: 1600,
    height: 900,
    type: "site",
    platform: "ios",
    dir: () => path.join(root, "docs", "screenshots", "site", "ios", "en-US"),
    file: (slide, index) =>
      `${String(index + 1).padStart(2, "0")}-${slide.id}.png`,
  },
};

const readmeRows = [
  {
    file: path.join(root, "docs", "screenshots", "site", "readme-row-1.png"),
    slides: [0, 1, 2],
  },
  {
    file: path.join(root, "docs", "screenshots", "site", "readme-row-2.png"),
    slides: [3, 4, 5],
  },
];

const featureSpecs = {
  fastlane: { width: 512, height: 250 },
  googleplay: { width: 1024, height: 500 },
};

function parseArgs(argv) {
  const args = { _: [] };
  for (let i = 0; i < argv.length; i += 1) {
    const item = argv[i];
    if (!item.startsWith("--")) {
      args._.push(item);
      continue;
    }

    const raw = item.slice(2);
    if (raw.startsWith("no-")) {
      args[raw.slice(3)] = false;
      continue;
    }

    const eq = raw.indexOf("=");
    if (eq >= 0) {
      args[raw.slice(0, eq)] = raw.slice(eq + 1);
      continue;
    }

    const next = argv[i + 1];
    if (next && !next.startsWith("--")) {
      args[raw] = next;
      i += 1;
    } else {
      args[raw] = true;
    }
  }
  return args;
}

function readDeck() {
  return JSON.parse(fs.readFileSync(deckFile, "utf8"));
}

function canonicalLocale(value = "en-US") {
  const normalized = String(value).replace("_", "-").toLowerCase();
  if (normalized.startsWith("ru")) return "ru";
  if (normalized.startsWith("uk")) return "uk";
  return "en-US";
}

function list(value, fallback) {
  if (value === undefined || value === true || value === "") return fallback;
  return String(value)
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

function mkdirp(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function executablePath(command) {
  const pathEnv = process.env.PATH || "";
  for (const dir of pathEnv.split(path.delimiter)) {
    const candidate = path.join(dir, command);
    try {
      fs.accessSync(candidate, fs.constants.X_OK);
      return candidate;
    } catch {
      // Continue looking through PATH.
    }
  }
  return null;
}

function requireCommand(command, installHint) {
  const found = executablePath(command);
  if (found) return found;
  const hint = installHint ? `\n${installHint}` : "";
  throw new Error(`Required command not found: ${command}${hint}`);
}

function run(command, args, options = {}) {
  execFileSync(command, args, {
    cwd: root,
    stdio: options.stdio || "inherit",
    env: { ...process.env, ...(options.env || {}) },
  });
}

function tryRun(command, args) {
  try {
    run(command, args);
    return true;
  } catch {
    return false;
  }
}

function escapeXml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function yamlString(value) {
  return JSON.stringify(String(value));
}

function pngSize(file) {
  const buffer = fs.readFileSync(file);
  if (buffer.length < 24 || buffer.readUInt32BE(0) !== 0x89504e47) {
    throw new Error(`${path.relative(root, file)} is not a PNG file`);
  }
  return {
    width: buffer.readUInt32BE(16),
    height: buffer.readUInt32BE(20),
  };
}

function imageHref(file) {
  return `data:image/png;base64,${fs.readFileSync(file).toString("base64")}`;
}

function slideCopy(deck, locale, slideId) {
  const localeDeck = deck.locales[locale];
  const copy = localeDeck?.slides?.[slideId];
  if (!copy) {
    throw new Error(
      `Missing localized copy for ${locale}/${slideId} in ${path.relative(
        root,
        deckFile
      )}`
    );
  }
  return copy;
}

function labels(deck, locale) {
  const result = deck.locales[locale]?.labels;
  if (!result) throw new Error(`Missing labels for ${locale}`);
  return result;
}

function rawDirFor(platform, locale, args) {
  return path.resolve(
    root,
    args["raw-dir"] ||
      path.join("docs", "screenshots", "raw", platform, localeFolders[locale])
  );
}

function rawFileFor(rawDir, slide, index) {
  return path.join(
    rawDir,
    `${String(index + 1).padStart(2, "0")}-${slide.id}.png`
  );
}

function screenshotStemFor(rawDir, slide, index) {
  const file = rawFileFor(rawDir, slide, index);
  return path.relative(root, file).replace(/\.png$/i, "");
}

function sourceForSlide(rawDir, slide, index) {
  const file = rawFileFor(rawDir, slide, index);
  if (!fs.existsSync(file)) {
    throw new Error(
      `Missing raw simulator screenshot: ${path.relative(root, file)}\n` +
        `Run capture first, for example:\n` +
        `  node scripts/generate_store_screenshots.mjs capture --platform android --locale en-US --package com.shifthackz.aisdv1.app.full`
    );
  }
  return { file, size: pngSize(file) };
}

function prepareAndroid({ args, packageName, locale }) {
  requireCommand(
    "adb",
    "Install Android platform tools or make sure adb is in PATH."
  );
  const device = args.device || platformDefaults.android.defaultDevice;
  const deviceArgs = device ? ["-s", device] : [];
  const shouldReset = args.reset !== false && args["keep-state"] !== true;
  const tag = localeTags[locale].android;

  tryRun("adb", [...deviceArgs, "shell", "input", "keyevent", "KEYCODE_BACK"]);
  tryRun("adb", [...deviceArgs, "shell", "input", "keyevent", "KEYCODE_BACK"]);
  tryRun("adb", [
    ...deviceArgs,
    "shell",
    "am",
    "force-stop",
    "com.google.android.googlequicksearchbox",
  ]);
  tryRun("adb", [
    ...deviceArgs,
    "shell",
    "am",
    "force-stop",
    "com.google.android.apps.nexuslauncher",
  ]);

  if (shouldReset) {
    run("adb", [...deviceArgs, "shell", "pm", "clear", packageName]);
  }

  if (args["light-theme"] !== false) {
    tryRun("adb", [...deviceArgs, "shell", "cmd", "uimode", "night", "no"]);
    tryRun("adb", [
      ...deviceArgs,
      "shell",
      "settings",
      "put",
      "secure",
      "ui_night_mode",
      "1",
    ]);
  }

  if (
    !tryRun("adb", [
      ...deviceArgs,
      "shell",
      "cmd",
      "locale",
      "set-app-locales",
      packageName,
      "--user",
      "0",
      "--locales",
      tag,
    ])
  ) {
    tryRun("adb", [
      ...deviceArgs,
      "shell",
      "cmd",
      "locale",
      "set-app-locales",
      packageName,
      tag,
    ]);
  }

  if (args["demo-status-bar"] !== false) {
    tryRun("adb", [
      ...deviceArgs,
      "shell",
      "settings",
      "put",
      "global",
      "sysui_demo_allowed",
      "1",
    ]);
    tryRun("adb", [
      ...deviceArgs,
      "shell",
      "am",
      "broadcast",
      "-a",
      "com.android.systemui.demo",
      "-e",
      "command",
      "enter",
    ]);
    tryRun("adb", [
      ...deviceArgs,
      "shell",
      "am",
      "broadcast",
      "-a",
      "com.android.systemui.demo",
      "-e",
      "command",
      "clock",
      "-e",
      "hhmm",
      "0941",
    ]);
    tryRun("adb", [
      ...deviceArgs,
      "shell",
      "am",
      "broadcast",
      "-a",
      "com.android.systemui.demo",
      "-e",
      "command",
      "battery",
      "-e",
      "level",
      "100",
      "-e",
      "plugged",
      "false",
    ]);
    tryRun("adb", [
      ...deviceArgs,
      "shell",
      "am",
      "broadcast",
      "-a",
      "com.android.systemui.demo",
      "-e",
      "command",
      "network",
      "-e",
      "wifi",
      "show",
      "-e",
      "level",
      "4",
      "-e",
      "mobile",
      "show",
      "-e",
      "datatype",
      "5g",
      "-e",
      "sims",
      "1",
    ]);
  }
}

function prepareIos({ args, bundleId, locale }) {
  requireCommand(
    "xcrun",
    "Install Xcode command line tools or make sure xcrun is in PATH."
  );
  const device = args.device || platformDefaults.ios.defaultDevice;
  const tag = localeTags[locale];

  tryRun("xcrun", ["simctl", "terminate", device, bundleId]);
  if (args["light-theme"] !== false) {
    tryRun("xcrun", ["simctl", "ui", device, "appearance", "light"]);
  }
  tryRun("xcrun", [
    "simctl",
    "status_bar",
    device,
    "override",
    "--time",
    "9:41",
    "--wifiBars",
    "3",
    "--cellularBars",
    "4",
    "--batteryState",
    "charged",
    "--batteryLevel",
    "100",
  ]);
  tryRun("xcrun", [
    "simctl",
    "spawn",
    device,
    "defaults",
    "write",
    "NSGlobalDomain",
    "AppleLanguages",
    "-array",
    tag.iosLanguage,
  ]);
  tryRun("xcrun", [
    "simctl",
    "spawn",
    device,
    "defaults",
    "write",
    "NSGlobalDomain",
    "AppleLocale",
    tag.iosLocale,
  ]);
}

function setupDemoFlow(deck, locale, prepareMode, platform) {
  if (prepareMode === "existing") return [];
  const l = labels(deck, locale);
  return [
    ...onboardingFlow(),
    ...maybeTap(l.next),
    ...waitDone(),
    ...tapPoint("8%,9%"),
    ...waitDone(),
    ...swipeDown(),
    ...waitDone(),
    ...swipeDown(),
    ...waitDone(),
    ...waitVisible(l.automatic1111, 45000),
    ...tap(l.automatic1111),
    ...waitDone(),
    ...tap(l.next),
    ...waitDone(),
    ...waitVisible(l.demoMode, 45000),
    ...tap(l.demoMode),
    ...waitDone(),
    ...tap(l.connect),
    ...waitVisible(l.txt2img, 45000),
  ];
}

function onboardingFlow() {
  const commands = [];
  for (let index = 0; index < 7; index += 1) {
    commands.push(...tapPoint("82%,90%"), ...waitDone(5000));
  }
  return commands;
}

function tap(text) {
  return [`- tapOn: ${yamlString(text)}`];
}

function tapPoint(point) {
  return ["- tapOn:", `    point: ${yamlString(point)}`];
}

function doubleTapPoint(point) {
  return [
    ...tapPoint(point),
    ...waitDone(350),
    ...tapPoint(point),
    ...waitDone(1000),
  ];
}

function waitVisible(text, timeout = 20000) {
  return [
    "- extendedWaitUntil:",
    `    visible: ${yamlString(text)}`,
    `    timeout: ${timeout}`,
  ];
}

function waitDone(timeout = 5000) {
  return ["- waitForAnimationToEnd:", `    timeout: ${timeout}`];
}

function take(rawDir, slide, index) {
  return [
    `- takeScreenshot: ${yamlString(screenshotStemFor(rawDir, slide, index))}`,
  ];
}

function takeNamed(rawDir, name) {
  const file = path.join(rawDir, `${name}.png`);
  return [
    `- takeScreenshot: ${yamlString(
      path.relative(root, file).replace(/\.png$/i, "")
    )}`,
  ];
}

function maybeTap(text) {
  return ["- tapOn:", `    text: ${yamlString(text)}`, "    optional: true"];
}

function hideKeyboard() {
  return ["- hideKeyboard"];
}

function dismissKeyboard() {
  return [...tapPoint("50%,17%"), ...waitDone(1000)];
}

function swipeUp() {
  return [
    "- swipe:",
    "    start: 50%,76%",
    "    end: 50%,34%",
    "    duration: 650",
  ];
}

function swipeUpToAccentColors(platform) {
  if (platform === "ios") {
    return [
      "- swipe:",
      "    start: 50%,73%",
      "    end: 50%,43%",
      "    duration: 1800",
    ];
  }

  return [
    "- swipe:",
    "    start: 50%,73%",
    "    end: 50%,53%",
    "    duration: 500",
  ];
}

function swipeDown() {
  return [
    "- swipe:",
    "    start: 50%,34%",
    "    end: 50%,76%",
    "    duration: 650",
  ];
}

function settingsPreferencesFlow(l) {
  return [
    ...tap(l.settings),
    ...waitVisible(l.settings, 20000),
    ...waitDone(),
    ...tap(l.taggedInput),
    ...waitDone(1500),
    ...tap(l.advancedFormDefault),
    ...waitDone(1500),
  ];
}

function fillPromptFlow(localeDeck, l) {
  return [
    ...tap(l.txt2img),
    ...waitVisible(l.textToImageTitle, 20000),
    ...waitDone(),
    ...tap(l.prompt),
    "- eraseText",
    `- inputText: ${yamlString(`${localeDeck.mockPrompt},`)}`,
    ...dismissKeyboard(),
    ...tap(l.negativePrompt),
    "- eraseText",
    `- inputText: ${yamlString(`${localeDeck.mockNegativePrompt},`)}`,
    ...dismissKeyboard(),
    ...tap(l.textToImageTitle),
    ...waitDone(),
    ...maybeTap(l.showAdvanced),
    ...waitDone(),
  ];
}

function extraGenerationsFlow(l, count = 2) {
  const commands = [];
  for (let index = 0; index < count; index += 1) {
    commands.push(
      ...tap(l.generate),
      ...waitVisible(l.close, 90000),
      ...waitDone(9000),
      ...tap(l.close),
      ...waitDone(3000)
    );
  }
  return commands;
}

function zoomOpenImageFlow(platform) {
  return platform === "ios" ? doubleTapPoint("50%,45%") : [];
}

function lookAndFeelFlow(l, rawDir, platform) {
  return [
    ...tapPoint("8%,9%"),
    ...waitVisible(l.settings, 20000),
    ...waitDone(),
    ...swipeUp(),
    ...waitDone(),
    ...waitVisible(l.lookAndFeel, 20000),
    ...waitDone(),
    ...takeNamed(rawDir, "settings-light"),
    ...tap(l.systemDarkTheme),
    ...waitDone(3000),
    ...takeNamed(rawDir, "settings-dark"),
    ...swipeUpToAccentColors(platform),
    ...waitDone(),
    ...waitVisible(l.accentColor, 20000),
    ...takeNamed(rawDir, "settings-dark-accent"),
  ];
}

function buildMaestroFlow({ deck, platform, locale, appId, rawDir, args }) {
  const l = labels(deck, locale);
  const localeDeck = deck.locales[locale];
  const prepareMode = args.prepare || "demo";
  const reset = args.reset !== false && args["keep-state"] !== true;
  const launchClearState = platform === "android" ? false : reset;
  const slidesById = new Map(
    deck.slides.map((slide, index) => [slide.id, { slide, index }])
  );
  const output = [
    `appId: ${yamlString(appId)}`,
    "---",
    "- launchApp:",
    `    clearState: ${launchClearState ? "true" : "false"}`,
    "    stopApp: true",
    ...setupDemoFlow(deck, locale, prepareMode, platform),
    ...settingsPreferencesFlow(l),
    ...fillPromptFlow(localeDeck, l),
    ...take(
      rawDir,
      slidesById.get("controls").slide,
      slidesById.get("controls").index
    ),
    ...tap(l.generate),
    ...waitVisible(l.close, 90000),
    ...waitDone(9000),
    ...take(
      rawDir,
      slidesById.get("create").slide,
      slidesById.get("create").index
    ),
    ...tap(l.close),
    ...waitDone(3000),
    ...extraGenerationsFlow(l, galleryTargetImageCount - 1),
    ...tap(l.gallery),
    ...waitVisible(l.gallery, 20000),
    ...waitDone(12000),
    ...take(
      rawDir,
      slidesById.get("gallery").slide,
      slidesById.get("gallery").index
    ),
    ...tapPoint("72%,30%"),
    ...waitVisible(l.galleryDetails, 30000),
    ...waitDone(9000),
    ...tap(l.galleryInfo),
    ...waitDone(),
    ...take(rawDir, slidesById.get("open").slide, slidesById.get("open").index),
    ...tapPoint("8%,9%"),
    ...waitVisible(l.gallery, 20000),
    ...waitDone(),
    ...tap(l.settings),
    ...waitVisible(l.settings, 20000),
    ...tap(l.configuration),
    ...waitVisible(l.configuration, 20000),
    ...waitVisible(l.automatic1111, 45000),
    ...waitDone(2000),
    ...take(
      rawDir,
      slidesById.get("providers").slide,
      slidesById.get("providers").index
    ),
    ...lookAndFeelFlow(l, rawDir, platform),
  ];

  if (platform === "ios") {
    output.splice(3, 0, "    permissions:", "      photos: allow");
  }

  return `${output.join("\n")}\n`;
}

function composeSettingsSplit(rawDir, slide, index) {
  const lightFile = path.join(rawDir, "settings-light.png");
  const darkFile = path.join(rawDir, "settings-dark.png");
  const darkAccentFile = path.join(rawDir, "settings-dark-accent.png");
  const outFile = rawFileFor(rawDir, slide, index);

  if (
    !fs.existsSync(lightFile) ||
    !fs.existsSync(darkFile) ||
    !fs.existsSync(darkAccentFile)
  ) {
    throw new Error(
      `Missing Look & Feel source screenshots for ${path.relative(
        root,
        outFile
      )}.\n` +
        `Expected ${path.relative(root, lightFile)}, ${path.relative(
          root,
          darkFile
        )}, and ${path.relative(root, darkAccentFile)}.`
    );
  }

  const lightSize = pngSize(lightFile);
  const darkSize = pngSize(darkFile);
  const darkAccentSize = pngSize(darkAccentFile);
  if (
    lightSize.width !== darkSize.width ||
    lightSize.height !== darkSize.height ||
    lightSize.width !== darkAccentSize.width ||
    lightSize.height !== darkAccentSize.height
  ) {
    throw new Error(
      `Settings split sources have different sizes: ` +
        `${lightSize.width}x${lightSize.height}, ` +
        `${darkSize.width}x${darkSize.height}, ` +
        `${darkAccentSize.width}x${darkAccentSize.height}`
    );
  }

  const { width, height } = lightSize;
  const id = `settings-split-${width}-${height}`;
  const isIosSource = width < 1250;
  const splitTopX = Math.round(width * 0.72);
  const splitBottomX = Math.round(width * 0.32);
  const accentTargetY = Math.round(height * (isIosSource ? 0.544 : 0.626));
  const accentSourceY = Math.round(height * (isIosSource ? 0.444 : 0.574));
  const accentYOffset = isIosSource ? -13 : 10;
  const bottomBarYOffset = isIosSource ? -11 : -38;
  const bottomBarY =
    Math.round(height * (isIosSource ? 0.874 : 0.904)) + bottomBarYOffset;
  const darkAccentY = accentTargetY - accentSourceY + accentYOffset;
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
      <defs>
        <clipPath id="${id}-dark-side">
          <polygon points="${splitTopX},0 ${width},0 ${width},${height} ${splitBottomX},${height}"/>
        </clipPath>
        <clipPath id="${id}-accent-patch">
          <rect x="0" y="${accentTargetY}" width="${width}" height="${
    bottomBarY - accentTargetY
  }"/>
        </clipPath>
      </defs>
      <image href="${imageHref(
        lightFile
      )}" x="0" y="0" width="${width}" height="${height}"/>
      <g clip-path="url(#${id}-dark-side)">
        <image href="${imageHref(
          darkFile
        )}" x="0" y="0" width="${width}" height="${height}"/>
        <g clip-path="url(#${id}-accent-patch)">
          <image href="${imageHref(
            darkAccentFile
          )}" x="0" y="${darkAccentY}" width="${width}" height="${height}"/>
        </g>
      </g>
      <path d="M ${splitTopX} 0 L ${splitBottomX} ${height}" stroke="#ffffff" stroke-opacity="0.78" stroke-width="${Math.max(
    3,
    Math.round(width * 0.006)
  )}"/>
    </svg>
  `;

  renderSvg(svg, outFile);
}

function prepareDerivedRawScreenshots({ deck, rawDir }) {
  requireCommand(
    "rsvg-convert",
    "Install librsvg or set rsvg-convert in PATH."
  );
  const settingsEntry = deck.slides
    .map((slide, index) => ({ slide, index }))
    .find((entry) => entry.slide.id === "settings");
  if (settingsEntry) {
    composeSettingsSplit(rawDir, settingsEntry.slide, settingsEntry.index);
  }
}

function capture(args) {
  const deck = readDeck();
  const platform = args.platform || deck.defaultPlatform || "android";
  const locale = canonicalLocale(args.locale || deck.defaultLocale);
  const rawDir = rawDirFor(platform, locale, args);
  mkdirp(rawDir);

  let appId;
  if (platform === "android") {
    const flavor = args.flavor || "full";
    appId =
      args.package ||
      platformDefaults.android.packageByFlavor[flavor] ||
      platformDefaults.android.packageByFlavor.full;
  } else if (platform === "ios") {
    appId = args.bundle || platformDefaults.ios.bundleId;
  } else {
    throw new Error(`Unknown platform: ${platform}`);
  }

  const flowDir = path.join(root, "docs", "screenshots", "raw", ".maestro");
  mkdirp(flowDir);
  const flowFile = path.join(flowDir, `${platform}-${locale}.yaml`);
  fs.writeFileSync(
    flowFile,
    buildMaestroFlow({ deck, platform, locale, appId, rawDir, args })
  );

  if (args["dry-run"]) {
    console.log(`Wrote ${path.relative(root, flowFile)}`);
    return;
  }

  requireCommand(
    "maestro",
    "Install Maestro first: https://docs.maestro.dev/maestro-cli/how-to-install-maestro-cli"
  );

  if (platform === "android") {
    prepareAndroid({ args, packageName: appId, locale });
  } else {
    prepareIos({ args, bundleId: appId, locale });
  }

  const maestroArgs = ["test", "--platform", platform];
  if (args.device) {
    maestroArgs.push("--udid", args.device);
  }
  maestroArgs.push(flowFile);
  run("maestro", maestroArgs);

  prepareDerivedRawScreenshots({ deck, rawDir });

  deck.slides.forEach((slide, index) => {
    pngSize(rawFileFor(rawDir, slide, index));
  });

  console.log(
    `Captured ${
      deck.slides.length
    } raw ${platform} screenshots into ${path.relative(root, rawDir)}`
  );
}

function defs(id, theme) {
  const [start, mid, end] = theme.background;
  return `
    <defs>
      <linearGradient id="bg-${id}" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="${start}"/>
        <stop offset="0.55" stop-color="${mid}"/>
        <stop offset="1" stop-color="${end}"/>
      </linearGradient>
      <linearGradient id="shine-${id}" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="#ffffff" stop-opacity="0.28"/>
        <stop offset="1" stop-color="#ffffff" stop-opacity="0"/>
      </linearGradient>
      <pattern id="grid-${id}" width="64" height="64" patternUnits="userSpaceOnUse">
        <path d="M 64 0 L 0 0 0 64" fill="none" stroke="${theme.ink}" stroke-width="1" opacity="0.08"/>
      </pattern>
      <filter id="shadow-${id}" x="-30%" y="-20%" width="160%" height="150%">
        <feDropShadow dx="0" dy="26" stdDeviation="30" flood-color="#000000" flood-opacity="0.34"/>
      </filter>
    </defs>
  `;
}

function background(id, width, height, theme) {
  const band = `M ${width * 0.52} 0 L ${width} 0 L ${width} ${
    height * 0.55
  } L ${width * 0.18} ${height} L 0 ${height} L 0 ${height * 0.78} Z`;
  const thin = `M ${width * 0.72} 0 L ${width} 0 L ${width} ${height} L ${
    width * 0.5
  } ${height} Z`;
  return `
    <rect width="${width}" height="${height}" fill="url(#bg-${id})"/>
    <path d="${band}" fill="url(#shine-${id})"/>
    <path d="${thin}" fill="${theme.accent}" opacity="0.12"/>
    <rect width="${width}" height="${height}" fill="url(#grid-${id})"/>
  `;
}

function renderTextBlock({
  x,
  y,
  width,
  copy,
  theme,
  scale = 1,
  align = "start",
  eyebrow = false,
}) {
  const anchor = align === "middle" ? "middle" : "start";
  const headlineSize = Math.round(84 * scale);
  const sublineSize = Math.round(30 * scale);
  const headlineStart = eyebrow ? Math.round(52 * scale) : 0;
  const lineHeight = Math.round(headlineSize * 1.13);
  const subY =
    y +
    headlineStart +
    copy.headline.length * lineHeight +
    Math.round(46 * scale);
  const lines = [];

  if (eyebrow) {
    lines.push(
      `<text x="${x}" y="${y}" text-anchor="${anchor}" fill="${
        theme.accent
      }" font-size="${Math.round(22 * scale)}" font-weight="800">SDAI</text>`
    );
  }
  copy.headline.forEach((line, index) => {
    lines.push(
      `<text x="${x}" y="${
        y + headlineStart + index * lineHeight
      }" text-anchor="${anchor}" fill="${
        theme.ink
      }" font-size="${headlineSize}" font-weight="800">${escapeXml(
        line
      )}</text>`
    );
  });

  wrapWords(copy.subline, Math.max(24, Math.round(width / (sublineSize * 0.5))))
    .slice(0, 3)
    .forEach((line, index) => {
      lines.push(
        `<text x="${x}" y="${
          subY + index * Math.round(sublineSize * 1.48)
        }" text-anchor="${anchor}" fill="${
          theme.muted
        }" font-size="${sublineSize}" font-weight="600">${escapeXml(
          line
        )}</text>`
      );
    });

  return lines.join("\n");
}

function renderStoreHeadline({ x, y, width, copy, theme, scale = 1 }) {
  const headlineSize = Math.round(122 * scale);
  const lineHeight = Math.round(headlineSize * 1.08);
  const lines = copy.headline.map(
    (line, index) =>
      `<text x="${x}" y="${
        y + index * lineHeight
      }" text-anchor="middle" fill="${theme.ink}" font-family="${escapeXml(
        storeHeadlineFontFamily
      )}" font-size="${headlineSize}" font-weight="900">${escapeXml(
        line
      )}</text>`
  );
  return lines.join("\n");
}

function wrapWords(text, maxChars) {
  const words = String(text).split(/\s+/).filter(Boolean);
  const lines = [];
  let line = "";
  for (const word of words) {
    const next = line ? `${line} ${word}` : word;
    if (next.length > maxChars && line) {
      lines.push(line);
      line = word;
    } else {
      line = next;
    }
  }
  if (line) lines.push(line);
  return lines;
}

function phoneFrame({ id, platform, source, x, y, width, height }) {
  const isIos = platform === "ios";
  const outerRadius = width * (isIos ? 0.132 : 0.115);
  const sideInset = width * 0.042;
  const topInset = width * (isIos ? 0.044 : 0.042);
  const bottomInset = width * (isIos ? 0.044 : 0.042);
  const screen = {
    x: x + sideInset,
    y: y + topInset,
    width: width - sideInset * 2,
    height: height - topInset - bottomInset,
    radius: outerRadius * (isIos ? 0.78 : 0.82),
  };

  const buttons = isIos
    ? `
      <rect x="${x - width * 0.012}" y="${y + height * 0.18}" width="${
        width * 0.012
      }" height="${height * 0.075}" rx="${width * 0.006}" fill="#7f7b76"/>
      <rect x="${x - width * 0.014}" y="${y + height * 0.31}" width="${
        width * 0.014
      }" height="${height * 0.115}" rx="${width * 0.007}" fill="#7f7b76"/>
      <rect x="${x + width}" y="${y + height * 0.265}" width="${
        width * 0.013
      }" height="${height * 0.135}" rx="${width * 0.006}" fill="#d8d0c4"/>
    `
    : `
      <rect x="${x + width}" y="${y + height * 0.19}" width="${
        width * 0.011
      }" height="${height * 0.095}" rx="${width * 0.0055}" fill="#7f838d"/>
      <rect x="${x + width}" y="${y + height * 0.33}" width="${
        width * 0.011
      }" height="${height * 0.13}" rx="${width * 0.0055}" fill="#7f838d"/>
    `;

  const sensor = isIos
    ? `<rect x="${screen.x + screen.width * 0.335}" y="${
        screen.y + width * 0.024
      }" width="${screen.width * 0.33}" height="${width * 0.066}" rx="${
        width * 0.033
      }" fill="#050508"/>`
    : `<circle cx="${x + width / 2}" cy="${screen.y + width * 0.04}" r="${
        width * 0.023
      }" fill="#050508"/><circle cx="${x + width / 2}" cy="${
        screen.y + width * 0.04
      }" r="${width * 0.01}" fill="#202334"/>`;

  return `
    <defs>
      <linearGradient id="phone-edge-${id}" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="${isIos ? "#eee7dc" : "#23252c"}"/>
        <stop offset="0.42" stop-color="${isIos ? "#a8a096" : "#090a0d"}"/>
        <stop offset="1" stop-color="${isIos ? "#f8f4eb" : "#424650"}"/>
      </linearGradient>
      <linearGradient id="phone-glass-${id}" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="#20212a"/>
        <stop offset="0.52" stop-color="#050508"/>
        <stop offset="1" stop-color="#161821"/>
      </linearGradient>
    </defs>
    <g filter="url(#shadow-${id})">
      ${buttons}
      <rect x="${x}" y="${y}" width="${width}" height="${height}" rx="${outerRadius}" fill="url(#phone-edge-${id})"/>
      <rect x="${x + width * 0.01}" y="${y + width * 0.01}" width="${
    width * 0.98
  }" height="${height - width * 0.02}" rx="${
    outerRadius * 0.93
  }" fill="url(#phone-glass-${id})"/>
      <rect x="${x + width * 0.02}" y="${y + width * 0.02}" width="${
    width * 0.96
  }" height="${height - width * 0.04}" rx="${
    outerRadius * 0.84
  }" fill="#050508"/>
      <clipPath id="screen-${id}">
        <rect x="${screen.x}" y="${screen.y}" width="${screen.width}" height="${
    screen.height
  }" rx="${screen.radius}"/>
      </clipPath>
      <g clip-path="url(#screen-${id})">
        <image href="${imageHref(source.file)}" x="${screen.x}" y="${
    screen.y
  }" width="${screen.width}" height="${
    screen.height
  }" preserveAspectRatio="xMidYMid slice"/>
      </g>
      ${sensor}
      <rect x="${x + width * 0.012}" y="${y + width * 0.012}" width="${
    width * 0.976
  }" height="${height - width * 0.024}" rx="${
    outerRadius * 0.91
  }" fill="none" stroke="#ffffff" stroke-opacity="${
    isIos ? "0.28" : "0.1"
  }" stroke-width="${Math.max(1.5, width * 0.004)}"/>
      <rect x="${screen.x}" y="${screen.y}" width="${screen.width}" height="${
    screen.height
  }" rx="${
    screen.radius
  }" fill="none" stroke="#ffffff" stroke-opacity="0.08" stroke-width="2"/>
    </g>
  `;
}

function renderStoreSvg({
  deck,
  platform,
  locale,
  slide,
  index,
  source,
  spec,
}) {
  const id = `${platform}-${locale}-${slide.id}-${spec.width}`;
  const copy = slideCopy(deck, locale, slide.id);
  const theme = slide.theme;
  const { width, height } = spec;
  const phoneTop = Math.round(height * (platform === "ios" ? 0.205 : 0.225));
  const phoneBottom = Math.round(height * (platform === "ios" ? 0.035 : 0.028));
  const phoneHeight = height - phoneTop - phoneBottom;
  const phoneWidth = Math.round(
    phoneHeight / (platform === "ios" ? 2.12 : 2.08)
  );
  const phoneX = Math.round((width - phoneWidth) / 2);
  const phoneY = phoneTop;
  const textX = width / 2;
  const textY = Math.round(height * (platform === "ios" ? 0.072 : 0.078));
  const textScale = width / 1240;

  return `
    <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" font-family="${escapeXml(
    svgFontFamily
  )}">
      ${defs(id, theme)}
      ${background(id, width, height, theme)}
      ${renderStoreHeadline({
        x: textX,
        y: textY,
        width: width * 0.86,
        copy,
        theme,
        scale: textScale,
      })}
      ${phoneFrame({
        id,
        platform,
        source,
        x: phoneX,
        y: phoneY,
        width: phoneWidth,
        height: phoneHeight,
      })}
    </svg>
  `;
}

function renderSiteSvg({ deck, platform, locale, slide, index, source, spec }) {
  const id = `site-${platform}-${locale}-${slide.id}`;
  const theme = slide.theme;
  const { width, height } = spec;
  const phoneHeight = height - 88;
  const phoneWidth = Math.round(
    phoneHeight / (platform === "ios" ? 2.12 : 2.08)
  );
  const phoneY = Math.round((height - phoneHeight) / 2);
  const phoneX = width - phoneWidth - 136;

  return `
    <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" font-family="${escapeXml(
    svgFontFamily
  )}">
      ${defs(id, theme)}
      ${background(id, width, height, theme)}
      ${phoneFrame({
        id,
        platform,
        source,
        x: phoneX,
        y: phoneY,
        width: phoneWidth,
        height: phoneHeight,
      })}
    </svg>
  `;
}

function renderFeatureGraphic({ deck, locale, target }) {
  const spec = featureSpecs[target];
  if (!spec) return;
  const copy = deck.locales[locale]?.featureGraphic;
  if (!copy) throw new Error(`Missing feature graphic copy for ${locale}`);

  const { width, height } = spec;
  const id = `feature-${target}-${locale}`;
  const theme = {
    background: ["#17151f", "#29264b", "#139fb3"],
    accent: "#42d3d6",
    ink: "#ffffff",
    muted: "#d7d4e9",
  };
  const iconFile = path.join(root, "docs", "assets", "sdai.png");
  const iconSize = Math.round(height * 0.62);
  const iconX = Math.round(width * 0.08);
  const iconY = Math.round((height - iconSize) / 2);
  const textX = iconX + iconSize + Math.round(width * 0.065);
  const titleSize = Math.round(height * 0.24);
  const lineSize = Math.round(height * (locale === "en-US" ? 0.084 : 0.068));
  const smallSize = Math.round(height * 0.058);
  const dir =
    target === "fastlane"
      ? path.join(
          root,
          "fastlane",
          "metadata",
          "android",
          localeFolders[locale],
          "images"
        )
      : path.join(
          root,
          "docs",
          "screenshots",
          "googleplay",
          localeFolders[locale]
        );
  mkdirp(dir);

  const { headline, subline } = copy;
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" font-family="${escapeXml(
    svgFontFamily
  )}">
      ${defs(id, theme)}
      ${background(id, width, height, theme)}
      <defs>
        <clipPath id="feature-icon-${id}">
          <rect x="${iconX}" y="${iconY}" width="${iconSize}" height="${iconSize}" rx="${Math.round(
    iconSize * 0.22
  )}"/>
        </clipPath>
      </defs>
      <rect x="${iconX}" y="${iconY}" width="${iconSize}" height="${iconSize}" rx="${Math.round(
    iconSize * 0.22
  )}" fill="#ffffff" opacity="0.94" filter="url(#shadow-${id})"/>
      <image href="${imageHref(
        iconFile
      )}" x="${iconX}" y="${iconY}" width="${iconSize}" height="${iconSize}" preserveAspectRatio="xMidYMid meet" clip-path="url(#feature-icon-${id})"/>
      <text x="${textX}" y="${Math.round(height * 0.41)}" fill="${
    theme.ink
  }" font-size="${titleSize}" font-weight="800">SDAI</text>
      <text x="${textX}" y="${Math.round(height * 0.61)}" fill="${
    theme.accent
  }" font-size="${lineSize}" font-weight="800">${escapeXml(headline)}</text>
      <text x="${textX}" y="${Math.round(height * 0.78)}" fill="${
    theme.muted
  }" font-size="${smallSize}" font-weight="600">${escapeXml(subline)}</text>
    </svg>
  `;

  renderSvg(svg, path.join(dir, "featureGraphic.png"));

  if (target === "fastlane") {
    fs.copyFileSync(iconFile, path.join(dir, "icon.png"));
  }
}

function renderSvg(svg, outFile) {
  mkdirp(path.dirname(outFile));
  mkdirp(path.join("/tmp", "sdai-fontconfig-cache"));
  const tempFile = path.join(
    path.dirname(outFile),
    `.${path.basename(outFile)}.svg`
  );
  fs.writeFileSync(tempFile, svg);
  try {
    run("rsvg-convert", ["--format", "png", "--output", outFile, tempFile], {
      env: { XDG_CACHE_HOME: path.join("/tmp", "sdai-fontconfig-cache") },
    });
  } finally {
    fs.rmSync(tempFile, { force: true });
  }
}

function renderReadmeRows(deck) {
  const spec = outputSpecs.appstore;
  const sourceDir = spec.dir("en-US", "ios");
  const rowWidth = spec.width * 3;
  const rowHeight = spec.height;
  const missing = [];

  for (const row of readmeRows) {
    for (const slideIndex of row.slides) {
      const slide = deck.slides[slideIndex];
      const file = path.join(sourceDir, spec.file(slide, slideIndex));
      if (!fs.existsSync(file)) missing.push(path.relative(root, file));
    }
  }

  if (missing.length) {
    console.warn(
      `Skipped README screenshot rows. Missing iOS App Store screenshots:\n- ${missing.join(
        "\n- "
      )}`
    );
    return;
  }

  readmeRows.forEach((row) => {
    const images = row.slides
      .map((slideIndex, column) => {
        const slide = deck.slides[slideIndex];
        const file = path.join(sourceDir, spec.file(slide, slideIndex));
        const size = pngSize(file);
        if (size.width !== spec.width || size.height !== spec.height) {
          throw new Error(
            `${path.relative(root, file)} is ${size.width}x${
              size.height
            }, expected ${spec.width}x${spec.height}`
          );
        }
        return `<image href="${imageHref(file)}" x="${
          column * spec.width
        }" y="0" width="${spec.width}" height="${spec.height}"/>`;
      })
      .join("\n");

    const svg = `
      <svg xmlns="http://www.w3.org/2000/svg" width="${rowWidth}" height="${rowHeight}" viewBox="0 0 ${rowWidth} ${rowHeight}">
        ${images}
      </svg>
    `;
    renderSvg(svg, row.file);
    const size = pngSize(row.file);
    if (size.width !== rowWidth || size.height !== rowHeight) {
      throw new Error(
        `${path.relative(root, row.file)} rendered as ${size.width}x${
          size.height
        }, expected ${rowWidth}x${rowHeight}`
      );
    }
    console.log(
      `Rendered README screenshot row: ${path.relative(root, row.file)}`
    );
  });
}

function render(args) {
  const deck = readDeck();
  const platform = args.platform || deck.defaultPlatform || "android";
  const localeList = list(args.locales || args.locale, [
    canonicalLocale(deck.defaultLocale),
  ]);
  const targets = list(
    args.targets,
    platform === "ios" ? ["appstore", "site"] : ["fastlane", "googleplay"]
  );
  requireCommand(
    "rsvg-convert",
    "Install librsvg or set rsvg-convert in PATH."
  );

  for (const rawLocale of localeList) {
    const locale = canonicalLocale(rawLocale);
    const rawDir = rawDirFor(platform, locale, args);
    prepareDerivedRawScreenshots({ deck, rawDir });

    for (const target of targets) {
      const spec = outputSpecs[target];
      if (!spec) throw new Error(`Unknown target: ${target}`);
      if (spec.platform && spec.platform !== platform) {
        throw new Error(
          `${target} is for ${spec.platform}, but --platform ${platform} was selected.`
        );
      }
      if (target === "site" && locale !== "en-US") {
        console.log(
          `Skipped ${spec.label} for ${locale}; website screenshots use ios/en-US only.`
        );
        continue;
      }

      const outDir = spec.dir(locale, platform);
      mkdirp(outDir);
      deck.slides.forEach((slide, index) => {
        const source = sourceForSlide(rawDir, slide, index);
        const svg =
          spec.type === "site"
            ? renderSiteSvg({
                deck,
                platform,
                locale,
                slide,
                index,
                source,
                spec,
              })
            : renderStoreSvg({
                deck,
                platform,
                locale,
                slide,
                index,
                source,
                spec,
              });
        const outFile = path.join(outDir, spec.file(slide, index));
        renderSvg(svg, outFile);
        const size = pngSize(outFile);
        if (size.width !== spec.width || size.height !== spec.height) {
          throw new Error(
            `${path.relative(root, outFile)} rendered as ${size.width}x${
              size.height
            }, expected ${spec.width}x${spec.height}`
          );
        }
      });

      renderFeatureGraphic({ deck, locale, target });
      console.log(`Rendered ${spec.label}: ${path.relative(root, outDir)}`);
    }
  }

  renderReadmeRows(deck);
}

function featureGraphics(args) {
  const deck = readDeck();
  const locales = list(args.locales || args.locale, [
    canonicalLocale(deck.defaultLocale),
  ]).map(canonicalLocale);
  const targets = list(args.targets, Object.keys(featureSpecs));
  for (const target of targets) {
    if (!featureSpecs[target])
      throw new Error(
        `Unknown feature graphics target: ${target}. Use fastlane or googleplay.`
      );
  }

  const iconFile = path.join(root, "docs", "assets", "sdai.png");
  const size = pngSize(iconFile);
  if (size.width !== 512 || size.height !== 512) {
    throw new Error(
      `${path.relative(root, iconFile)} is ${size.width}x${
        size.height
      }, expected 512x512`
    );
  }
  requireCommand(
    "rsvg-convert",
    "Install librsvg or set rsvg-convert in PATH."
  );

  for (const locale of locales) {
    for (const target of targets) {
      renderFeatureGraphic({ deck, locale, target });
      console.log(
        `Rendered ${target} feature graphic for ${locale}${
          target === "fastlane" ? " and copied icon.png" : ""
        }.`
      );
    }
  }
}

function plan(args) {
  const deck = readDeck();
  const platform = args.platform || deck.defaultPlatform || "android";
  const locale = canonicalLocale(args.locale || deck.defaultLocale);
  const rawDir = rawDirFor(platform, locale, args);

  console.log(`Platform: ${platform}`);
  console.log(`Locale: ${locale}`);
  console.log(`Raw source: ${path.relative(root, rawDir)}`);
  console.log("");
  deck.slides.forEach((slide, index) => {
    const copy = slideCopy(deck, locale, slide.id);
    console.log(`${index + 1}. ${slide.id}`);
    console.log(
      `   raw: ${path.relative(root, rawFileFor(rawDir, slide, index))}`
    );
    console.log(`   headline: ${copy.headline.join(" / ")}`);
    console.log(`   subline: ${copy.subline}`);
  });
}

function all(args) {
  capture(args);
  render(args);
}

function printHelp() {
  console.log(`
Usage:
  node scripts/generate_store_screenshots.mjs plan --platform android --locale en-US
  node scripts/generate_store_screenshots.mjs capture --platform android --locale en-US --package com.shifthackz.aisdv1.app.full
  node scripts/generate_store_screenshots.mjs render --platform android --locale en-US --targets fastlane,googleplay
  node scripts/generate_store_screenshots.mjs feature-graphics --locales en-US,ru,uk --targets fastlane,googleplay
  node scripts/generate_store_screenshots.mjs capture --platform ios --locale uk --device booted
  node scripts/generate_store_screenshots.mjs render --platform ios --locale en-US --targets appstore,site

Commands:
  plan              Print the localized screenshot deck and required raw files.
  capture           Generate and run a Maestro flow against a real simulator/emulator.
  render            Render raw simulator captures into store/site marketing screenshots.
  feature-graphics  Render featureGraphic.png and copy the Fastlane icon for selected locales.
  all               Run capture, then render.

Capture options:
  --platform android|ios
  --locale en-US|ru|uk
  --device <id>             adb device id or xcrun simctl device id.
  --package <id>            Android package id. Defaults to full flavor.
  --flavor playstore|full|foss
  --bundle <id>             iOS bundle id.
  --prepare demo|existing   demo resets/configures A1111 demo mode. existing skips setup.
  --keep-state              Do not clear app state before capture.
  --no-light-theme          Do not force the simulator/emulator into light appearance.
  --dry-run                 Only write the Maestro flow.

Render options:
  --targets fastlane,googleplay,appstore,site
  --raw-dir <path>          Raw simulator screenshot directory.
  --locales en-US,ru,uk     Render multiple locales for the same platform.

Feature graphics options:
  --targets fastlane,googleplay  Defaults to both targets.
  --locale en-US|ru|uk           Defaults to the deck locale.
  --locales en-US,ru,uk          Render multiple locales.

Notes:
  - Render never falls back to existing fastlane screenshots.
  - Raw screenshots must come from capture and live in docs/screenshots/raw/<platform>/<locale>/.
  - Website screenshots are generated from iOS en-US only.
  - README screenshot rows are generated from iOS en-US App Store screenshots.
  - Maestro is required for capture: https://docs.maestro.dev/getting-started/installing-maestro
  - Maestro is required for capture: https://docs.maestro.dev/maestro-cli/how-to-install-maestro-cli
`);
}

const args = parseArgs(process.argv.slice(2));
const command = args._[0] || "help";

try {
  if (command === "plan") plan(args);
  else if (command === "capture") capture(args);
  else if (command === "render") render(args);
  else if (command === "feature-graphics") featureGraphics(args);
  else if (command === "all") all(args);
  else {
    printHelp();
    if (command !== "help" && command !== "--help" && command !== "-h")
      process.exitCode = 1;
  }
} catch (error) {
  console.error(`\n${error.message}`);
  process.exitCode = 1;
}
