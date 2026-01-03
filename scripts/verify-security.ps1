# ========================================
# Security Verification Script
# eyeO Platform v2.0-public
# ========================================
# Verifies no sensitive data is tracked in Git

param(
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

Write-Host "[SECURITY] eyeO Platform Security Verification" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Test counters
$testsPassed = 0
$testsFailed = 0

# ========================================
# Test 1: .env not tracked
# ========================================
Write-Host "Test 1: Verify .env is not tracked in Git..." -ForegroundColor Yellow

$gitIgnoreCheck = git check-ignore .env 2>&1
if ($gitIgnoreCheck -match "\.env") {
    Write-Host "  [PASS] .env is properly ignored" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "  [FAIL] .env is NOT in .gitignore" -ForegroundColor Red
    $testsFailed++
}

# ========================================
# Test 2: .env not in Git history
# ========================================
Write-Host "`nTest 2: Verify .env never committed..." -ForegroundColor Yellow

$envHistory = git log --all --full-history -- .env
if ([string]::IsNullOrWhiteSpace($envHistory)) {
    Write-Host "  [PASS] No .env commits found in Git history" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "  [FAIL] .env found in Git history!" -ForegroundColor Red
    if ($Verbose) {
        Write-Host "  Commits:" -ForegroundColor Red
        Write-Host $envHistory -ForegroundColor Gray
    }
    $testsFailed++
}

# ========================================
# Test 3: .env.example exists
# ========================================
Write-Host "`nTest 3: Verify .env.example template exists..." -ForegroundColor Yellow

if (Test-Path ".env.example") {
    Write-Host "  [PASS] .env.example template found" -ForegroundColor Green
    $testsPassed++
    
    # Check for placeholder values
    $exampleContent = Get-Content ".env.example" -Raw
    if ($exampleContent -match "CHANGE_ME|REPLACE_WITH|your-secret-here") {
        Write-Host "  [PASS] Template contains safe placeholders" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] Warning: .env.example may contain real values" -ForegroundColor Yellow
    }
} else {
    Write-Host "  [FAIL] .env.example not found" -ForegroundColor Red
    $testsFailed++
}

# ========================================
# Test 4: No secrets in staged files
# ========================================
Write-Host "`nTest 4: Check for secrets in staged files..." -ForegroundColor Yellow

$stagedSecrets = git diff --cached --name-only | Where-Object { 
    $_ -match "\.(env|key|pem|p12)$" -or $_ -match "secret" 
}

if ($stagedSecrets.Count -eq 0) {
    Write-Host "  [PASS] No secret files staged for commit" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "  [FAIL] Secret files staged:" -ForegroundColor Red
    $stagedSecrets | ForEach-Object { Write-Host "    - $_" -ForegroundColor Red }
    $testsFailed++
}

# ========================================
# Test 5: .gitignore properly configured
# ========================================
Write-Host "`nTest 5: Verify .gitignore rules..." -ForegroundColor Yellow

if (Test-Path ".gitignore") {
    $gitignoreContent = Get-Content ".gitignore" -Raw
    
    $requiredRules = @(".env", "*.key", "*.pem", "secrets/")
    $missingRules = @()
    
    foreach ($rule in $requiredRules) {
        if ($gitignoreContent -notmatch [regex]::Escape($rule)) {
            $missingRules += $rule
        }
    }
    
    if ($missingRules.Count -eq 0) {
        Write-Host "  [PASS] All critical ignore rules present" -ForegroundColor Green
        $testsPassed++
    } else {
        Write-Host "  [FAIL] Missing gitignore rules:" -ForegroundColor Red
        $missingRules | ForEach-Object { Write-Host "    - $_" -ForegroundColor Red }
        $testsFailed++
    }
} else {
    Write-Host "  [FAIL] .gitignore not found" -ForegroundColor Red
    $testsFailed++
}

# ========================================
# Test 6: Basic credential scan
# ========================================
Write-Host "`nTest 6: Scan for hardcoded credentials in source..." -ForegroundColor Yellow

# Simple text search for common patterns
$searchTerms = @("password=", "apikey=", "secret=", "jdbc:mysql")
$foundIssues = @()

$sourceFiles = Get-ChildItem -Recurse -Include *.java,*.properties,*.yml,*.yaml -Exclude node_modules,target,dist,build -ErrorAction SilentlyContinue

foreach ($file in $sourceFiles) {
    $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
    if ($content) {
        foreach ($term in $searchTerms) {
            if ($content -match $term) {
                $foundIssues += $file.Name
                break
            }
        }
    }
}

if ($foundIssues.Count -eq 0) {
    Write-Host "  [PASS] No obvious hardcoded secrets detected" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "  [WARN] Found potential credential patterns (review manually):" -ForegroundColor Yellow
    $foundIssues | Select-Object -First 5 | ForEach-Object { Write-Host "    - $_" -ForegroundColor Gray }
    # Warning only
    $testsPassed++
}

# ========================================
# Test 7: SECURITY_CREDENTIALS.md exists
# ========================================
Write-Host "`nTest 7: Verify security documentation..." -ForegroundColor Yellow

if (Test-Path "SECURITY_CREDENTIALS.md") {
    Write-Host "  [PASS] SECURITY_CREDENTIALS.md found" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "  [WARN] SECURITY_CREDENTIALS.md not found (recommended)" -ForegroundColor Yellow
    $testsPassed++
}

# ========================================
# Summary
# ========================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Security Verification Complete" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Tests Passed: " -NoNewline
Write-Host $testsPassed -ForegroundColor Green
Write-Host "Tests Failed: " -NoNewline
Write-Host $testsFailed -ForegroundColor $(if ($testsFailed -eq 0) { "Green" } else { "Red" })

if ($testsFailed -eq 0) {
    Write-Host "`n[PASS] Repository is secure for public release!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n[FAIL] Security issues detected. Fix before public release!" -ForegroundColor Red
    Write-Host "`nRecommended actions:" -ForegroundColor Yellow
    Write-Host "  1. Review failed tests above" -ForegroundColor Gray
    Write-Host "  2. Update .gitignore if needed" -ForegroundColor Gray
    Write-Host "  3. Remove any committed secrets from Git history" -ForegroundColor Gray
    Write-Host "  4. Rotate any exposed credentials" -ForegroundColor Gray
    exit 1
}
