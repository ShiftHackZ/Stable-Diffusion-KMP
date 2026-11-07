# SDAI icon sources

`sdai.png` is the full-color artwork. `sdai-symbol.svg` is its transparent vector adaptation for in-app, notification and adaptive launcher use. Keep the sun, landscape and surrounding colored shapes together.

```sh
node scripts/generate_brand_icons.mjs
```

Sharp must be available to Node. An isolated tools installation can be used without adding application dependencies:

```sh
npm install --prefix /tmp/sdai-icon-tools --no-save sharp@0.35.2
NODE_PATH=/tmp/sdai-icon-tools/node_modules node scripts/generate_brand_icons.mjs
```

Android FOSS and FULL launcher resources add a blue (`#007BFE`) diagonal ribbon with white labels across the top-left corner. Playstore, iOS, notifications, splash screens, in-app logos and public/store artwork use the neutral icon. The themed Android variants retain the label as a cut-out in the monochrome ribbon.

The adaptive layer is 108dp with the artwork placed on a centered 72dp canvas. Its silhouette fits inside the central 66dp safe region. The splash has its own resource names so Android flavor overrides cannot add a launcher ribbon to it.

To refresh only existing feature graphics after changing the canonical website icon, preserving their text and the application screenshots (requires `rsvg-convert`):

```sh
node scripts/generate_store_screenshots.mjs feature-graphics --locales en-US,ru,uk --targets fastlane,googleplay
```
