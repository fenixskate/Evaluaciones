#!/usr/bin/env sh
set -eu
test_status=0
mvn -B clean test -Dbrowser="${BROWSER:-firefox}" -Dheadless="${HEADLESS:-true}" "$@" || test_status=$?
post_status=0
if [ -s target/current-run.txt ]; then
  evidence=$(cat target/current-run.txt)
  run_id=$(basename "$evidence")
  destination="output/reports/$run_id"
  mkdir -p "$destination"
  for report in target/cucumber-report.* target/execution-timeline.csv; do
    if [ -f "$report" ]; then cp "$report" "$destination/"; fi
  done
  mvn -B exec:java -Dexec.mainClass=integrations.email.ReportNotifier "-Dexec.args=$evidence" || post_status=$?
  mvn -B exec:java -Dexec.mainClass=integrations.xray.XraySimulator || post_status=$?
else
  echo 'No se generaron evidencias de esta ejecucion. No se adjuntaran reportes anteriores.'
  post_status=1
fi
if [ "$test_status" -ne 0 ]; then exit "$test_status"; fi
exit "$post_status"
