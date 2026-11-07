# Android release workflow

[Android Release](../../.github/workflows/release_android.yml) builds selected Android
artifacts on native Ubuntu 24.04 x86-64 GitHub runners. Each artifact is built twice
on separate fresh runners. Only byte-identical unsigned outputs with the expected
package, version and embedded Git revision proceed to signing. Signing is enabled
by default; no separate unsigned run or existing-release reference is required.
Signed APKs then pass signature-copy verification against the independent rebuild.

## Select outputs

Run the workflow from the repository's default branch and set `target_ref` to the
branch, tag or commit to build. The workflow resolves that input once to a commit
SHA and checks out its exact recursive submodule revisions.

Each flavor has an independent `skip`, `apk`, `aab` or `both` selector:

| Flavor     | Default | APK task                        | AAB task                      |
| ---------- | ------- | ------------------------------- | ----------------------------- |
| FOSS       | `apk`   | `:app:assembleFossRelease`      | `:app:bundleFossRelease`      |
| Full       | `apk`   | `:app:assembleFullRelease`      | `:app:bundleFullRelease`      |
| Play Store | `aab`   | `:app:assemblePlaystoreRelease` | `:app:bundlePlaystoreRelease` |

At least one output must be selected. `both` produces two independent outputs for
that flavor. For example, this builds and signs only the FOSS APK and both Full
formats:

```sh
gh workflow run release_android.yml \
  -f target_ref=master \
  -f foss=apk \
  -f full=both \
  -f playstore=skip
```

Set `sign=false` for an unsigned verification run that needs no keystore secrets.
Signing is restricted to workflow runs from the default branch; `target_ref` can
still select a feature branch. Workflow tools come from the workflow's own commit,
independently of the selected application source.

## Configure signing

Create the GitHub environment `android-release` and restrict its deployment
branches to the repository's default branch. Build and verification jobs do not
use this environment. The signing job downloads verified artifacts and never
checks out or executes the selected application source.

Configure these environment secrets:

| Secret                            | Value                                                        |
| --------------------------------- | ------------------------------------------------------------ |
| `ANDROID_RELEASE_KEYSTORE_BASE64` | Base64 encoding of the existing release JKS                  |
| `ANDROID_RELEASE_STORE_PASSWORD`  | JKS password                                                 |
| `ANDROID_RELEASE_KEY_ALIAS`       | Existing signing key alias                                   |
| `ANDROID_RELEASE_KEY_PASSWORD`    | Optional separate key password; defaults to the JKS password |

Set the environment variable `ANDROID_RELEASE_CERT_SHA256` to the signing
certificate's SHA-256 fingerprint. Both colon-separated and plain hexadecimal
fingerprints are accepted. The signer checks it before signing and checks the
resulting artifact's certificate again afterwards.

The keystore can be transferred directly from a local file without writing an
encoded copy into the repository:

```sh
base64 < /path/to/release.jks |
  gh secret set ANDROID_RELEASE_KEYSTORE_BASE64 --env android-release
gh secret set ANDROID_RELEASE_STORE_PASSWORD --env android-release
gh secret set ANDROID_RELEASE_KEY_ALIAS --env android-release
gh variable set ANDROID_RELEASE_CERT_SHA256 --env android-release
```

The password and alias commands prompt for their values. Keep the original local
JKS backup. The temporary signing copy is deleted when the signing process exits.

By default, Play Store artifacts use the same release key. If Play Console has a
different upload key, configure `ANDROID_PLAY_KEYSTORE_BASE64`,
`ANDROID_PLAY_STORE_PASSWORD`, `ANDROID_PLAY_KEY_ALIAS`, optionally
`ANDROID_PLAY_KEY_PASSWORD`, and variable `ANDROID_PLAY_CERT_SHA256`. A partial
Play configuration fails instead of falling back to another key. These settings
apply to both APK and AAB outputs for the Play Store flavor.

