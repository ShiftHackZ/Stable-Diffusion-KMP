#!/usr/bin/env bash
# Sign release artifacts in a job that never executes the target source checkout.
set +x
set -euo pipefail
umask 077
export LC_ALL=C

fail() { printf '%s\n' "$1" >&2; exit 1; }
[[ $# == 5 ]] || fail 'Usage: sign.sh <foss|full|playstore> <apk|aab> <unsigned-file> <output-dir> <version-name>'
variant="$1"
extension="$2"
unsigned_file="$3"
output_dir="$4"
version_name="$5"
[[ "$extension" == apk || "$extension" == aab ]] || fail 'Unknown artifact format.'
[[ "$version_name" =~ ^[A-Za-z0-9][A-Za-z0-9._+-]*$ ]] || fail 'Unsupported version name.'
[[ -f "$unsigned_file" && ! -L "$unsigned_file" ]] || fail 'Unsigned artifact must be a regular file.'
case "$variant" in
  foss|full) application_id="com.shifthackz.aisdv1.app.$variant" ;;
  playstore) application_id=com.shifthackz.aisdv1.app ;;
  *) fail 'Unknown release variant.' ;;
esac
[[ "$unsigned_file" == *."$extension" ]] || fail 'Artifact extension does not match the variant.'
: "${JAVA_HOME:?JAVA_HOME must point to the pinned JDK.}"
sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
[[ -n "$sdk_root" ]] || fail 'ANDROID_SDK_ROOT or ANDROID_HOME is required.'
build_tools_version="${ANDROID_BUILD_TOOLS_VERSION:-35.0.0}"
[[ "$build_tools_version" == 35.0.0 ]] || fail 'This signing profile requires Android Build Tools 35.0.0.'
build_tools="$sdk_root/build-tools/$build_tools_version"
keytool="$JAVA_HOME/bin/keytool"
jarsigner="$JAVA_HOME/bin/jarsigner"
[[ -x "$keytool" && -x "$jarsigner" ]] || fail 'Pinned JDK signing tools are missing.'

# A partial Play Store configuration must never fall back to the release key.
prefix=ANDROID_RELEASE
if [[ "$variant" == playstore && -n "${ANDROID_PLAY_KEYSTORE_BASE64:-}${ANDROID_PLAY_STORE_PASSWORD:-}${ANDROID_PLAY_KEY_ALIAS:-}${ANDROID_PLAY_KEY_PASSWORD:-}${ANDROID_PLAY_CERT_SHA256:-}" ]]; then
  prefix=ANDROID_PLAY
fi
for suffix in KEYSTORE_BASE64 STORE_PASSWORD KEY_ALIAS CERT_SHA256; do
  variable="${prefix}_${suffix}"
  [[ -n "${!variable:-}" ]] || fail "Missing signing input: $variable"
done
variable="${prefix}_KEYSTORE_BASE64"; keystore_base64="${!variable}"
variable="${prefix}_STORE_PASSWORD"; export SDAI_SIGN_STORE_PASSWORD="${!variable}"
variable="${prefix}_KEY_ALIAS"; key_alias="${!variable}"
variable="${prefix}_KEY_PASSWORD"; export SDAI_SIGN_KEY_PASSWORD="${!variable:-$SDAI_SIGN_STORE_PASSWORD}"
variable="${prefix}_CERT_SHA256"; expected_cert="${!variable}"
expected_cert="$(printf '%s' "$expected_cert" | tr -d '[:space:]:' | tr '[:upper:]' '[:lower:]')"
[[ "$expected_cert" =~ ^[0-9a-f]{64}$ ]] || fail 'Expected certificate must be a SHA-256 fingerprint.'
unset ANDROID_RELEASE_KEYSTORE_BASE64 ANDROID_RELEASE_STORE_PASSWORD ANDROID_RELEASE_KEY_ALIAS ANDROID_RELEASE_KEY_PASSWORD
unset ANDROID_PLAY_KEYSTORE_BASE64 ANDROID_PLAY_STORE_PASSWORD ANDROID_PLAY_KEY_ALIAS ANDROID_PLAY_KEY_PASSWORD

signing_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/sdai-signing.XXXXXX")"
trap 'rm -rf -- "$signing_dir"' EXIT
trap 'exit 1' HUP INT TERM
keystore="$signing_dir/release.jks"
printf '%s' "$keystore_base64" | base64 --decode > "$keystore" || fail 'Could not decode the signing keystore.'
unset keystore_base64
"$keytool" -exportcert -keystore "$keystore" -storepass:env SDAI_SIGN_STORE_PASSWORD \
  -alias "$key_alias" -file "$signing_dir/signer.der" > "$signing_dir/keytool.log" 2>&1 \
  || fail 'Could not read the configured signing certificate.'
actual_cert="$(sha256sum "$signing_dir/signer.der" | cut -d ' ' -f 1)"
[[ "$actual_cert" == "$expected_cert" ]] || fail 'Keystore certificate does not match the expected SHA-256 fingerprint.'

