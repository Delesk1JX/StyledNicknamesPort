# Extracts the Mojang-named Minecraft sources that ModDevGradle caches, so that the
# real API of a game version can be grepped instead of guessed from error messages.
#
#   powershell -File tools/extract-mc-sources.ps1 -Version 26.1.2 -Out ..\.mc-sources\26.1.2
#
# Each game version gets its own directory because the three targets do not share
# class names (for example TeamColor only exists from 26.2 on).

param(
    [Parameter(Mandatory = $true)][string]$Version,
    [Parameter(Mandatory = $true)][string]$Out
)

$cache = Join-Path $env:USERPROFILE ".gradle\caches\neoformruntime\intermediate_results"

$jar = Get-ChildItem $cache -Filter "sourcesAndCompiledWithNeoForge*_output.jar" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "No NeoForm sources jar found in $cache. Build one of the projects first."
}

New-Item -ItemType Directory -Force -Path $Out | Out-Null

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)

$count = 0
foreach ($entry in $zip.Entries) {
    if (-not $entry.FullName.EndsWith('.java')) { continue }

    $target = Join-Path $Out $entry.FullName
    $dir = Split-Path -Parent $target
    if (-not (Test-Path -LiteralPath $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }

    [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
    $count++
}

$zip.Dispose()

Write-Host "extracted $count java sources from"
Write-Host "  $($jar.FullName)"
Write-Host "to $Out (version $Version)"