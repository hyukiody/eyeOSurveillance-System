# ========================================
# Git History Cleanup Script
# Removes personal and Obsidian files from entire Git history
# ========================================
# WARNING: This rewrites Git history. Coordinate with team before running.

param(
    [switch]$DryRun,
    [switch]$ForceExecute
)

$ErrorActionPreference = "Stop"

Write-Host "[CLEANUP] Git History Cleanup - Personal Files Removal" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Files and patterns to remove from history
$filesToRemove = @(
    # Private directory
    ".private/*",
    
    # Personal documentation
    "PRIVATE_DEV_README.md",
    "*/CAREER_LEARNING_PATH.md",
    "*/LEARNING_PATH.md",
    "*/PORTFOLIO_STRATEGY.md",
    
    # Development sessions
    "docs/DEV_SESSION_*.md",
    "docs/DIRECTIVES_INDEX.md",
    "*/COMPLETION_SUMMARY.md",
    "*/UPDATE_SUMMARY.md",
    
    # Obsidian workspace
    ".obsidian/*",
    "*.obsidian",
    
    # Personal notes
    "ops/knowledge/*",
    "*/notes/*",
    
    # Security audits
    "*/security-audit.ps1",
    
    # Credentials
    "*.key",
    "*.pem",
    "*.p12",
    "secrets/*",
    "credentials/*"
)

Write-Host "Files/patterns to remove from history:" -ForegroundColor Yellow
$filesToRemove | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }

if ($DryRun) {
    Write-Host "`n[DRY RUN] Checking what would be removed..." -ForegroundColor Yellow
    
    foreach ($pattern in $filesToRemove) {
        $found = git log --all --pretty=format: --name-only --diff-filter=A -- $pattern | Sort-Object -Unique
        if ($found) {
            Write-Host "`nPattern: $pattern" -ForegroundColor Cyan
            $found | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }
        }
    }
    
    Write-Host "`n[DRY RUN] Use -ForceExecute to actually remove these files from history" -ForegroundColor Yellow
    exit 0
}

if (-not $ForceExecute) {
    Write-Host "`n[ERROR] This operation rewrites Git history!" -ForegroundColor Red
    Write-Host "Run with -DryRun first to preview changes" -ForegroundColor Yellow
    Write-Host "Then run with -ForceExecute to proceed" -ForegroundColor Yellow
    exit 1
}

# Confirm with user
Write-Host "`n[WARNING] This will rewrite Git history!" -ForegroundColor Red
Write-Host "All collaborators will need to re-clone the repository." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to cancel, or any other key to continue..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host "`n[EXECUTE] Starting Git history cleanup..." -ForegroundColor Green

# Method 1: Using git filter-branch (built-in, slower but reliable)
Write-Host "`nMethod: git filter-branch" -ForegroundColor Cyan

foreach ($pattern in $filesToRemove) {
    Write-Host "  Removing: $pattern" -ForegroundColor Gray
    
    git filter-branch --force --index-filter `
        "git rm -rf --cached --ignore-unmatch $pattern" `
        --prune-empty --tag-name-filter cat -- --all 2>&1 | Out-Null
}

# Clean up
Write-Host "`nCleaning up Git database..." -ForegroundColor Cyan
git reflog expire --expire=now --all
git gc --prune=now --aggressive

Write-Host "`n[SUCCESS] Git history cleaned!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "  1. Review changes: git log --oneline" -ForegroundColor Gray
Write-Host "  2. Force push: git push origin --force --all" -ForegroundColor Gray
Write-Host "  3. Force push tags: git push origin --force --tags" -ForegroundColor Gray
Write-Host "  4. Notify team to re-clone repository" -ForegroundColor Gray

# Show size difference
Write-Host "`nRepository size analysis:" -ForegroundColor Cyan
$repoSize = (Get-ChildItem -Path ".git" -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
Write-Host "  .git directory: $([math]::Round($repoSize, 2)) MB" -ForegroundColor Gray
