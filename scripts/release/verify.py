#!/usr/bin/env python3
"""Validate release metadata and compare clean unsigned builds before signing."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import time
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
import zipfile

MAX_REFERENCE_BYTES = 512 * 1024 * 1024
FOSS_CERTIFICATE = "8974efc853e2d5e93eb29dd809fc57cae7411fcfaca03de3f7c152f441a90064"


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(stream):
    result = hashlib.sha256()
    for chunk in iter(lambda: stream.read(1024 * 1024), b""):
        result.update(chunk)
    return result.hexdigest()


def file_digest(path):
    with path.open("rb") as stream:
        return digest(stream)


def write_json(path, value):
    path.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n")


def zip_inventory(path):
    result = {}
    with zipfile.ZipFile(path) as archive:
        for index, entry in enumerate(archive.infolist()):
            require(entry.filename not in result, "Duplicate ZIP entry: " + entry.filename)
            with archive.open(entry) as stream:
                result[entry.filename] = {
                    "sha256": digest(stream), "size": entry.file_size,
                    "compressed_size": entry.compress_size, "compression": entry.compress_type,
                    "timestamp": entry.date_time, "order": index,
                    "extra_sha256": hashlib.sha256(entry.extra).hexdigest(),
                }
    return result


def run(arguments, report=None):
    result = subprocess.run([str(arg) for arg in arguments], capture_output=True, text=True,
                            timeout=300, check=False)
    if report:
        report.write_text(result.stdout + result.stderr)
    require(result.returncode == 0, "Artifact verification command failed: " + str(arguments[0]))
    return result.stdout


def validate_metadata(path, flavor, format_, version, code, sha, tools, output):
    app_id = "com.shifthackz.aisdv1.app" + ("" if flavor == "playstore" else "." + flavor)
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        require(len(names) == len(set(names)), "Artifact contains duplicate ZIP entries.")
        require(not any(re.fullmatch(r"META-INF/[^/]+\.(SF|RSA|DSA|EC)", name, re.I)
                        for name in names), "Artifact contains an existing JAR signature.")
        vcs = [name for name in names if name.endswith("/version-control-info.textproto")]
        require(vcs, "AGP version-control-info.textproto is missing.")
        for name in vcs:
            require(archive.getinfo(name).file_size < 65536, "VCS metadata is unexpectedly large.")
            revisions = re.findall(r'\brevision\s*:\s*"([^"]+)"', archive.read(name).decode())
            require(revisions and all(revision == sha for revision in revisions), "Embedded source revision does not match the requested commit.")
        if format_ == "apk":
            with path.open("rb") as stream:
                stream.seek(max(0, archive.start_dir - 16))
                require(stream.read(16) != b"APK Sig Block 42", "APK already contains a signing block.")
    if format_ == "apk":
        badging = run([tools / "aapt2", "dump", "badging", path], output / "apk-metadata.txt")
        package = next((line for line in badging.splitlines() if line.startswith("package: ")), "")
        fields = dict(re.findall(r"(\w+)='([^']*)'", package))
        actual = (fields.get("name"), fields.get("versionName"), fields.get("versionCode"))
        require(not any(line.startswith("application-debuggable") for line in badging.splitlines()), "APK is debuggable.")
        run([tools / "zipalign", "-c", "-P", "16", "4", path], output / "alignment.txt")
    else:
        bundletool = Path(os.environ["BUNDLETOOL_JAR"])
        command = [Path(os.environ["JAVA_HOME"]) / "bin/java", "-jar", bundletool]
        run(command + ["validate", "--bundle=" + str(path)], output / "bundle-validation.txt")
        xml = run(command + ["dump", "manifest", "--bundle=" + str(path), "--module=base"], output / "bundle-manifest.xml")
        manifest = ET.fromstring(xml)
        android = "{http://schemas.android.com/apk/res/android}"
        actual = (manifest.get("package"), manifest.get(android + "versionName"), manifest.get(android + "versionCode"))
        application = manifest.find("application")
        require(application is not None and application.get(android + "debuggable", "false") in ("false", "0"), "App Bundle is debuggable or lacks an application.")
    require(actual == (app_id, version, code), "Application ID or version does not match the release request.")
    return app_id


def allowed_url(url, initial=False):
    parsed = urllib.parse.urlsplit(url)
    require(parsed.scheme == "https" and not parsed.username and not parsed.password
            and parsed.port in (None, 443) and not parsed.fragment, "Reference URL must use HTTPS without credentials.")
    host = parsed.hostname
    allowed = ((host == "github.com" and re.fullmatch(r"/[^/]+/[^/]+/releases/download/[^/]+/[^/]+", parsed.path))
               or (host in ("f-droid.org", "fdroid.org") and parsed.path.startswith("/repo/"))
               or (not initial and host in ("release-assets.githubusercontent.com", "objects.githubusercontent.com")))
    require(allowed and not (initial and parsed.query), "Reference URL host or path is not allowed.")


class SafeRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        allowed_url(newurl)
        return super().redirect_request(req, fp, code, msg, headers, newurl)


def compare_reference(url, expected_hash, unsigned, tools, output):
    allowed_url(url, initial=True)
    require(re.fullmatch(r"[0-9a-fA-F]{64}", expected_hash), "Reference SHA-256 is required.")
    expected_cert = re.sub(r"[\s:]", "", os.environ.get("ANDROID_RELEASE_CERT_SHA256", "") or FOSS_CERTIFICATE).lower()
    require(re.fullmatch(r"[0-9a-f]{64}", expected_cert), "Invalid expected reference certificate.")
    with tempfile.TemporaryDirectory(prefix="sdai-reference-") as temporary:
        reference = Path(temporary) / "reference.apk"
        opener = urllib.request.build_opener(SafeRedirect())
        started = time.monotonic()
        with opener.open(url, timeout=30) as response, reference.open("wb") as stream:
            require(int(response.headers.get("Content-Length", "0")) <= MAX_REFERENCE_BYTES, "Reference APK exceeds the download limit.")
            total = 0
            for chunk in iter(lambda: response.read(1024 * 1024), b""):
                total += len(chunk)
                require(total <= MAX_REFERENCE_BYTES and time.monotonic() - started < 300, "Reference download exceeded its limit.")
                stream.write(chunk)
        require(file_digest(reference) == expected_hash.lower(), "Reference APK checksum mismatch.")
        certificates = run([tools / "apksigner", "verify", "--print-certs", reference])
        fingerprints = re.findall(r"^Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]+)$", certificates, re.M)
        require([value.lower() for value in fingerprints] == [expected_cert], "Reference APK signing certificate mismatch.")
        report = output / "reference-comparison.txt"
        comparison = run(["apksigcopier", "compare", "--unsigned", reference, unsigned], report)
        report.write_text(comparison + "Reference APK signature matches the unsigned rebuild.\n")
    return {"status": "verified", "url": url, "sha256": expected_hash.lower(), "certificate_sha256": expected_cert}


def main():
    require(len(sys.argv) in (9, 11), "Usage: verify.py FIRST SECOND OUTPUT FLAVOR FORMAT VERSION CODE SOURCE_SHA [REFERENCE_URL REFERENCE_SHA256]")
    first, second, output = (Path(value).resolve() for value in sys.argv[1:4])
    flavor, format_, version, code, sha = sys.argv[4:9]
    require(flavor in ("foss", "full", "playstore") and format_ in ("apk", "aab"), "Invalid release variant.")
    require(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._+-]*", version) and re.fullmatch(r"[1-9][0-9]*", code), "Invalid version.")
    require(re.fullmatch(r"[0-9a-f]{40}", sha), "Source revision must be a full commit SHA.")
    require(first.is_file() and second.is_file() and not first.samefile(second), "Two separate build artifacts are required.")
    output.mkdir(parents=True, exist_ok=True)
    require(not (output / ("unsigned." + format_)).exists(), "Verified output already exists.")
    hashes = [file_digest(first), file_digest(second)]
    if hashes[0] != hashes[1]:
        inventories = [zip_inventory(path) for path in (first, second)]
        differences = {name: [inventory.get(name) for inventory in inventories]
                       for name in sorted(set(inventories[0]) | set(inventories[1]))
                       if inventories[0].get(name) != inventories[1].get(name)}
        write_json(output / "reproducibility-diff.json", {"archive_sha256": hashes, "byte_identical": False,
                   "entry_differences": differences, "archive_only_difference": not differences})
        raise ValueError("Unsigned rebuilds differ; see reproducibility-diff.json.")
    tools = Path(os.environ.get("ANDROID_SDK_ROOT") or os.environ["ANDROID_HOME"]) / "build-tools" / os.environ.get("ANDROID_BUILD_TOOLS_VERSION", "35.0.0")
    os.environ["PATH"] = str(tools) + os.pathsep + os.environ.get("PATH", "")
    for name in tuple(os.environ):
        if name.startswith("APKSIGCOPIER_"):
            del os.environ[name]
    app_id = validate_metadata(first, flavor, format_, version, code, sha, tools, output)
    reference = {"status": "not_requested"}
    if len(sys.argv) == 11:
        require((flavor, format_) == ("foss", "apk"), "Reference comparison applies only to the FOSS APK.")
        reference = compare_reference(sys.argv[9], sys.argv[10], first, tools, output)
    metadata = {"sha256": hashes[0], "flavor": flavor, "format": format_, "source_sha": sha,
                "version_name": version, "version_code": int(code), "application_id": app_id,
                "unsigned_rebuild_comparison": "byte_identical", "reference_comparison": reference,
                "fdroid_pipeline_execution": "not_run"}
    shutil.copyfile(first, output / ("unsigned." + format_))
    write_json(output / "metadata.json", metadata)
    print(json.dumps(metadata, sort_keys=True))


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, KeyError, zipfile.BadZipFile, ET.ParseError, subprocess.TimeoutExpired) as error:
        sys.exit(str(error))
