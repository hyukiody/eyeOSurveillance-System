# ========================================
# eyeO Platform - Master Deployment Script
# ========================================
# Professional showcase automation
# Builds, deploys, and validates all services

param(
    [switch]$Clean = $false,
    [switch]$NoBuild = $false,
    [switch]$LoadDemo = $false,
    [string]$Profile = "production"
)

$ErrorActionPreference = "Stop"

Write-Host "🚀 eyeO Platform - Master Deployment" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# ==================== Pre-flight Checks ====================

Write-Host "✓ Running pre-flight checks..." -ForegroundColor Yellow

# Check Docker
if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "✗ Docker not found. Please install Docker Desktop." -ForegroundColor Red
    exit 1
}

# Check Docker Compose
if (!(Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "✗ Docker Compose not found." -ForegroundColor Red
    exit 1
}

# Check .env file
if (!(Test-Path ".env")) {
    Write-Host "⚠ .env file not found. Copying from .env.example..." -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
    Write-Host "⚠ Please edit .env file with your secure values before continuing." -ForegroundColor Yellow
    Write-Host "  Required: EYEO_MASTER_KEY, JWT_SECRET_KEY, database passwords" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Pre-flight checks passed" -ForegroundColor Green
Write-Host ""

# ==================== Clean Previous Deployment ====================

if ($Clean) {
    Write-Host "🧹 Cleaning previous deployment..." -ForegroundColor Yellow
    
    docker-compose -f docker-compose.master.yml down -v
    
    Write-Host "✓ Cleanup complete" -ForegroundColor Green
    Write-Host ""
}

# ==================== Build Services ====================

if (!$NoBuild) {
    Write-Host "🔨 Building services..." -ForegroundColor Yellow
    
    $services = @("identity-service", "stream-processing", "data-core", "frontend")
    
    foreach ($service in $services) {
        Write-Host "  Building $service..." -ForegroundColor Cyan
        docker-compose -f docker-compose.master.yml build $service
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "✗ Build failed for $service" -ForegroundColor Red
            exit 1
        }
    }
    
    Write-Host "✓ All services built successfully" -ForegroundColor Green
    Write-Host ""
}

# ==================== Start Databases ====================

Write-Host "💾 Starting databases..." -ForegroundColor Yellow

docker-compose -f docker-compose.master.yml up -d identity-db stream-db sentinel-db

Write-Host "  Waiting for databases to initialize (30 seconds)..." -ForegroundColor Cyan
Start-Sleep -Seconds 30

# Check database health
$dbHealthy = $true
$databases = @("identity-db", "stream-db", "sentinel-db")

foreach ($db in $databases) {
    $health = docker inspect --format='{{.State.Health.Status}}' "eyeo-$db" 2>$null
    
    if ($health -ne "healthy") {
        Write-Host "  ⚠ $db is not healthy yet (status: $health)" -ForegroundColor Yellow
        $dbHealthy = $false
    } else {
        Write-Host "  ✓ $db is healthy" -ForegroundColor Green
    }
}

if (!$dbHealthy) {
    Write-Host "  Waiting additional 30 seconds for databases..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}

Write-Host "✓ Databases started" -ForegroundColor Green
Write-Host ""

# ==================== Start Backend Services ====================

Write-Host "⚙️  Starting backend services..." -ForegroundColor Yellow

docker-compose -f docker-compose.master.yml up -d identity-service stream-processing data-core

Write-Host "  Waiting for backend services to start (40 seconds)..." -ForegroundColor Cyan
Start-Sleep -Seconds 40

Write-Host "✓ Backend services started" -ForegroundColor Green
Write-Host ""

# ==================== Start Frontend & API Gateway ====================

Write-Host "🌐 Starting frontend and API gateway..." -ForegroundColor Yellow

docker-compose -f docker-compose.master.yml up -d frontend api-gateway

Start-Sleep -Seconds 10

Write-Host "✓ Frontend and API gateway started" -ForegroundColor Green
Write-Host ""

# ==================== Health Checks ====================

Write-Host "🏥 Running health checks..." -ForegroundColor Yellow

$services = @(
    @{Name="API Gateway"; URL="http://localhost/health"},
    @{Name="Identity Service"; URL="http://localhost/api/auth/health"},
    @{Name="Frontend"; URL="http://localhost/"}
)

$allHealthy = $true

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri $service.URL -Method GET -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            Write-Host "  ✓ $($service.Name) - OK" -ForegroundColor Green
        } else {
            Write-Host "  ⚠ $($service.Name) - Status: $($response.StatusCode)" -ForegroundColor Yellow
            $allHealthy = $false
        }
    } catch {
        Write-Host "  ✗ $($service.Name) - FAILED" -ForegroundColor Red
        $allHealthy = $false
    }
}

