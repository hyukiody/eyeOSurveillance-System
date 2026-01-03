# ========================================
# eyeO Platform - Test Script
# ========================================
# Quick API tests for containerized services

$ErrorActionPreference = "Stop"

Write-Host "🧪 eyeO Platform - API Tests" -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Cyan
Write-Host ""

# Test helper function
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [int]$ExpectedStatus = 200
    )
    
    try {
        Write-Host "Testing $Name..." -NoNewline
        $response = Invoke-WebRequest -Uri $Url -Method GET -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host " ✓" -ForegroundColor Green
            return $true
        } else {
            Write-Host " ✗ (Status: $($response.StatusCode))" -ForegroundColor Yellow
            return $false
        }
    } catch {
        Write-Host " ✗ (Error: $($_.Exception.Message))" -ForegroundColor Red
        return $false
    }
}

# Wait for services
Write-Host "⏳ Waiting for services to be ready (30s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Write-Host ""

# Test endpoints
Write-Host "📡 Testing Health Endpoints:" -ForegroundColor Cyan
Write-Host ""

$allHealthy = $true

$allHealthy = (Test-Endpoint "Identity Service Health" "http://localhost:8081/actuator/health") -and $allHealthy
$allHealthy = (Test-Endpoint "Data Core Health" "http://localhost:9090/health") -and $allHealthy
$allHealthy = (Test-Endpoint "Frontend" "http://localhost:5173") -and $allHealthy

Write-Host ""

if ($allHealthy) {
    Write-Host "✅ All services are healthy!" -ForegroundColor Green
    Write-Host ""
    
    # Test API functionality
    Write-Host "🔐 Testing Authentication API:" -ForegroundColor Cyan
    Write-Host ""
    
    # Generate random username
    $randomUser = "testuser_" + (Get-Random -Maximum 9999)
    
    Write-Host "Registering user '$randomUser'..." -NoNewline
    try {
        $registerBody = @{
            username = $randomUser
            password = "Test123!"
            email = "$randomUser@example.com"
        } | ConvertTo-Json
        
        $registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
            -Method POST `
            -Body $registerBody `
            -ContentType "application/json" `
            -TimeoutSec 10
        
        Write-Host " ✓" -ForegroundColor Green
        Write-Host "  User ID: $($registerResponse.id)" -ForegroundColor Gray
        
        # Test login
        Write-Host "Logging in as '$randomUser'..." -NoNewline
        $loginBody = @{
            username = $randomUser
            password = "Test123!"
        } | ConvertTo-Json
        
        $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
            -Method POST `
            -Body $loginBody `
            -ContentType "application/json" `
            -TimeoutSec 10
        
        Write-Host " ✓" -ForegroundColor Green
        Write-Host "  Token received: $($loginResponse.token.Substring(0, 20))..." -ForegroundColor Gray
        
        Write-Host ""
        Write-Host "✅ Authentication flow working correctly!" -ForegroundColor Green
        
    } catch {
        Write-Host " ✗" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} else {
    Write-Host "⚠️  Some services are not healthy. Check logs:" -ForegroundColor Yellow
    Write-Host "   docker-compose -f docker-compose.dev.yml logs" -ForegroundColor Gray
}

Write-Host ""
Write-Host "📚 For more tests, see: API_TESTING_GUIDE.md" -ForegroundColor Cyan
Write-Host ""
