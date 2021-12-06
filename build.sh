#!/usr/bin/env bash
set -e
docker build -t quay.io/wutiarn/edustor-recognition .
docker push quay.io/wutiarn/edustor-recognition