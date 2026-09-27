$ErrorActionPreference = 'Stop'
mvn -s .mvn/settings.xml test @args
$testStatus = $LASTEXITCODE
mvn -s .mvn/settings.xml -q exec:java '-Dexec.mainClass=integrations.email.ReportNotifier'
mvn -s .mvn/settings.xml -q exec:java '-Dexec.mainClass=integrations.xray.XraySimulator'
if ($testStatus -ne 0) { exit $testStatus }
