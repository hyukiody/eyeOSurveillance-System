# eyeO Platform - Deployment Validation
# Verifies all components are ready for showcase

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "eyeO Platform - Deployment Validation" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$allPassed = $true

# File Checks
Write-Host "Checking deployment files..." -ForegroundColor Yellow

$requiredFiles = @(
    "docker-compose.master.yml",
    "deploy-master.ps1",
    ".env.example",
    "README.md",
    "DEVELOPMENT_CYCLE_COMPLETE.md",
    "PROJECT_WRAP_UP.md",
    "docs/MASTER_DEPLOYMENT_GUIDE.md",
    "docs/PUBLIC_SHOWCASE_GUIDE.md",
    "docs/SECURITY_IMPLEMENTATION_REPORT.md",
    "specs/openapi.yaml",
    "sample-data/detection-events.json"
)

foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ MISSING: $file" -ForegroundColor Red
        $allPassed = $false
    }
}

Write-Host ""

# ==================== Service Directory Checks ====================
Service Directory Checks
Write-Host "
$services = @(
    "identity-service",
    "stream-processing",
    "data-core",
    "frontend"
)

foreach ($service in $services) {
    if (Test-Path $service) {
        Write-Host "  ✓ $service/" -ForegroundColor Green
        
        # Check for Dockerfile
        if (Test-Path "$service/Dockerfile") {
            Write-Host "    ✓ Dockerfile" -ForegroundColor DarkGreen
        } else {
            Write-Host "    ⚠ Missing Dockerfile" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✗ MISSING: $service/" -ForegroundColor Red
        $allPassed = $false
    }
}

Write-Host ""

# ==================== Security Files Check ====================
Security Files Check
Write-Host "
$securityFiles = @(
    "data-core/src/main/java/com/eyeo/data/config/SecurityConfiguration.java",
    "identity-service/src/main/java/com/teraapi/identity/entity/SecurityAuditLog.java",
    "identity-service/src/main/java/com/teraapi/identity/service/AuditLogService.java",
    "frontend/src/workers/stream.worker.ts"
)

foreach ($file in $securityFiles) {
    if (Test-Path $file) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Not found: $file" -ForegroundColor Yellow
    }
}

Write-Host ""

# ==================== Docker Check ====================
Docker Check
Write-Host "
if (Get-Command docker -ErrorAction SilentlyContinue) {
    Write-Host "  ✓ Docker installed" -ForegroundColor Green
    
    $dockerVersion = docker --version
    Write-Host "    $dockerVersion" -ForegroundColor DarkGreen
} else {
    Write-Host "  ✗ Docker not found" -ForegroundColor Red
    $allPassed = $false
}

if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
    Write-Host "  ✓ Docker Compose installed" -ForegroundColor Green
    
    $composeVersion = docker-compose --version
    Write-Host "    $composeVersion" -ForegroundColor DarkGreen
} else {
    Write-Host "  ✗ Docker Compose not found" -ForegroundColor Red
    $allPassed = $false
}

Write-Host ""

# ==================== Environment Check ====================
Environment Check
Write-Host "
if (Test-Path ".env") {
    Write-Host "  ✓ .env file exists" -ForegroundColor Green
    
    # Check for required variables
    $envContent = Get-Content ".env" -Raw
    
    $requiredVars = @("EYEO_MASTER_KEY", "JWT_SECRET_KEY", "POSTGRES_PASSWORD")
    
    foreach ($var in $requiredVars) {
        if ($envContent -match $var) {
            Write-Host "    ✓ $var configured" -ForegroundColor DarkGreen
        } else {
            Write-Host "    ⚠ $var not set" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  ⚠ .env file not found (will use .env.example)" -ForegroundColor Yellow
    Write-Host "    Run: Copy-Item .env.example .env" -ForegroundColor Cyan
}

Write-Host ""

# ==================== Java & Maven Check ====================
Java and Maven Check
Write-Host "
if (Get-Command java -ErrorAction SilentlyContinue) {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    
    if ($javaVersion -match "21") {
        Write-Host "  ✓ Java 21 installed" -ForegroundColor Green
        Write-Host "    $javaVersion" -ForegroundColor DarkGreen
    } else {
        Write-Host "  ⚠ Java installed but not version 21" -ForegroundColor Yellow
        Write-Host "    $javaVersion" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ⚠ Java not found (required for local builds)" -ForegroundColor Yellow
}

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "  ✓ Maven installed" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Maven not found (required for local builds)" -ForegroundColor Yellow
}

Write-Host ""

# ==================== Node.js Check ====================
Node.js Check
Write-Host "
if (Get-Command node -ErrorAction SilentlyContinue) {
    $nodeVersion = node --version
    Write-Host "  ✓ Node.js installed: $nodeVersion" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Node.js not found (required for frontend)" -ForegroundColor Yellow
}

if (Get-Command npm -ErrorAction SilentlyContinue) {
    $npmVersion = npm --version
    Write-Host "  ✓ npm installed: $npmVersion" -ForegroundColor Green
} else {
    Write-Host "  ⚠ npm not found" -ForegroundColor Yellow
}

Write-Host ""

# ==================== Documentation Check ====================
Documentation Check
Write-Host "
$docFiles = @{
    "Architecture" = "ARCHITECTURE.md"
    "Security" = "SECURITY.md"
    "Deployment" = "DEPLOYMENT.md"
    "API Testing" = "API_TESTING_GUIDE.md"
    "Contributing" = "CONTRIBUTING.md"
}

foreach ($doc in $docFiles.GetEnumerator()) {
    if (Test-Path $doc.Value) {
        Write-Host "  ✓ $($doc.Key): $($doc.Value)" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ $($doc.Key): $($doc.Value) not found" -ForegroundColor Yellow
    }
}

Write-Host ""

# ==================== Final Summary ====================

WrFinal Summary
Write-Host "========================================" -ForegroundColor Cyan

if ($allPassed) {
    Write-Host "All critical checks passed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Platform is ready for deployment!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Run: .\deploy-master.ps1 -LoadDemo" -ForegroundColor White
    Write-Host "  2. Wait ~3-5 minutes for services to start" -ForegroundColor White
    Write-Host "  3. Access: http://localhost" -ForegroundColor White
    Write-Host ""
    Write-Host "Documentation:" -ForegroundColor Cyan
    Write-Host "  - Deployment Guide: docs\MASTER_DEPLOYMENT_GUIDE.md" -ForegroundColor White
    Write-Host "  - Project Summary: DEVELOPMENT_CYCLE_COMPLETE.md" -ForegroundColor White
    Write-Host "  - Wrap-Up Guide: PROJECT_WRAP_UP.md" -ForegroundColor White
} else {
    Write-Host "Some checks failed" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please review the errors above and fix before deployment." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""