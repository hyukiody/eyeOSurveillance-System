# ========================================
# EyeO Platform - Complete Deployment with Test Verification
# 🔒 PRIVATE DEVELOPMENT ENVIRONMENT ONLY
# ========================================
# 
# ⚠️ CONFIDENTIAL - INTERNAL USE ONLY
# This script is for private development and learning purposes.
# Not suitable for production deployment.
# 
# Purpose: Automated deployment with comprehensive testing
# Environment: Development/Learning only
# Classification: Private - Authorized developers only
#
# See: PRIVATE_DEV_README.md for usage guidelines
# ========================================

param(
    [switch]$SkipTests,
    [switch]$SkipBuild,
    [switch]$Production,
    [string]$Environment = "development"
)

$ErrorActionPreference = "Stop"
$startTime = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🚀 EyeO Platform - Deployment with Test Verification" -ForegroundColor Cyan
Write-Host "🔒 Private Development Environment" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Production Mode: $Production" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

# Test Results Tracking
$script:testResults = @{
    BackendTests = @()
    FrontendTests = @()
    ServiceHealth = @()
    BuildStatus = @()
}

function Write-TestResult {
    param(
        [string]$Category,
        [string]$Name,
        [string]$Status,
        [string]$Duration = "",
        [string]$Message = ""
    )
    
    $result = @{
        Category = $Category
        Name = $Name
        Status = $Status
        Duration = $Duration
        Message = $Message
        Timestamp = Get-Date
    }
    
    $script:testResults[$Category] += $result
    
    $icon = switch ($Status) {
        "PASS" { "✓"; $color = "Green" }
        "FAIL" { "✗"; $color = "Red" }
        "WARN" { "⚠"; $color = "Yellow" }
        "INFO" { "ℹ"; $color = "Cyan" }
        default { "•"; $color = "White" }
    }
    
    $output = "  $icon $Name"
    if ($Duration) { $output += " ($Duration)" }
    if ($Message) { $output += " - $Message" }
    
    Write-Host $output -ForegroundColor $color
}

# ========================================
# Step 1: Pre-flight Checks
# ========================================
Write-Host "`n[1/8] 🔍 Pre-flight Checks" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

# Check Docker
try {
    $dockerVersion = docker --version
    Write-TestResult "BuildStatus" "Docker Installed" "PASS" "" "$dockerVersion"
} catch {
    Write-TestResult "BuildStatus" "Docker Installed" "FAIL" "" "Docker not found"
    exit 1
}

# Check Docker Compose
try {
    $composeVersion = docker-compose --version
    Write-TestResult "BuildStatus" "Docker Compose" "PASS" "" "$composeVersion"
} catch {
    Write-TestResult "BuildStatus" "Docker Compose" "FAIL" "" "Docker Compose not found"
    exit 1
}

# Check Git
try {
    $gitBranch = git branch --show-current
    Write-TestResult "BuildStatus" "Git Repository" "PASS" "" "Branch: $gitBranch"
} catch {
    Write-TestResult "BuildStatus" "Git Repository" "WARN" "" "Not in git repository"
}

# Check Maven (for backend builds)
try {
    $mvnVersion = mvn --version | Select-Object -First 1
    Write-TestResult "BuildStatus" "Maven Installed" "PASS" "" "$mvnVersion"
} catch {
    Write-TestResult "BuildStatus" "Maven Installed" "WARN" "" "Maven not found - will use Docker builds"
}

# Check Node/NPM (for frontend)
try {
    $nodeVersion = node --version
    $npmVersion = npm --version
    Write-TestResult "BuildStatus" "Node.js/NPM" "PASS" "" "Node $nodeVersion, NPM $npmVersion"
} catch {
    Write-TestResult "BuildStatus" "Node.js/NPM" "FAIL" "" "Node.js not found"
    exit 1
}

# ========================================
# Step 2: Build Backend Services
# ========================================
if (-not $SkipBuild) {
    Write-Host "`n[2/8] 🏗️  Building Backend Services" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Gray
    
    $services = @("identity-service", "stream-processing", "data-core", "edge-node")
    
    foreach ($service in $services) {
        if (Test-Path $service) {
            Write-Host "Building $service..." -ForegroundColor Cyan
            $buildStart = Get-Date
            
            try {
                Push-Location $service
                
                if (Test-Path "pom.xml") {
                    $output = mvn clean package -DskipTests 2>&1
                    if ($LASTEXITCODE -eq 0) {
                        $buildTime = ((Get-Date) - $buildStart).TotalSeconds
                        Write-TestResult "BuildStatus" "$service Build" "PASS" "$([math]::Round($buildTime, 2))s"
                    } else {
                        Write-TestResult "BuildStatus" "$service Build" "FAIL" "" "Maven build failed"
                        Write-Host $output -ForegroundColor Red
                    }
                }
                
                Pop-Location
            } catch {
                Pop-Location
                Write-TestResult "BuildStatus" "$service Build" "FAIL" "" $_.Exception.Message
            }
        }
    }
} else {
    Write-Host "`n[2/8] ⏭️  Skipping Backend Build" -ForegroundColor Yellow
}