APKs use `apksigner` with alignment preservation. After signing, the workflow uses
`compare_apk.py` to verify the signed APK against the second independent CI build.
The same helper supports local comparison against F-Droid APKs. AABs use `jarsigner`; the upload
key must match Play Console's registered upload certificate. Google Play's app
signing certificate can be different from the upload certificate.

## Toolchain and memory

[toolchain.json](toolchain.json) pins download URLs and SHA-256 checksums for:

- Temurin OpenJDK 17.0.21+9
- Gradle 8.13
- Android SDK platform 36, revision 2
- Android Build Tools 35.0.0
- Android NDK 27.0.12077973
- CMake 3.22.1, including its Ninja executable
- bundletool 1.18.3

The workflow installs these tools in a new directory. Build jobs reclaim the
preinstalled Android SDK's space on disposable GitHub-hosted runners before
installing the pinned SDK. Android archive checksums were calculated from the
official downloads and cross-checked against Google's repository metadata. JDK,
Gradle and bundletool checksum sources are recorded in the profile.

Gradle runs with a 4 GiB heap, at most two workers, no parallel project execution,
and the Kotlin compiler inside the Gradle process. Native Ninja compilation is limited
to two workers, including direct Ninja invocations from AGP. Each selected output
and each rebuild use separate runners and fresh Gradle homes. Build logs include
process resource usage; environment reports include runner memory and disk space.

The current profile requires the target revision to use Gradle 8.13, compile SDK
36 and the `:app` module at `app/android`. Older revisions with incompatible build
layouts or tool versions need a corresponding profile and output path update.
The source's AGP, Kotlin and library versions remain selected by its own build
files. The profile does not freeze Ubuntu runner images or Maven repositories;
environment reports and independent rebuild checks expose differences.

For a local native Linux build, use a clean source checkout with initialized
submodules and a separate checkout containing these workflow tools:

```sh
bash /path/to/ci/scripts/release/setup.sh build /path/to/empty-toolchain-directory
source /path/to/empty-toolchain-directory/environment.sh
bash /path/to/ci/scripts/release/build.sh \
  /path/to/source foss apk /path/to/output FULL_SOURCE_COMMIT_SHA
```

The build helper rejects local SDK and signing configuration in the source
checkout. It applies the SDK/NDK/CMake profile through a Gradle init script without
changing tracked source files.

## First signed run and local F-Droid comparison

Configure the signing environment, then run the workflow with signing enabled for
an existing version. For FOSS 1.1.2 (version code 209), use the source commit from
the [F-Droid recipe](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.shifthackz.aisdv1.app.foss.yml):

```sh
gh workflow run release_android.yml --ref master \
  -f target_ref=9da52144b82235629231a59e32b40a31103b77cc \
  -f foss=apk -f full=skip -f playstore=skip -f sign=true
```

Leave both `foss_reference_*` inputs empty. The historical `1.1.2` tag points at a
different revision, so use the full commit above. Download and extract the signed
`sdai-foss-apk-1.1.2` workflow artifact after the run.

