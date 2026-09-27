#!/usr/bin/env sh
set -eu
mvn -s .mvn/settings.xml test "$@" || status=$?
mvn -s .mvn/settings.xml -q exec:java '-Dexec.mainClass=integrations.email.ReportNotifier' || true
mvn -s .mvn/settings.xml -q exec:java '-Dexec.mainClass=integrations.xray.XraySimulator' || true
exit ${status:-0}
