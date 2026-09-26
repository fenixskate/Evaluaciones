param([string]$Browser = "firefox")
$ErrorActionPreference = "Stop"
mvn -B test "-Dbrowser=$Browser" "-Dheadless=true"
mvn -B test-compile exec:java `
  "-Dexec.mainClass=notifications.ReportNotifier" `
  "-Dexec.args=target/cucumber-report.html"
