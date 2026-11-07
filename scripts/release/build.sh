#!/usr/bin/env bash
# Build one unsigned variant from a clean checkout using the pinned native tools.
set -euo pipefail

source_dir="$(cd "${1:?Source directory required}" && pwd)"
flavor="${2:?Flavor required}"
format="${3:?Format required}"
output_dir="${4:?Output directory required}"
expected_sha="${5:?Source SHA required}"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

case "$flavor" in foss|full|playstore) ;; *) exit 2 ;; esac
case "$format" in apk) task="assemble${flavor^}Release" ;; aab) task="bundle${flavor^}Release" ;; *) exit 2 ;; esac
: "${SDAI_GRADLE:?Run setup.sh in build mode first}"
: "${ANDROID_HOME:?Android SDK required}"
: "${JAVA_HOME:?JDK required}"
: "${GRADLE_USER_HOME:?Isolated Gradle home required}"

cd "$source_dir"
[[ "$(git rev-parse HEAD)" == "$expected_sha" ]] || { echo 'Source revision changed' >&2; exit 1; }
[[ -z "$(git status --porcelain --untracked-files=normal)" ]] || { echo 'A clean source checkout is required' >&2; exit 1; }
[[ ! -e app/keystore/signing.properties ]] || { echo 'Release builds must not receive signing credentials' >&2; exit 1; }
[[ ! -e local.properties ]] || { echo 'Release builds must not inherit local SDK configuration' >&2; exit 1; }
git submodule status --recursive > "${RUNNER_TEMP:-/tmp}/sdai-submodules.txt"
if grep -Eq '^[-+U]' "${RUNNER_TEMP:-/tmp}/sdai-submodules.txt"; then
    echo 'Submodule revisions do not match the selected source commit' >&2
    exit 1
fi

export TZ=UTC LC_ALL=C.UTF-8 LANG=C.UTF-8
export SOURCE_DATE_EPOCH
SOURCE_DATE_EPOCH="$(git show -s --format=%ct HEAD)"
export CMAKE_BUILD_PARALLEL_LEVEL=2
export SDAI_REAL_NINJA="${ANDROID_HOME}/cmake/3.22.1/bin/ninja"
export SDAI_NINJA_WRAPPER="${script_dir}/ninja.sh"
export SDAI_RELEASE_PROFILE="${script_dir}/toolchain.json"
mkdir -p "$output_dir"
output_dir="$(cd "$output_dir" && pwd)"

# No restored task outputs or compiler caches participate in either build.
/usr/bin/time -v "$SDAI_GRADLE" ":app:${task}" \
    --init-script "${script_dir}/release.init.gradle" \
    --no-daemon --no-parallel --no-build-cache --no-configuration-cache \
    --max-workers=2 --stacktrace \
    -Dorg.gradle.java.home="$JAVA_HOME" \
    '-Dorg.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8' \
    -Dorg.gradle.vfs.watch=false \
    -Pkotlin.compiler.execution.strategy=in-process \
    -Porg.gradle.java.installations.auto-download=false \
    -Porg.gradle.java.installations.auto-detect=false \
    -Porg.gradle.java.installations.paths="$JAVA_HOME" \
    -Pandroid.builder.sdkDownload=false \
    -Pkotlin.native.ignoreDisabledTargets=true \
    -Dkotlin.native.ignoreDisabledTargets=true \
    2>&1 | tee "${output_dir}/build.log"

if [[ "$format" == apk ]]; then
    artifact_dir="app/android/build/outputs/apk/${flavor}/release"
else
    artifact_dir="app/android/build/outputs/bundle/${flavor}Release"
fi
mapfile -t artifacts < <(find "$artifact_dir" -maxdepth 1 -type f -name "*.${format}" -print)
[[ "${#artifacts[@]}" == 1 ]] || { echo 'Expected exactly one unsigned artifact' >&2; exit 1; }
cp "${artifacts[0]}" "${output_dir}/unsigned.${format}"
cp "${script_dir}/toolchain.json" "${output_dir}/toolchain.json"
cp "${RUNNER_TEMP:-/tmp}/sdai-submodules.txt" "${output_dir}/submodules.txt"
{
    printf 'Source SHA: %s\nSource epoch: %s\n' "$expected_sha" "$SOURCE_DATE_EPOCH"
    "$JAVA_HOME/bin/java" -version 2>&1
    "$SDAI_GRADLE" --version
    uname -a
    cat /etc/os-release
    free -h
    df -h "$source_dir"
    command -v c++ >/dev/null && c++ --version
} > "${output_dir}/environment.txt"
