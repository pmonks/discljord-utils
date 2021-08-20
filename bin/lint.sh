#!/usr/bin/env bash

echo "ℹ️  Linting (clj-kondo)..."
clojure -Srepro -M:kondo

echo "ℹ️  Linting (eastwood)..."
clojure -Srepro -M:eastwood
