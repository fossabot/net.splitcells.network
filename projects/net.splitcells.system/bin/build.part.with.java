#!/usr/bin/env bash
set -e
chmod +x ./bin/*
export JAVA_VERSION=11 # This is required on FreeBSD, if an older Java version is set as default.
mvn clean install
cd ../net.splitcells.website.server
  # TODO ./bin/serve.to.folder
  cd ..