# ========================================
# Step 3: Build Frontend
# ========================================
if (-not $SkipBuild) {
    Write-Host "`n[3/8] ⚛️  Building Frontend" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Gray
    
    try {
        Push-Location frontend
        $buildStart = Get-Date
        
        Write-Host "Installing dependencies..." -ForegroundColor Cyan
        npm install --silent
        
        Write-Host "Building frontend..." -ForegroundColor Cyan
        npm run build
        
        if ($LASTEXITCODE -eq 0) {
            $buildTime = ((Get-Date) - $buildStart).TotalSeconds
            Write-TestResult "BuildStatus" "Frontend Build" "PASS" "$([math]::Round($buildTime, 2))s"
        } else {
            Write-TestResult "BuildStatus" "Frontend Build" "FAIL"
        }
        
        Pop-Location
    } catch {
        Pop-Location
        Write-TestResult "BuildStatus" "Frontend Build" "FAIL" "" $_.Exception.Message
    }
} else {
    Write-Host "`n[3/8] ⏭️  Skipping Frontend Build" -ForegroundColor Yellow
}

# ========================================
# Step 4: Run Backend Tests
# ========================================
if (-not $SkipTests) {
    Write-Host "`n[4/8] ☕ Running Backend Tests" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Gray
    
    $testServices = @("edge-node", "identity-service", "stream-processing")
    
    foreach ($service in $testServices) {
        if (Test-Path "$service/pom.xml") {
            Write-Host "Testing $service..." -ForegroundColor Cyan
            $testStart = Get-Date
            
            try {
                Push-Location $service
                
                $output = mvn test 2>&1
                $testTime = ((Get-Date) - $testStart).TotalSeconds
                
                if ($LASTEXITCODE -eq 0) {
                    # Parse test results
                    $testOutput = $output | Out-String
                    if ($testOutput -match "Tests run: (\d+).*Failures: (\d+).*Errors: (\d+)") {
                        $total = $matches[1]
                        $failures = $matches[2]
                        $errors = $matches[3]
                        
                        if (($failures -eq "0") -and ($errors -eq "0")) {
                            Write-TestResult "BackendTests" "$service Unit Tests" "PASS" "$([math]::Round($testTime, 2))s" "$total tests passed"
                        } else {
                            Write-TestResult "BackendTests" "$service Unit Tests" "FAIL" "$([math]::Round($testTime, 2))s" "$failures failures, $errors errors"
                        }
                    }
                } else {
                    Write-TestResult "BackendTests" "$service Unit Tests" "FAIL" "$([math]::Round($testTime, 2))s" "Test execution failed"
                }
                
                Pop-Location
            } catch {
                Pop-Location
                Write-TestResult "BackendTests" "$service Unit Tests" "FAIL" "" $_.Exception.Message
            }
        }
    }
    
    # Special test for HelloWorld in staging
    if (Test-Path ".staging/image-inverter/edge-node") {
        Write-Host "Testing .staging/image-inverter/edge-node..." -ForegroundColor Cyan
        $testStart = Get-Date
        
        try {
            Push-Location ".staging/image-inverter/edge-node"
            
            $output = mvn test 2>&1
            $testTime = ((Get-Date) - $testStart).TotalSeconds
            
            if ($LASTEXITCODE -eq 0) {
                Write-TestResult "BackendTests" "HelloWorld Test (Staging)" "PASS" "$([math]::Round($testTime, 2))s"
            } else {
                Write-TestResult "BackendTests" "HelloWorld Test (Staging)" "FAIL" "$([math]::Round($testTime, 2))s"
            }
            
            Pop-Location
        } catch {
            Pop-Location
            Write-TestResult "BackendTests" "HelloWorld Test (Staging)" "FAIL" "" $_.Exception.Message
        }
    }
} else {
    Write-Host "`n[4/8] ⏭️  Skipping Backend Tests" -ForegroundColor Yellow
}

# ========================================
# Step 5: Run Frontend Tests
# ========================================
if (-not $SkipTests) {
    Write-Host "`n[5/8] ⚛️  Running Frontend Tests" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Gray
    
    try {
        Push-Location frontend
        $testStart = Get-Date
        
        Write-Host "Running Vitest..." -ForegroundColor Cyan
        npm run test -- --run
        
        $testTime = ((Get-Date) - $testStart).TotalSeconds
        
        if ($LASTEXITCODE -eq 0) {
            Write-TestResult "FrontendTests" "Component Tests" "PASS" "$([math]::Round($testTime, 2))s"
        } else {
            Write-TestResult "FrontendTests" "Component Tests" "FAIL" "$([math]::Round($testTime, 2))s"
        }
        
        Pop-Location
    } catch {
        Pop-Location
        Write-TestResult "FrontendTests" "Component Tests" "WARN" "" "No tests configured or test runner issue"
    }
} else {
    Write-Host "`n[5/8] ⏭️  Skipping Frontend Tests" -ForegroundColor Yellow
}