Compare that signed CI APK locally against the published F-Droid APK of the same
version. [F-Droid verifies reproducibility](https://f-droid.org/docs/Reproducible_Builds/)
by copying an upstream APK's signature onto the independent APK and verifying the
result with `apksigner`. The helper uses the same method through
[`apksigcopier compare`](https://github.com/obfusk/apksigcopier#compare-copy--verify).
For a signed comparison APK, its previous signature is excluded during copying;
both original APK signatures are also verified. Neither input is modified.

Local verification needs Python 3, Java, and Android Build Tools 35.0.0 on macOS or
Linux. It needs no keystore, Linux build machine or Docker. Use an installed SDK
for the local operating system; `setup.sh` installs Linux binaries only.

```sh
python3 -m venv /tmp/sdai-apk-verify
/tmp/sdai-apk-verify/bin/pip install --require-hashes -r scripts/release/requirements.txt
export PATH="/tmp/sdai-apk-verify/bin:$ANDROID_HOME/build-tools/35.0.0:$PATH"

curl --fail --location --proto '=https' --proto-redir '=https' \
  --output /tmp/fdroid-foss-209.apk \
  https://f-droid.org/repo/com.shifthackz.aisdv1.app.foss_209.apk

/tmp/sdai-apk-verify/bin/python scripts/release/compare_apk.py \
  /path/to/sdai-foss-release-1.1.2.apk /tmp/fdroid-foss-209.apk \
  --expected-cert-sha256 8974efc853e2d5e93eb29dd809fc57cae7411fcfaca03de3f7c152f441a90064 \
  --comparison-sha256 cbe6e5d50ee33a92d6903288d483644d708bb241fb565720288ce6d5d1dcde56 \
  --report /tmp/sdai-fdroid-comparison.json
```

The checksum identifies the published F-Droid 1.1.2 APK and is also recorded in its
[verification report](https://verification.f-droid.org/unsigned/com.shifthackz.aisdv1.app.foss_209.apk.json).
The helper checks the release certificate, requires a v2/v3 signature (which covers
ZIP metadata as well as file contents), checks the comparison APK checksum, and
fails if signature copying or verification fails. A successful report has
`status: verified`. No private key is used during comparison.

For an actual unsigned APK from an independent F-Droid build, use `--unsigned`:

```sh
/tmp/sdai-apk-verify/bin/python scripts/release/compare_apk.py \
  /path/to/ci-signed-foss.apk /path/to/fdroid-rebuild-unsigned.apk \
  --unsigned \
  --expected-cert-sha256 8974efc853e2d5e93eb29dd809fc57cae7411fcfaca03de3f7c152f441a90064 \
  --report /tmp/sdai-fdroid-rebuild-comparison.json
```

The unsigned input must come from a separate F-Droid recipe build or its pipeline
artifacts. The verification server may publish a JSON report without serving the
raw unsigned APK. For existing releases, the signed published APK above is usable
directly. Comparing it proves equivalence to that already verified F-Droid release;
it does not execute a fresh F-Droid build. Two CI builds only establish repeatability
within this workflow's environment. Reports record `fdroid_pipeline_execution: not_run`.

For new releases, finalize the release tag before building and ensure F-Droid
selects the same commit. The APK signature-copy method does not apply to AABs;
the workflow compares unsigned AABs and verifies their signatures separately.

### Optional comparison before signing

To compare the FOSS APK against an existing signed release, select the FOSS APK and
provide both `foss_reference_url` and `foss_reference_sha256`. The reference must
be a GitHub release asset or an F-Droid repository APK. The workflow verifies its
download checksum and signing certificate, then uses pinned `apksigcopier` to
copy its signature to the unsigned rebuild and verify it. A mismatch fails the job
and prevents signing.

The reference certificate defaults to F-Droid's published SDAI certificate:

```text
8974efc853e2d5e93eb29dd809fc57cae7411fcfaca03de3f7c152f441a90064
```

A repository-level `ANDROID_RELEASE_CERT_SHA256` variable can override that
reference certificate. Verification jobs intentionally do not access the signing
environment. The environment-level variable remains mandatory for signing.

## Artifacts

Each output has its own downloadable artifact:

- `unsigned-<flavor>-<format>-<attempt>`: build output, log and environment evidence.
- `verified-<flavor>-<format>`: matching unsigned output, metadata and validation reports.
- `sdai-<flavor>-<format>-<version>`: signed output, SHA-256 checksum and signer report.

Signed filenames are `sdai-<flavor>-release-<version>.apk` or `.aab`. Metadata
records the source commit, version, unsigned checksum and reference comparison
status. Signed APK artifacts also contain `signature-copy-comparison.json` from
comparison with the independent CI rebuild. ZIP entry differences are retained if
the two unsigned builds disagree.

Artifacts are retained for 14 days. The workflow does not publish GitHub releases,
move tags, upload to Play Console or submit F-Droid metadata. Download the verified
signed files for the normal release process. AAB repeatability is checked before
signing; JAR signing metadata may differ between signing runs.
