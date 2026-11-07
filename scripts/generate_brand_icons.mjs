#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";

const require = createRequire(import.meta.url);
const sharp = require("sharp");
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const source = path.join(root, "artwork/icon/sdai.png");
const symbol = fs.readFileSync(
  path.join(root, "artwork/icon/sdai-symbol.svg"),
  "utf8"
);
const viewBox = symbol.match(/viewBox="([^"]+)"/)[1];
const paths = [...symbol.matchAll(/<path\b[^>]*>/gs)].map((match) => match[0]);
const monochrome = paths
  .map((element) => element.replace(/fill="[^"]+"/g, 'fill="#FFFFFF"'))
  .join("\n");
const background = "#F5F2EF";
const densities = { mdpi: 48, hdpi: 72, xhdpi: 96, xxhdpi: 144, xxxhdpi: 192 };
const sourceImage = `data:image/png;base64,${fs
  .readFileSync(source)
  .toString("base64")}`;

function write(relative, content) {
  const file = path.join(root, relative);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, content);
}

async function png(input, size, relative, opaque = false) {
  const file = path.join(root, relative);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  let output = sharp(input).resize(size, size);
  if (opaque) output = output.flatten({ background }).removeAlpha();
  await output.png().toFile(file);
}

function svg(body, size = 1024) {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 1024 1024">${body}</svg>`;
}

function mark(body = paths.join("\n")) {
  return `<svg width="1024" height="1024" viewBox="${viewBox}">${body}</svg>`;
}

// Place the diagonal flavor band across the top-left of a 1024px launcher canvas.
// Native SVG text keeps the flavor label crisp at every generated density.
function ribbon(label, color = "#007BFE", textColor = "#FFFFFF") {
  if (!label) return "";
  return `<path d="M0 429 L490 0 H669 L0 606 Z" fill="${color}"/>
    <text x="300" y="260" transform="rotate(-41.7 300 260)" text-anchor="middle"
      dominant-baseline="central" font-family="Arial" font-size="128" font-weight="700"
      fill="${textColor}">${label}</text>`;
}

function launcher(label, adaptive = false, themed = false) {
  let body;
  if (themed) {
    // A cut-out label distinguishes flavors even when Android applies one theme color.
    body = `<defs><mask id="symbol"><rect width="1024" height="1024" fill="black"/>
      ${mark(monochrome)}${ribbon(label, "#FFFFFF", "#000000")}</mask></defs>
      <rect width="1024" height="1024" fill="white" mask="url(#symbol)"/>`;
  } else if (adaptive) {
    body = mark() + ribbon(label);
  } else {
    body = `<image href="${sourceImage}" width="1024" height="1024"/>${ribbon(
      label
    )}`;
  }
  // The 1024px art fits inside 72dp of Android's 108dp adaptive layer.
  // The colored silhouette stays within the central 66dp safe region.
  if (adaptive)
    body = `<g transform="translate(170.6667 170.6667) scale(0.6666667)">${body}</g>`;
  return Buffer.from(svg(body));
}

function adaptiveXml(foreground, monochromeName) {
  return `<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/${foreground}" />
    <monochrome android:drawable="@drawable/${monochromeName}" />
</adaptive-icon>
`;
}

// Use the full-color artwork for opaque store, website and iOS icons.
await png(source, 512, "docs/assets/sdai.png", true);
await png(source, 512, "app/android/src/main/ic_launcher-playstore.png", true);
for (const locale of ["en-US", "ru", "uk"]) {
  await png(
    source,
    512,
    `fastlane/metadata/android/${locale}/images/icon.png`,
    true
  );
}
const catalogDir = "app/ios/iosApp/Assets.xcassets/AppIcon.appiconset";
const catalog = JSON.parse(
  fs.readFileSync(path.join(root, catalogDir, "Contents.json"), "utf8")
);
for (const item of new Map(
  catalog.images.map((item) => [item.filename, item])
).values()) {
  const size = Number.parseFloat(item.size) * Number.parseFloat(item.scale);
  await png(source, size, `${catalogDir}/${item.filename}`, true);
}

// In-app artwork is transparent and never includes a flavor ribbon.
await png(
  Buffer.from(svg(mark())),
  512,
  "presentation/src/commonMain/composeResources/drawable/sdai_logo.png"
);
await png(
  Buffer.from(svg(mark())),
  512,
  "presentation/src/androidMain/res/drawable/ic_sdai_logo.png"
);
fs.rmSync(
  path.join(
    root,
    "presentation/src/androidMain/res/drawable/ic_sdai_logo.webp"
  ),
  { force: true }
);
write(".idea/icon.svg", symbol);

for (const [flavor, label] of [
  ["main", ""],
  ["foss", "FOSS"],
  ["full", "FULL"],
]) {
  const res = `app/android/src/${flavor}/res`;
  for (const [density, size] of Object.entries(densities)) {
    const icon = launcher(label);
    await png(icon, size, `${res}/mipmap-${density}/ic_launcher.png`, true);
    const round = await sharp(icon)
      .resize(size, size)
      .composite([
        {
          input: Buffer.from(
            `<svg width="${size}" height="${size}"><circle cx="${
              size / 2
            }" cy="${size / 2}" r="${size / 2}" fill="white"/></svg>`
          ),
          blend: "dest-in",
        },
      ])
      .png()
      .toBuffer();
    write(`${res}/mipmap-${density}/ic_launcher_round.png`, round);
    await png(
      launcher(label, true),
      size * 2.25,
      `${res}/drawable-${density}/ic_launcher_foreground.png`
    );
    await png(
      launcher(label, true, true),
      size * 2.25,
      `${res}/drawable-${density}/ic_launcher_monochrome.png`
    );
    if (flavor === "main") {
      // Dedicated resources prevent FOSS/FULL launcher overrides reaching the splash.
      await png(
        launcher("", true),
        size * 2.25,
        `${res}/drawable-${density}/ic_splash_foreground.png`
      );
      await png(
        launcher(""),
        size,
        `${res}/mipmap-${density}/ic_splash.png`,
        true
      );
    }
  }
}

const mainRes = "app/android/src/main/res";
for (const name of ["ic_launcher", "ic_launcher_round"]) {
  write(
    `${mainRes}/mipmap-anydpi-v26/${name}.xml`,
    adaptiveXml("ic_launcher_foreground", "ic_launcher_monochrome")
  );
}
write(
  `${mainRes}/mipmap-anydpi-v26/ic_splash.xml`,
  adaptiveXml("ic_splash_foreground", "ic_splash_foreground")
);
write(
  `${mainRes}/values/ic_launcher_background.xml`,
  `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">${background}</color>
</resources>
`
);
fs.rmSync(path.join(root, `${mainRes}/drawable/ic_launcher_foreground.xml`), {
  force: true,
});
fs.rmSync(path.join(root, `${mainRes}/drawable/ic_sd_flat.xml`), {
  force: true,
});

// Android small notification icons use only alpha, never the colored launcher bitmap.
const notificationPaths = paths
  .map((element) => {
    const data = element.match(/\bd="([^"]+)"/)[1];
    return `    <path android:fillColor="#FFFFFFFF" android:pathData="${data}" />`;
  })
  .join("\n");
const [, , width, height] = viewBox.split(/\s+/).map(Number);
write(
  "core/notification/src/androidMain/res/drawable/ic_notification.xml",
  `<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="${width}" android:viewportHeight="${height}">
${notificationPaths}
</vector>
`
);

console.log(
  "Generated SDAI icons; ribbons are limited to Android FOSS/FULL launchers."
);
