#!/usr/bin/env pwsh
# Common PowerShell functions analogous to common.sh

function Get-RepoRoot {
    if ($env:MODERNIZE_REPO_ROOT) {
        return $env:MODERNIZE_REPO_ROOT
    }
    try {
        $result = git rev-parse --show-toplevel 2>$null
        if ($LASTEXITCODE -eq 0) {
            return $result
        }
    } catch {
        # Git command failed
    }
    
    # Scripts must be run from the project root.
    # $PWD is the correct fallback when the kit lives outside the repo (e.g. ~/.copilot).
    return (Get-Location).Path
}

# Resolve the kit installation root (where skills/ and agents/ directories live).
# Priority: MODERNIZE_KIT_ROOT env var > repo-local .github > ~/.copilot > fallback .github
function Get-KitRoot {
    param([string]$RepoRoot = (Get-RepoRoot))

    if ($env:MODERNIZE_KIT_ROOT) {
        return $env:MODERNIZE_KIT_ROOT
    }

    $repoLocal = Join-Path $RepoRoot '.github/skills'
    if (Test-Path $repoLocal) {
        return (Join-Path $RepoRoot '.github')
    }

    $globalCopilot = Join-Path $HOME '.copilot/skills'
    if (Test-Path $globalCopilot) {
        return (Join-Path $HOME '.copilot')
    }

    # Default to repo-local .github
    return (Join-Path $RepoRoot '.github')
}

function Get-CurrentBranch {

    # If an explicit feature directory is provided, branch name is irrelevant.
    if ($env:APP_MOD_FEATURE_DIR) {
        return "(feature-dir-override)"
    }
    
    # Then check git if available
    try {
        $result = git rev-parse --abbrev-ref HEAD 2>$null
        if ($LASTEXITCODE -eq 0) {
            return $result
        }
    } catch {
        # Git command failed
    }
    
    # For non-git repos, try to find the latest feature directory
    $repoRoot = Get-RepoRoot
    $specsDir = Join-Path $repoRoot "specs"
    
    if (Test-Path $specsDir) {
        $latestFeature = ""
        $highest = 0
        
        Get-ChildItem -Path $specsDir -Directory | ForEach-Object {
            if ($_.Name -match '^(\d{3})-') {
                $num = [int]$matches[1]
                if ($num -gt $highest) {
                    $highest = $num
                    $latestFeature = $_.Name
                }
            }
        }
        
        if ($latestFeature) {
            return $latestFeature
        }
    }
    
    # Final fallback
    return "main"
}

function Test-HasGit {
    try {
        git rev-parse --show-toplevel 2>$null | Out-Null
        return ($LASTEXITCODE -eq 0)
    } catch {
        return $false
    }
}

function Test-FeatureBranch {
    param(
        [string]$Branch,
        [bool]$HasGit = $true
    )
    
    # For non-git repos, we can't enforce branch naming but still provide output
    if (-not $HasGit) {
        Write-Warning "[specify] Warning: Git repository not detected; skipped branch validation"
        return $true
    }
    
    # If the caller explicitly specifies the feature directory, do not enforce branch naming.
    if ($env:APP_MOD_FEATURE_DIR) {
        Write-Warning "[modernize] APP_MOD_FEATURE_DIR set; skipped branch validation"
        return $true
    }

    if ($Branch -notmatch '^[0-9]{3}-') {
        Write-Output "ERROR: Not on a feature branch. Current branch: $Branch"
        Write-Output "Feature branches should be named like: 001-feature-name"
        return $false
    }
    return $true
}

function Get-FeatureDir {
    param([string]$RepoRoot, [string]$Branch)
    if ($env:APP_MOD_FEATURE_DIR) {
        $d = $env:APP_MOD_FEATURE_DIR
        if ([System.IO.Path]::IsPathRooted($d)) {
            return (Resolve-Path $d).Path
        }
        return (Resolve-Path (Join-Path $RepoRoot $d)).Path
    }

    $direct = Join-Path $RepoRoot ".github/modernize/$Branch"
    if (Test-Path $direct) { return $direct }

    $features = Join-Path $RepoRoot ".github/modernize/features/$Branch"
    if (Test-Path $features) { return $features }

    return $features
}

function Get-FeaturePathsEnv {
    $repoRoot = Get-RepoRoot
    $currentBranch = Get-CurrentBranch
    $hasGit = Test-HasGit
    $featureDir = Get-FeatureDir -RepoRoot $repoRoot -Branch $currentBranch
    $kitRoot = Get-KitRoot -RepoRoot $repoRoot
    
    [PSCustomObject]@{
        REPO_ROOT     = $repoRoot
        CURRENT_BRANCH = $currentBranch
        HAS_GIT       = $hasGit
        KIT_ROOT      = $kitRoot
        FEATURE_DIR   = $featureDir
        FEATURE_SPEC  = Join-Path $featureDir 'spec.md'
        IMPL_PLAN     = Join-Path $featureDir 'plan.md'
        TASKS         = Join-Path $featureDir 'tasks.md'
        RESEARCH      = Join-Path $featureDir 'research.md'
        DATA_MODEL    = Join-Path $featureDir 'data-model.md'
        QUICKSTART    = Join-Path $featureDir 'quickstart.md'
        CONTRACTS_DIR = Join-Path $featureDir 'contracts'
    }
}

function Test-FileExists {
    param([string]$Path, [string]$Description)
    if (Test-Path -Path $Path -PathType Leaf) {
        Write-Output "  ✓ $Description"
        return $true
    } else {
        Write-Output "  ✗ $Description"
        return $false
    }
}

function Test-DirHasFiles {
    param([string]$Path, [string]$Description)
    if ((Test-Path -Path $Path -PathType Container) -and (Get-ChildItem -Path $Path -ErrorAction SilentlyContinue | Where-Object { -not $_.PSIsContainer } | Select-Object -First 1)) {
        Write-Output "  ✓ $Description"
        return $true
    } else {
        Write-Output "  ✗ $Description"
        return $false
    }
}

