#!/usr/bin/env sh
set -eu
mvn -B test -Dbrowser="${BROWSER:-firefox}" -Dheadless=true
mvn -B test-compile exec:java \
  -Dexec.mainClass=notifications.ReportNotifier \
  -Dexec.args=target/cucumber-report.html
