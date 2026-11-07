# Nightly Android Builds

SDAI publishes an Android `full` flavor nightly APK from `develop` through GitHub Actions.

The public APK URL is stable:

```text
https://github.com/ShiftHackZ/SDAI-KMP/releases/download/nightly/sdai-full-nightly.apk
```

The release page is:

```text
https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/nightly
```

## What Gets Built

- Android only.
- `full` flavor only.
- Gradle produces an unsigned release APK; CI signs that APK with Android SDK `apksigner`.
- The scheduled workflow runs daily and skips the build when only docs, website files, or repository metadata changed since the previous nightly.
- Manual `force` builds can publish a new artifact even when the functional-file check would skip.

The workflow does not commit generated files or changing build metadata back to the repository.

## Publication Model

Nightlies are published to one GitHub prerelease named `Android Full Nightly`, backed by the moving tag `nightly`.

The workflow force-moves the `nightly` tag to the built commit, removes obsolete uploaded assets from the nightly release, and uploads the current assets with fixed filenames using overwrite mode. The Releases page should therefore show one nightly release, and that release should contain only the latest APK and checksum assets.

The workflow does not upload separate GitHub Actions artifacts, so APK downloads are kept in the single current nightly release.

## Signing Model

Nightly signing does not use JKS or Gradle `signing.properties`.

The workflow signs the unsigned APK with Android SDK `apksigner` using:

- `ANDROID_NIGHTLY_KEY_PK8_BASE64`: Base64-encoded plain PKCS#8 private key file.
- `ANDROID_NIGHTLY_CERT_PEM_BASE64`: Base64-encoded X.509 certificate file.

These values should be stored as repository or environment secrets. Do not commit them to the repository.

If the nightly key is different from the normal `full` release signing certificate, Android will not install a nightly APK as an update over an existing `com.shifthackz.aisdv1.app.full` build. Testers must uninstall the old build first, or the nightly signing certificate must match the installed build.

## Manual Run

GitHub only exposes `workflow_dispatch` for workflow files that exist on the repository default branch. Keep `.github/workflows/nightly_android.yml` on `master`; the workflow still builds `develop` through the `target_ref` input.

Run from the GitHub UI:

1. Open `Actions`.
2. Select `Android Full Nightly`.
3. Click `Run workflow`.
4. Keep `target_ref` as `develop`.
5. Set `force` to `true` when you want a build even if no functional files changed.
6. Keep `publish` as `true` to overwrite the public nightly prerelease assets.

Run from GitHub CLI:

```bash
gh workflow run nightly_android.yml --ref master -f target_ref=develop -f force=true -f publish=true
```

After the job finishes, send the stable APK URL above to testers.

## Daily Cron

The workflow runs once per day:

```yaml
schedule:
  - cron: "37 2 * * *"
```

Scheduled workflows run from the default branch, while the checkout step uses `develop` as the default build target.

## F-Droid Safety

Nightlies use the non-version, moving `nightly` tag and the GitHub release is marked as a prerelease. It is not a stable release tag, and scheduled builds point the moving tag at the selected build target, normally `develop`.

F-Droid release automation should continue to use its normal versioned tags from `master`; do not configure F-Droid metadata to match the `nightly` tag.