filename="sdai-$variant-release-$version_name.$extension"
signed_file="$signing_dir/$filename"
mkdir -p "$output_dir"
[[ ! -e "$output_dir/$filename" ]] || fail 'The signed output already exists.'
unzip -Z1 "$unsigned_file" > "$signing_dir/entries.txt" || fail 'Artifact is not a readable ZIP archive.'
if grep -Eiq '^META-INF/[^/]+\.(SF|RSA|DSA|EC)$' "$signing_dir/entries.txt"; then
  fail 'Input contains an existing JAR signature.'
fi

if [[ "$extension" == apk ]]; then
  for tool in aapt2 zipalign apksigner; do
    [[ -x "$build_tools/$tool" ]] || fail "Missing pinned Android signing tool: $tool"
  done
  "$build_tools/aapt2" dump badging "$unsigned_file" > "$signing_dir/badging.txt" 2> "$signing_dir/aapt2.log" \
    || fail 'Could not inspect APK metadata.'
  package_line="$(sed -n '/^package: /{p;q;}' "$signing_dir/badging.txt")"
  actual_id="$(printf '%s\n' "$package_line" | sed -n "s/.*name='\([^']*\)'.*/\1/p")"
  actual_version="$(printf '%s\n' "$package_line" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p")"
  [[ "$actual_id" == "$application_id" && "$actual_version" == "$version_name" ]] \
    || fail 'APK application ID or version does not match the release request.'
  if grep -q '^application-debuggable' "$signing_dir/badging.txt"; then
    fail 'Refusing to sign a debuggable APK.'
  fi
  if "$build_tools/apksigner" verify "$unsigned_file" > "$signing_dir/input-signature.log" 2>&1; then
    fail 'APK input is already signed.'
  fi
  # AGP supplies alignment; rewriting the ZIP here would change the rebuild input.
  "$build_tools/zipalign" -c -P 16 4 "$unsigned_file" > "$signing_dir/alignment.log" 2>&1 \
    || fail 'APK alignment verification failed; rebuild with the pinned toolchain.'
  "$build_tools/apksigner" sign --ks "$keystore" --ks-key-alias "$key_alias" \
    --ks-pass env:SDAI_SIGN_STORE_PASSWORD --key-pass env:SDAI_SIGN_KEY_PASSWORD \
    --alignment-preserved true --v4-signing-enabled false --v1-signer-name SDAI \
    --out "$signed_file" "$unsigned_file" \
    > "$signing_dir/sign.log" 2>&1 || fail 'APK signing failed.'
  "$build_tools/apksigner" verify --verbose --print-certs "$signed_file" > "$signing_dir/verify.log" 2>&1 \
    || fail 'Signed APK verification failed.'
  actual_cert="$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' "$signing_dir/verify.log" | tr '[:upper:]' '[:lower:]')"
  "$build_tools/zipalign" -c -P 16 4 "$signed_file" > "$signing_dir/alignment.log" 2>&1 \
    || fail 'Signed APK alignment verification failed.'
else
  grep -qx 'base/manifest/AndroidManifest.xml' "$signing_dir/entries.txt" || fail 'Input is not an Android App Bundle.'
  "$jarsigner" -keystore "$keystore" -storepass:env SDAI_SIGN_STORE_PASSWORD \
    -keypass:env SDAI_SIGN_KEY_PASSWORD -sigfile SDAI -digestalg SHA-256 \
    -signedjar "$signed_file" "$unsigned_file" "$key_alias" > "$signing_dir/sign.log" 2>&1 \
    || fail 'App Bundle signing failed.'
  # Explicitly trust only the fingerprint-checked Android signing certificate.
  "$keytool" -importcert -noprompt -alias sdai -file "$signing_dir/signer.der" \
    -keystore "$signing_dir/trust.jks" -storetype JKS -storepass:env SDAI_SIGN_STORE_PASSWORD \
    > "$signing_dir/trust.log" 2>&1 || fail 'Could not prepare certificate verification.'
  "$jarsigner" -verify -strict -keystore "$signing_dir/trust.jks" \
    -storepass:env SDAI_SIGN_STORE_PASSWORD "$signed_file" sdai > "$signing_dir/verify.log" 2>&1 \
    || fail 'Signed App Bundle verification failed.'
  "$keytool" -printcert -jarfile "$signed_file" > "$signing_dir/certificate.log" 2>&1 \
    || fail 'Could not inspect the App Bundle signing certificate.'
  actual_cert="$(awk '/^[[:space:]]*SHA256:/ {print $2; exit}' "$signing_dir/certificate.log" | tr -d ':' | tr '[:upper:]' '[:lower:]')"
fi
[[ "$actual_cert" == "$expected_cert" ]] || fail 'Signed artifact certificate does not match the expected fingerprint.'
unset SDAI_SIGN_STORE_PASSWORD SDAI_SIGN_KEY_PASSWORD key_alias

mv "$signed_file" "$output_dir/$filename"
(cd "$output_dir" && sha256sum "$filename" > "$filename.sha256")
{
  printf 'artifact=%s\nvariant=%s\nversion_name=%s\n' "$filename" "$variant" "$version_name"
  printf 'signer_certificate_sha256=%s\nsignature_verified=true\n' "$actual_cert"
  if [[ "$extension" == apk ]]; then
    printf 'application_id=%s\nalignment_verified=true\n' "$application_id"
    grep '^Verified using ' "$signing_dir/verify.log"
  fi
} > "$output_dir/$filename.signer.txt"
printf 'Signed and verified %s\n' "$filename"
