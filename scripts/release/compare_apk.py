#!/usr/bin/env python3
"""Verify a signed release against an independent APK using signature copying.

The comparison APK may be a published signed APK or, with --unsigned, an
independently rebuilt unsigned APK. Neither input is modified; no keystore is
needed. This checks artifact equivalence, not execution of F-Droid's build recipe.
"""

import argparse
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys

from verify import file_digest, require, run, write_json


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("signed_apk", type=Path, help="Signed release APK to verify")
    parser.add_argument("comparison_apk", type=Path, help="Independent APK from the same source revision")
    parser.add_argument("--unsigned", action="store_true", help="The comparison APK is unsigned")
    parser.add_argument("--expected-cert-sha256", required=True, help="Expected release signer certificate fingerprint")
    parser.add_argument("--comparison-sha256", help="Expected checksum of the independent APK")
    parser.add_argument("--report", type=Path, help="Write a JSON verification report")
    args = parser.parse_args()

    signed, comparison = args.signed_apk.resolve(), args.comparison_apk.resolve()
    require(signed.is_file() and comparison.is_file(), "Both APK files must exist.")
    require(not signed.samefile(comparison), "An independent comparison APK is required.")
    if args.report:
        report_path = args.report.resolve()
        for apk in (signed, comparison):
            require(report_path != apk and not (report_path.exists() and report_path.samefile(apk)),
                    "The report must not overwrite either input APK.")
    expected_cert = re.sub(r"[\s:]", "", args.expected_cert_sha256).lower()
    require(re.fullmatch(r"[0-9a-f]{64}", expected_cert), "Invalid release certificate SHA-256.")
    if args.comparison_sha256:
        require(re.fullmatch(r"[0-9a-fA-F]{64}", args.comparison_sha256), "Invalid comparison APK SHA-256.")
    for tool in ("apksigner", "apksigcopier"):
        require(shutil.which(tool), tool + " must be installed and on PATH.")
    os.environ["LC_ALL"] = "C"
    for name in tuple(os.environ):
        if name.startswith("APKSIGCOPIER_"):
            del os.environ[name]

    report = {
        "status": "failed",
        "method": "apksigcopier compare --unsigned" if args.unsigned else "apksigcopier compare",
        "signed_apk_sha256": file_digest(signed),
        "comparison_apk_sha256": file_digest(comparison),
        "comparison_apk_signed": not args.unsigned,
        "expected_certificate_sha256": expected_cert,
        "fdroid_pipeline_execution": "not_run",
    }
    try:
        if args.comparison_sha256:
            require(report["comparison_apk_sha256"] == args.comparison_sha256.lower(), "Comparison APK checksum mismatch.")
        certificates = run(["apksigner", "verify", "--verbose", "--print-certs", signed])
        fingerprints = re.findall(r"^Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]+)$", certificates, re.M)
        require([value.lower() for value in fingerprints] == [expected_cert], "Release APK signing certificate mismatch.")
        require(re.search(r"^Verified using v[23](?:\.\d+)? scheme.*: true$", certificates, re.M),
                "A v2 or v3 APK signature is required to verify ZIP metadata as well as file contents.")

        # compare verifies signatures, copies the release signature to the second
        # APK's payload in a temporary file, then verifies the reconstructed APK.
        command = ["apksigcopier", "compare"]
        if args.unsigned:
            command.append("--unsigned")
        result = subprocess.run(command + [str(signed), str(comparison)],
                                capture_output=True, text=True, timeout=300, check=False)
        report["comparison_output"] = result.stdout + result.stderr
        require(result.returncode == 0, "APK signature-copy comparison failed; the independent APK does not reproduce this signed release.")
        report["status"] = "verified"
    except (ValueError, OSError, subprocess.TimeoutExpired) as error:
        report["error"] = str(error)
        raise
    finally:
        if args.report:
            args.report.parent.mkdir(parents=True, exist_ok=True)
            write_json(args.report, report)
    print(json.dumps(report, indent=2, sort_keys=True))


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, subprocess.TimeoutExpired) as error:
        sys.exit(str(error))
