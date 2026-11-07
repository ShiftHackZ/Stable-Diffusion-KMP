#!/usr/bin/env bash
# AGP invokes Ninja directly, so a CMake environment limit alone is insufficient.
set -euo pipefail
for arg in "$@"; do
    if [[ "$arg" == -t ]]; then
        exec "${SDAI_REAL_NINJA:?Pinned Ninja path required}" "$@"
    fi
done
exec "${SDAI_REAL_NINJA:?Pinned Ninja path required}" "$@" -j2
