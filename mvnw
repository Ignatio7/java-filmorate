#!/usr/bin/env sh

set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPS_FILE="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"
DIST_URL=$(sed -n 's/^distributionUrl=//p' "$PROPS_FILE")

if [ -z "$DIST_URL" ]; then
  echo "distributionUrl is not set in $PROPS_FILE" >&2
  exit 1
fi

DIST_NAME=$(basename "$DIST_URL")
DIST_DIR="$BASE_DIR/.mvn/wrapper/dists"
ARCHIVE_PATH="$DIST_DIR/$DIST_NAME"

mkdir -p "$DIST_DIR"

if [ ! -f "$ARCHIVE_PATH" ]; then
  echo "Downloading Maven distribution..."
  curl -fsSL "$DIST_URL" -o "$ARCHIVE_PATH"
fi

MAVEN_ROOT=$(tar -tzf "$ARCHIVE_PATH" | sed -n '1p' | cut -d/ -f1)
MAVEN_HOME="$DIST_DIR/$MAVEN_ROOT"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  echo "Extracting Maven distribution..."
  tar -xzf "$ARCHIVE_PATH" -C "$DIST_DIR"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
