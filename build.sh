#!/usr/bin/env bash
set -e
./gradlew assemble
docker build -t wutiarn/edustor-pdfgen .