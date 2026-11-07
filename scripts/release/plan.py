#!/usr/bin/env python3
"""Resolve the selected release artifacts from a checked-out source revision."""

import json
import os
import re
import subprocess
import sys
import tomllib
from pathlib import Path
from urllib.parse import urlparse


def main():
    source = Path(sys.argv[1]).resolve()
    sha = subprocess.check_output(
        ["git", "-C", str(source), "rev-parse", "HEAD"], text=True
    ).strip()
    versions = tomllib.loads((source / "gradle/libs.versions.toml").read_text())["versions"]
    version_name = str(versions["versionName"])
    version_code = str(versions["versionCode"])
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._+-]{0,99}", version_name):
        raise ValueError("versionName must be safe for an artifact filename")
    if not version_code.isdecimal() or not 0 < int(version_code) <= 2100000000:
        raise ValueError("Invalid Android versionCode")

    targets = []
    for flavor in ("foss", "full", "playstore"):
        selection = os.environ[flavor.upper() + "_OUTPUT"]
        if selection not in ("skip", "apk", "aab", "both"):
            raise ValueError("Unknown output selection: " + selection)
        for kind in ("apk", "aab"):
            if selection not in (kind, "both"):
                continue
            task = ("assemble" if kind == "apk" else "bundle") + flavor.capitalize() + "Release"
            targets.append({"flavor": flavor, "format": kind, "task": ":app:" + task})
    if not targets:
        raise ValueError("Select at least one APK or AAB")

    reference = os.environ.get("FOSS_REFERENCE_URL", "")
    checksum = os.environ.get("FOSS_REFERENCE_SHA256", "")
    if reference or checksum:
        parsed = urlparse(reference)
        if parsed.scheme != "https" or parsed.username or parsed.password:
            raise ValueError("The FOSS reference must use a public HTTPS URL")
        if not re.fullmatch(r"[a-fA-F0-9]{64}", checksum):
            raise ValueError("Provide the reference APK's complete SHA-256")
        if not any(t["flavor"] == "foss" and t["format"] == "apk" for t in targets):
            raise ValueError("A reference APK requires the FOSS APK selection")

    profile = json.loads(Path(__file__).with_name("toolchain.json").read_text())
    wrapper = (source / "gradle/wrapper/gradle-wrapper.properties").read_text()
    if not re.search(r"/gradle-" + re.escape(profile["gradleVersion"]) + r"-(bin|all)\.zip", wrapper):
        raise ValueError("This revision needs a different pinned Gradle toolchain profile")
    if str(versions["compileSdk"]) != str(profile["compileSdk"]):
        raise ValueError("This revision needs a different pinned Android SDK profile")

    values = {
        "source_sha": sha,
        "version_name": version_name,
        "version_code": version_code,
        "targets": json.dumps({"include": targets}, separators=(",", ":")),
        "builds": json.dumps(
            {"include": [dict(t, attempt=a) for t in targets for a in (1, 2)]},
            separators=(",", ":"),
        ),
    }
    with open(os.environ["GITHUB_OUTPUT"], "a") as output:
        for name, value in values.items():
            output.write(f"{name}={value}\n")
    with open(os.environ["GITHUB_STEP_SUMMARY"], "a") as summary:
        summary.write(f"Source: `{sha}`\n\nVersion: `{version_name}` ({version_code})\n\n")
        summary.write("| Flavor | Format | Gradle task |\n| --- | --- | --- |\n")
        for target in targets:
            summary.write(f"| {target['flavor']} | {target['format']} | `{target['task']}` |\n")
        summary.write("\nEach output is built twice on separate runners before signing.\n")


if __name__ == "__main__":
    main()
