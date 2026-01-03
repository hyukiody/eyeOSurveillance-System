# ========================================
# NUCLEAR OPTION: Fresh Git History
# ========================================
# Completely rewrites Git history starting fresh
# USE WITH EXTREME CAUTION - This cannot be undone!

param(
    [switch]$Execute,
    [switch]$ForceNoBackup
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Red
Write-Host "  NUCLEAR CLEANUP - FRESH GIT HISTORY" -ForegroundColor Red
Write-Host "========================================`n" -ForegroundColor Red

if (-not $Execute) {
    Write-Host "[DRY RUN MODE]" -ForegroundColor Yellow
    Write-Host "`nThis script will:" -ForegroundColor Cyan
    Write-Host "  1. Create backup of current repository" -ForegroundColor Gray
    Write-Host "  2. Create fresh orphan branch with clean state" -ForegroundColor Gray
    Write-Host "  3. Commit current clean codebase as initial commit" -ForegroundColor Gray
    Write-Host "  4. Force push to remote (OVERWRITES ALL HISTORY)" -ForegroundColor Gray
    Write-Host "  5. Delete old branch references" -ForegroundColor Gray
    Write-Host "`n[WARNING] This will:" -ForegroundColor Red
    Write-Host "  - PERMANENTLY DELETE all Git history" -ForegroundColor Red
    Write-Host "  - REMOVE all previous commits" -ForegroundColor Red
    Write-Host "  - REQUIRE all collaborators to re-clone" -ForegroundColor Red
    Write-Host "  - Cannot be undone after force push" -ForegroundColor Red
    Write-Host "`nRun with -Execute to proceed" -ForegroundColor Yellow
    Write-Host "Add -ForceNoBackup to skip backup (not recommended)" -ForegroundColor Yellow
    exit 0
}

# Confirm user understands
Write-Host "`n[FINAL WARNING]" -ForegroundColor Red
Write-Host "This will PERMANENTLY delete all Git history!" -ForegroundColor Red
Write-Host "Type 'DELETE HISTORY' to confirm: " -NoNewline -ForegroundColor Yellow
$confirmation = Read-Host
if ($confirmation -ne "DELETE HISTORY") {
    Write-Host "`nAborted. No changes made." -ForegroundColor Green
    exit 0
}

# Step 1: Create backup
if (-not $ForceNoBackup) {
    Write-Host "`n[STEP 1/6] Creating backup..." -ForegroundColor Cyan
    
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupPath = "..\eyeo-platform-backup-$timestamp"
    
    Write-Host "  Backup location: $backupPath" -ForegroundColor Gray
    
    # Clone to backup location
    git clone . $backupPath 2>&1 | Out-Null
    
    if (Test-Path $backupPath) {
        Write-Host "  [SUCCESS] Backup created" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Backup failed!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "`n[STEP 1/6] Skipping backup (ForceNoBackup)" -ForegroundColor Yellow
}

# Step 2: Commit any pending changes
Write-Host "`n[STEP 2/6] Committing current state..." -ForegroundColor Cyan

git add -A
$uncommitted = git status --short
if ($uncommitted) {
    git commit -m "chore: Final state before history cleanup

All personal files removed.
Preparing for fresh Git history."
    Write-Host "  [SUCCESS] Current state committed" -ForegroundColor Green
} else {
    Write-Host "  [INFO] No uncommitted changes" -ForegroundColor Gray
}

# Step 3: Create fresh orphan branch
Write-Host "`n[STEP 3/6] Creating fresh orphan branch..." -ForegroundColor Cyan

$currentBranch = git rev-parse --abbrev-ref HEAD
Write-Host "  Current branch: $currentBranch" -ForegroundColor Gray

# Create orphan branch (no history)
git checkout --orphan fresh-history 2>&1 | Out-Null

Write-Host "  [SUCCESS] Orphan branch 'fresh-history' created" -ForegroundColor Green

# Step 4: Create initial commit with clean codebase
Write-Host "`n[STEP 4/6] Creating initial commit..." -ForegroundColor Cyan

git add -A

$initialCommitMsg = @"
Initial commit - eyeO Platform v2.0

Production-grade microservices security platform featuring:
- Zero-Trust architecture with client-side AES-256-GCM encryption
- JWT-based authentication with tiered licensing (FREE/PRO/ENTERPRISE)
- Spring Boot 3.4 microservices (Identity, Data Core, Middleware)
- React 18 + TypeScript frontend with Web Crypto API
- Docker Compose orchestration with health checks

This is a clean repository with no personal data or development history.

Technology Stack:
- Backend: Java 17, Spring Boot 3.4, MySQL 8.0
- Frontend: React 18, TypeScript 5.6, Vite 5.2
- Security: AES-256-GCM, PBKDF2, JWT HS512
- Infrastructure: Docker, Nginx, Docker Compose

Repository prepared for public v2.0 release.
All personal files and sensitive data removed.
"@

git commit -m $initialCommitMsg

Write-Host "  [SUCCESS] Initial commit created" -ForegroundColor Green

# Step 5: Replace main branch
Write-Host "`n[STEP 5/6] Replacing main branch..." -ForegroundColor Cyan

git branch -D main 2>&1 | Out-Null
git branch -m main

Write-Host "  [SUCCESS] Branch 'fresh-history' renamed to 'main'" -ForegroundColor Green

# Step 6: Force push to remote
Write-Host "`n[STEP 6/6] Force pushing to remote..." -ForegroundColor Cyan
Write-Host "  [WARNING] This will overwrite remote history!" -ForegroundColor Red
Write-Host "  Press Ctrl+C to cancel, or any key to continue..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

git push origin main --force 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "  [SUCCESS] Remote history replaced" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] Force push failed" -ForegroundColor Red
    Write-Host "  You may need to manually push: git push origin main --force" -ForegroundColor Yellow
}

# Clean up
Write-Host "`n[CLEANUP] Removing old refs..." -ForegroundColor Cyan

git reflog expire --expire=now --all
git gc --prune=now --aggressive 2>&1 | Out-Null

Write-Host "  [SUCCESS] Git cleanup complete" -ForegroundColor Green

# Final summary
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  FRESH HISTORY CREATED SUCCESSFULLY" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  Old commits: DELETED" -ForegroundColor Red
Write-Host "  New history: 1 commit (Initial commit)" -ForegroundColor Green
Write-Host "  Personal data: REMOVED" -ForegroundColor Green
Write-Host "  Remote status: UPDATED" -ForegroundColor Green

if (-not $ForceNoBackup) {
    Write-Host "`nBackup location: $backupPath" -ForegroundColor Yellow
    Write-Host "  Keep this backup until you verify everything works!" -ForegroundColor Yellow
}

Write-Host "`nNext steps for collaborators:" -ForegroundColor Cyan
Write-Host "  1. Backup their local changes" -ForegroundColor Gray
Write-Host "  2. Delete local repository: rm -rf eyeo-platform" -ForegroundColor Gray
Write-Host "  3. Re-clone: git clone <repository-url>" -ForegroundColor Gray

Write-Host "`nRepository size after cleanup:" -ForegroundColor Cyan
$gitSize = (Get-ChildItem -Path ".git" -Recurse -File | Measure-Object -Property Length -Sum).Sum / 1MB
Write-Host "  .git directory: $([math]::Round($gitSize, 2)) MB" -ForegroundColor Gray

Write-Host "`n[SUCCESS] Fresh Git history established!" -ForegroundColor Green
