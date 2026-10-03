# Strips a UTF-8 BOM from every source file under the project.
#
# Both PowerShell's Set-Content -Encoding UTF8 and editors that keep an existing
# file's encoding add a BOM, which javac and Gradle's Groovy parser reject on
# line 1 with a confusing "illegal character" / "Unexpected character" error.
# Run this after editing sources:  powershell -File tools/strip-bom.ps1

$root = Split-Path -Parent $PSScriptRoot
$bom = [byte[]](0xEF, 0xBB, 0xBF)
$fixed = 0

Get-ChildItem -Path $root -Recurse -File -Include *.java, *.gradle, *.json, *.properties, *.mcmeta, *.toml |
    Where-Object { $_.FullName -notmatch '\\build\\|\\\.gradle\\' } |
    ForEach-Object {
        $bytes = [System.IO.File]::ReadAllBytes($_.FullName)

        if ($bytes.Length -ge 3 -and $bytes[0] -eq $bom[0] -and $bytes[1] -eq $bom[1] -and $bytes[2] -eq $bom[2]) {
            [System.IO.File]::WriteAllBytes($_.FullName, $bytes[3..($bytes.Length - 1)])
            $fixed++
            Write-Host "stripped BOM: $($_.FullName.Substring($root.Length + 1))"
        }
    }

Write-Host "done, $fixed file(s) fixed"