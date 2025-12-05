<#
build-and-package.ps1
PowerShell script to compile the Java sources and produce an executable JAR (Option A).
Usage:
  .\build-and-package.ps1            # runs with defaults
  .\build-and-package.ps1 -Run      # runs the generated JAR after building
  .\build-and-package.ps1 -JarName MyApp.jar -MainClass controller.SimController

Notes:
- Requires JDK (javac and jar) in PATH.
- Produces output in the `out` folder and a runnable JAR at the repository root.
- Copies resource folders (config, docs) into the JAR if present.
#>

param(
    [switch]$Run = $false,
    [string]$JarName = "SchedulerSimulator.jar",
    [string]$MainClass = "controller.SimController",
    [string]$OutDir = "out",
    [string]$JdkHome = $env:JAVA_HOME
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Write-Host "Building project..."

# Locate javac and jar tools (try PATH first, then provided JdkHome / JAVA_HOME / common locations)
$javacCmd = Get-Command javac -ErrorAction SilentlyContinue
$jarCmd = Get-Command jar -ErrorAction SilentlyContinue

# Helper: try to resolve tool path from a JDK home folder
function Resolve-ToolFromJdkHome([string]$toolName, [string]$jdkPath) {
    if (-not $jdkPath) { return $null }
    $candidate = Join-Path $jdkPath "bin\$toolName.exe"
    if (Test-Path $candidate) { return $candidate }
    return $null
}

if (-not $javacCmd -and $JdkHome) {
    $candidate = Resolve-ToolFromJdkHome -toolName 'javac' -jdkPath $JdkHome
    if ($candidate) { $javacCmd = $candidate }
}
if (-not $jarCmd -and $JdkHome) {
    $candidate = Resolve-ToolFromJdkHome -toolName 'jar' -jdkPath $JdkHome
    if ($candidate) { $jarCmd = $candidate }
}

# Try common installation folders on Windows
if (-not $jarCmd -or -not $javacCmd) {
    $commonRoots = @('C:\Program Files\Java', 'C:\Program Files (x86)\Java')
    foreach ($root in $commonRoots) {
        if (-not (Test-Path $root)) { continue }
        Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            $p = $_.FullName
            if (-not $javacCmd) {
                $cand = Resolve-ToolFromJdkHome -toolName 'javac' -jdkPath $p
                if ($cand) { $javacCmd = $cand }
            }
            if (-not $jarCmd) {
                $cand = Resolve-ToolFromJdkHome -toolName 'jar' -jdkPath $p
                if ($cand) { $jarCmd = $cand }
            }
        }
        if ($javacCmd -and $jarCmd) { break }
    }
}

if (-not $javacCmd) {
    Write-Error "Required tool 'javac' not found. Ensure JDK is installed and 'javac' is on PATH or set JAVA_HOME/JdkHome to your JDK installation. Run: Get-Command javac or setx JAVA_HOME 'C:\\Program Files\\Java\\jdk-xx'"
    exit 1
}
if (-not $jarCmd) {
    Write-Error "Required tool 'jar' not found. Ensure JDK is installed and 'jar' is on PATH or set JAVA_HOME/JdkHome to your JDK installation. Run: Get-Command jar or setx JAVA_HOME 'C:\\Program Files\\Java\\jdk-xx'"
    exit 1
}

# Remember project root (absolute path) so we can reference the JAR later from temp dirs
$projectRoot = (Get-Location).ProviderPath


# Ensure output dir
if (!(Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir | Out-Null
}

# Collect .java sources
$srcFiles = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
# Safely measure count (avoids accessing .Count on an undefined/empty variable)
$srcCount = ($srcFiles | Measure-Object).Count
if ($srcCount -eq 0) {
    Write-Error "No Java source files found. Run this script from the repository root."
    exit 1
}

# Compile
Write-Host "Compiling $srcCount Java files..."
& $javacCmd -d $OutDir $srcFiles
Write-Host "Compilation finished. Classes are in: $OutDir"

# Prepare manifest
$manifest = "manifest.txt"
"Main-Class: $MainClass`n" | Out-File -Encoding ascii -FilePath $manifest

# Optionally include resource folders (config, docs, view files) into a temporary folder
$tempResources = Join-Path $env:TEMP ("sched_build_res_" + [System.Guid]::NewGuid().ToString())
New-Item -ItemType Directory -Path $tempResources | Out-Null
$resourceFolders = @("config", "docs", "view", "resources")
foreach ($f in $resourceFolders) {
    if (Test-Path $f) {
        Write-Host "Including resource folder: $f"
        Copy-Item -Recurse -Force $f $tempResources | Out-Null
    }
}

# Create the jar (first add classes, then resources)
$jarFullPath = Join-Path $projectRoot $JarName
$manifestPath = Join-Path $projectRoot $manifest
if (Test-Path $jarFullPath) { Remove-Item $jarFullPath -Force }
& $jarCmd cfm $jarFullPath $manifestPath -C $OutDir .

# Add resources (if any)
if ((Get-ChildItem -Recurse $tempResources | Measure-Object).Count -gt 0) {
    Write-Host "Adding resources to JAR..."
    # Copy the resources into a temp dir and update the jar
    $tmpDir = Join-Path $env:TEMP ("sched_jar_tmp_" + [System.Guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Path $tmpDir | Out-Null
    Copy-Item -Recurse -Force $tempResources\* $tmpDir\
    Push-Location $tmpDir
    # Update jar with resource files
    $filesToAdd = Get-ChildItem -Recurse | ForEach-Object { $_.FullName }
    foreach ($file in $filesToAdd) {
        $rel = $file.Substring($tmpDir.Length+1)
        & $jarCmd uf $jarFullPath -C $tmpDir $rel 2>$null | Out-Null
    }
    Pop-Location
    Remove-Item -Recurse -Force $tmpDir
}

# Clean temp resources
Remove-Item -Recurse -Force $tempResources

Write-Host "Created JAR: $JarName"

if ($Run) {
    Write-Host "Running $JarName..."
    java -jar $JarName
}

Write-Host "Done."