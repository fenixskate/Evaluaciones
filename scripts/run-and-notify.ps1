param([string]$Browser = "firefox")
$ErrorActionPreference = "Stop"
$env:BROWSER = $Browser
bash scripts/run-and-notify.sh @args
exit $LASTEXITCODE
