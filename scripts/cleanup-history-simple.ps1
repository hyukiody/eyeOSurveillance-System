# ========================================
# Simplified Git History Cleanup
# Removes personal files from Git history
# ========================================

$ErrorActionPreference = "Continue"

Write-Host "[CLEANUP] Removing personal files from Git history..." -ForegroundColor Cyan

# List of specific files to remove
$files = @(
    ".private/DEVELOPMENT_NOTICE.md",
    ".private/README.md",
    ".private/UPDATE_SUMMARY.md",
    ".private/PORTFOLIO_STRATEGY.md",
    ".private/SECURITY_GUIDE.md",
    ".private/security-audit.ps1",
    "PRIVATE_DEV_README.md",
    "edge-node/CAREER_LEARNING_PATH.md",
    "middleware/LEARNING_PATH.md",
    "docs/DEV_SESSION_2026-01-02.md",
    "docs/DIRECTIVES_INDEX.md",
    "edge-node/docs/COMPLETION_SUMMARY.md"
)

Write-Host "`nRemoving individual files..." -ForegroundColor Yellow

foreach ($file in $files) {
    Write-Host "  Processing: $file" -ForegroundColor Gray
    git filter-branch -f --index-filter "git rm -rf --cached --ignore-unmatch '$file'" --prune-empty HEAD 2>&1 | Out-Null
}

Write-Host "`nRemoving directory: ops/knowledge/*" -ForegroundColor Yellow
git filter-branch -f --index-filter "git rm -rf --cached --ignore-unmatch 'ops/knowledge'" --prune-empty HEAD 2>&1 | Out-Null

Write-Host "`nCleaning up Git refs..." -ForegroundColor Cyan
Remove-Item -Path ".git/refs/original" -Recurse -Force -ErrorAction SilentlyContinue
git reflog expire --expire=now --all
git gc --prune=now --aggressive

Write-Host "`n[SUCCESS] Git history cleaned!" -ForegroundColor Green

$repoSize = (Get-ChildItem -Path ".git" -Recurse -File | Measure-Object -Property Length -Sum).Sum / 1MB
Write-Host "Repository .git size: $([math]::Round($repoSize, 2)) MB" -ForegroundColor Cyan
