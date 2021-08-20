#!/usr/bin/env bash

set -e
set -o pipefail

echo "ℹ️ Downloading dependencies..."
clojure -Srepro -P --report stderr

echo "ℹ️ Compiling code..."
clojure -Srepro -M:check    # Note: Clojure CLI doesn't support --report with -M 🙄; see https://ask.clojure.org/index.php/10692/clojure-cli-report-incompatible-with-x for details