Write-Host ""

# ==================== Container Status ====================

Write-Host "📊 Container status:" -ForegroundColor Yellow
Write-Host ""

docker-compose -f docker-compose.master.yml ps

Write-Host ""

# ==================== Load Demo Data ====================

if ($LoadDemo) {
    Write-Host "📦 Loading demo data..." -ForegroundColor Yellow
    
    # Create demo user accounts
    Write-Host "  Creating demo user accounts..." -ForegroundColor Cyan
    
    $demoUsers = @(
        @{email="demo@eyeo.com"; password="Demo2024!"; tier="ENTERPRISE"},
        @{email="pro@eyeo.com"; password="Pro2024!"; tier="PRO"},
        @{email="free@eyeo.com"; password="Free2024!"; tier="FREE"}
    )
    
    foreach ($user in $demoUsers) {
        $body = @{
            email = $user.email
            password = $user.password
            tier = $user.tier
        } | ConvertTo-Json
        
        try {
            Invoke-RestMethod -Uri "http://localhost/api/auth/register" `
                -Method POST `
                -Body $body `
                -ContentType "application/json" `
                -ErrorAction SilentlyContinue
            
            Write-Host "    ✓ Created user: $($user.email) (Tier: $($user.tier))" -ForegroundColor Green
        } catch {
            Write-Host "    ⚠ User $($user.email) may already exist" -ForegroundColor Yellow
        }
    }
    
    Write-Host "✓ Demo data loaded" -ForegroundColor Green
    Write-Host ""
}

# ==================== Deployment Summary ====================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Deployment Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($allHealthy) {
    Write-Host "All services are healthy and operational!" -ForegroundColor Green
} else {
    Write-Host "⚠ Some services may need additional time to start." -ForegroundColor Yellow
    Write-Host "  Run 'docker-compose -f docker-compose.master.yml ps' to check status." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🌐 Access Points:" -ForegroundColor Cyan
Write-Host "  Dashboard:  http://localhost" -ForegroundColor White
Write-Host "  API Docs:   http://localhost/swagger-ui.html" -ForegroundColor White
Write-Host "  Health:     http://localhost/health" -ForegroundColor White
Write-Host ""

if ($LoadDemo) {
    Write-Host "👥 Demo Accounts:" -ForegroundColor Cyan
    Write-Host "  Enterprise: demo@eyeo.com / Demo2024!" -ForegroundColor White
    Write-Host "  Pro:        pro@eyeo.com / Pro2024!" -ForegroundColor White
    Write-Host "  Free:       free@eyeo.com / Free2024!" -ForegroundColor White
    Write-Host ""
}

Write-Host "📚 Documentation:" -ForegroundColor Cyan
Write-Host "  Deployment Guide: docs\MASTER_DEPLOYMENT_GUIDE.md" -ForegroundColor White
Write-Host "  Showcase Guide:   docs\PUBLIC_SHOWCASE_GUIDE.md" -ForegroundColor White
Write-Host "  API Spec:         specs\openapi.yaml" -ForegroundColor White
Write-Host ""

Write-Host "🛠️  Management Commands:" -ForegroundColor Cyan
Write-Host "  View logs:   docker-compose -f docker-compose.master.yml logs -f" -ForegroundColor White
Write-Host "  Stop all:    docker-compose -f docker-compose.master.yml down" -ForegroundColor White
Write-Host "  Restart:     docker-compose -f docker-compose.master.yml restart" -ForegroundColor White
Write-Host ""

Write-Host "🎉 eyeO Platform is ready for professional showcase!" -ForegroundColor Green
Write-Host ""

# Open dashboard in browser
$openBrowser = Read-Host "Open dashboard in browser? (Y/n)"
if ($openBrowser -ne 'n') {
    Start-Process "http://localhost"
}
