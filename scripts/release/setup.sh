#!/usr/bin/env bash
# Install the release toolchain into a new directory without using runner SDK caches.
# Usage: setup.sh <build|sign> <empty-toolchain-directory> [empty-android-sdk-directory]
set -euo pipefail

fail() { printf 'Toolchain setup: %s\n' "$*" >&2; exit 1; }
[[ $# == 2 || $# == 3 ]] || fail 'expected mode, empty toolchain directory and optional empty SDK directory'
[[ $(uname -s) == Linux && $(uname -m) == x86_64 ]] || fail 'Linux x86_64 is required'
mode=$1
[[ $mode == build || $mode == sign ]] || fail 'mode must be build or sign'
for command in curl jq sha256sum tar unzip awk; do
  command -v "$command" >/dev/null || fail "missing command: $command"
done

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
profile="$script_dir/toolchain.json"
mkdir -p -- "$2"
toolchain_root=$(cd -- "$2" && pwd)
[[ -z $(ls -A -- "$toolchain_root") ]] || fail 'toolchain directory must be empty'
mkdir -p -- "${3:-$toolchain_root/android-sdk}"
android_sdk_root=$(cd -- "${3:-$toolchain_root/android-sdk}" && pwd)
[[ -z $(ls -A -- "$android_sdk_root") ]] || fail 'Android SDK directory must be empty'
work=$(mktemp -d "$toolchain_root/.download.XXXXXX")
trap 'rm -rf -- "$work"' EXIT

# Every download is checked before extraction. SHA-256 values for Android archives
# were calculated after validating Google's repository XML SHA-1 checksums.
install_artifact() {
  local name=$1 url digest archive_root destination relative_destination archive stage expected actual
  url=$(jq -er --arg name "$name" '.artifacts[$name].url' "$profile")
  digest=$(jq -er --arg name "$name" '.artifacts[$name].sha256' "$profile")
  relative_destination=$(jq -er --arg name "$name" '.artifacts[$name].destination' "$profile")
  if [[ $relative_destination == android-sdk/* ]]; then
    destination="$android_sdk_root/${relative_destination#android-sdk/}"
  else
    destination="$toolchain_root/$relative_destination"
  fi
  archive="$work/$name.archive"
  printf 'Installing %s\n' "$name"
  curl --fail --location --retry 3 --proto '=https' --tlsv1.2 --output "$archive" "$url"
  printf '%s  %s\n' "$digest" "$archive" | sha256sum --check --status
  mkdir -p -- "$(dirname -- "$destination")"
  if [[ $name == bundletool ]]; then
    mv -- "$archive" "$destination"
    return
  fi
  stage="$work/$name"
  mkdir -- "$stage"
  if [[ $url == *.tar.gz ]]; then
    tar -xzf "$archive" -C "$stage"
  else
    unzip -q "$archive" -d "$stage"
  fi
  archive_root=$(jq -er --arg name "$name" '.artifacts[$name].archiveRoot' "$profile")
  if [[ $archive_root == . ]]; then
    mv -- "$stage" "$destination"
  else
    mv -- "$stage/$archive_root" "$destination"
  fi
  rm -- "$archive"
  expected=$(jq -r --arg name "$name" '.artifacts[$name].revision // empty' "$profile")
  if [[ -n $expected ]]; then
    actual=$(awk -F= '/^Pkg.Revision[[:space:]]*=/ {gsub(/[[:space:]]/, "", $2); print $2}' "$destination/source.properties")
    [[ $actual == "$expected" ]] || fail "$name revision is $actual, expected $expected"
  fi
}

install_artifact jdk
install_artifact buildTools
install_artifact bundletool
if [[ $mode == build ]]; then
  for artifact in gradle platform ndk cmake; do
    install_artifact "$artifact"
  done
fi

export JAVA_HOME="$toolchain_root/jdk"
export ANDROID_HOME="$android_sdk_root"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export SDAI_TOOLCHAIN_DIR="$toolchain_root"
export BUNDLETOOL_JAR="$toolchain_root/bundletool.jar"
build_tools=$(jq -er '.buildToolsVersion' "$profile")
export ANDROID_BUILD_TOOLS_VERSION="$build_tools"
cmake=$(jq -er '.cmakeVersion' "$profile")
mkdir -p -- "$ANDROID_HOME/licenses"
jq -er '.androidSdkLicenseSha1' "$profile" > "$ANDROID_HOME/licenses/android-sdk-license"
cp -- "$profile" "$toolchain_root/toolchain.json"

environment="$toolchain_root/environment.sh"
printf '# Source this file to use this release toolchain.\n' > "$environment"
record_env() {
  local name=$1 value=$2
  printf 'export %s=%q\n' "$name" "$value" >> "$environment"
  if [[ -n ${GITHUB_ENV:-} ]]; then
    printf '%s=%s\n' "$name" "$value" >> "$GITHUB_ENV"
  fi
}
for name in JAVA_HOME ANDROID_HOME ANDROID_SDK_ROOT SDAI_TOOLCHAIN_DIR BUNDLETOOL_JAR ANDROID_BUILD_TOOLS_VERSION; do
  record_env "$name" "${!name}"
done
tool_paths=("$JAVA_HOME/bin" "$ANDROID_HOME/build-tools/$build_tools")
if [[ $mode == build ]]; then
  export GRADLE_HOME="$toolchain_root/gradle"
  export GRADLE_USER_HOME="$toolchain_root/gradle-user-home"
  export SDAI_GRADLE="$GRADLE_HOME/bin/gradle"
  mkdir -p -- "$GRADLE_USER_HOME"
  for name in GRADLE_HOME GRADLE_USER_HOME SDAI_GRADLE; do
    record_env "$name" "${!name}"
  done
  tool_paths+=("$ANDROID_HOME/cmake/$cmake/bin" "$GRADLE_HOME/bin")
fi
path_prefix=$(IFS=:; printf '%s' "${tool_paths[*]}")
# Preserve the caller's PATH when this generated file is sourced later.
# shellcheck disable=SC2016
printf 'export PATH=%q:"$PATH"\n' "$path_prefix" >> "$environment"
if [[ -n ${GITHUB_PATH:-} ]]; then
  printf '%s\n' "${tool_paths[@]}" >> "$GITHUB_PATH"
fi

jdk_version=$(jq -er '.jdkVersion' "$profile")
grep -Fq "JAVA_RUNTIME_VERSION=\"$jdk_version\"" "$JAVA_HOME/release" || fail 'unexpected JDK runtime version'
"$JAVA_HOME/bin/java" -version
"$JAVA_HOME/bin/java" -jar "$BUNDLETOOL_JAR" version
printf 'Toolchain ready: %s\n' "$toolchain_root"