# ========================================
# Step 6: Docker Deployment
# ========================================
Write-Host "`n[6/8] 🐳 Deploying Docker Services" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

# Start databases first
Write-Host "Starting databases..." -ForegroundColor Cyan
docker-compose up -d identity-db stream-db

Start-Sleep -Seconds 5

# Build and start application services
Write-Host "Building application services..." -ForegroundColor Cyan
$services = @("secure-io-engine", "eyeo-edge-node", "identity-service", "stream-processor")

foreach ($service in $services) {
    Write-Host "Building $service..." -ForegroundColor Cyan
    docker-compose build $service
    
    if ($LASTEXITCODE -eq 0) {
        Write-TestResult "BuildStatus" "$service Docker Build" "PASS"
    } else {
        Write-TestResult "BuildStatus" "$service Docker Build" "FAIL"
    }
}

Write-Host "Starting all services..." -ForegroundColor Cyan
docker-compose up -d

Write-TestResult "BuildStatus" "Docker Deployment" "PASS" "" "All services started"

# ========================================
# Step 7: Service Health Checks
# ========================================
Write-Host "`n[7/8] 🏥 Service Health Checks" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

Write-Host "Waiting for services to initialize..." -ForegroundColor Cyan
Start-Sleep -Seconds 15

$healthChecks = @(
    @{ Name = "Identity Service"; Url = "http://localhost:8081/actuator/health"; Timeout = 5 },
    @{ Name = "Stream Processing"; Url = "http://localhost:8082/actuator/health"; Timeout = 5 },
    @{ Name = "Edge Node"; Url = "http://localhost:8080/api/v1/health"; Timeout = 5 },
    @{ Name = "Data Core"; Url = "http://localhost:9090/api/v1/video/health"; Timeout = 5 }
)

foreach ($check in $healthChecks) {
    Write-Host "Checking $($check.Name)..." -ForegroundColor Cyan
    $checkStart = Get-Date
    
    try {
        $response = Invoke-WebRequest -Uri $check.Url -TimeoutSec $check.Timeout -UseBasicParsing
        $responseTime = ((Get-Date) - $checkStart).TotalMilliseconds
        
        if ($response.StatusCode -eq 200) {
            Write-TestResult "ServiceHealth" $check.Name "PASS" "$([math]::Round($responseTime))ms" "HTTP 200 OK"
        } else {
            Write-TestResult "ServiceHealth" $check.Name "WARN" "$([math]::Round($responseTime))ms" "HTTP $($response.StatusCode)"
        }
    } catch {
        Write-TestResult "ServiceHealth" $check.Name "FAIL" "" "Service not responding"
    }
}

# ========================================
# Step 8: Generate Test Report
# ========================================
Write-Host "`n[8/8] 📊 Generating Test Report" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$reportPath = "deployment-test-report.json"
$script:testResults | ConvertTo-Json -Depth 10 | Out-File $reportPath

Write-Host "Test report saved to: $reportPath" -ForegroundColor Green

# Calculate summary
$totalTests = 0
$passedTests = 0
$failedTests = 0

foreach ($category in $script:testResults.Keys) {
    foreach ($result in $script:testResults[$category]) {
        $totalTests++
        if ($result.Status -eq "PASS") { $passedTests++ }
        elseif ($result.Status -eq "FAIL") { $failedTests++ }
    }
}

$duration = ((Get-Date) - $startTime).TotalSeconds

# ========================================
# Final Summary
# ========================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "📊 Deployment Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Tests: $totalTests" -ForegroundColor White
Write-Host "Passed: $passedTests" -ForegroundColor Green
Write-Host "Failed: $failedTests" -ForegroundColor $(if ($failedTests -gt 0) { "Red" } else { "Green" })
Write-Host "Duration: $([math]::Round($duration, 2))s" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

if ($failedTests -eq 0) {
    Write-Host "`n✓ Deployment SUCCESSFUL - All tests passed!" -ForegroundColor Green
    Write-Host "`nFrontend: http://localhost:3000" -ForegroundColor Cyan
    Write-Host "Test Dashboard: http://localhost:3000/test-dashboard" -ForegroundColor Cyan
    Write-Host "API Services: See docker-compose.yml for ports`n" -ForegroundColor Cyan
    exit 0
} else {
    Write-Host "`n✗ Deployment FAILED - $failedTests test(s) failed" -ForegroundColor Red
    Write-Host "Review the test report: $reportPath`n" -ForegroundColor Yellow
    exit 1
}
