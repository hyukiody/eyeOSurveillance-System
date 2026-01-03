#!/usr/bin/env pwsh
# setup.ps1 - Script de configuração inicial do EyeO Platform

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "🔒 EyeO Platform - Setup Script" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Verifica pré-requisitos
Write-Host "1. Verificando pré-requisitos..." -ForegroundColor Yellow

# Docker
try {
    $dockerVersion = docker --version
    Write-Host "  ✓ Docker: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Docker não encontrado. Instale em https://docker.com" -ForegroundColor Red
    exit 1
}

# Docker Compose
try {
    $composeVersion = docker-compose --version
    Write-Host "  ✓ Docker Compose: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Docker Compose não encontrado" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Configuração de ambiente
Write-Host "2. Configurando variáveis de ambiente..." -ForegroundColor Yellow

if (!(Test-Path ".env")) {
    Write-Host "  Criando arquivo .env a partir do template..." -ForegroundColor Gray
    Copy-Item ".env.example" ".env"
    
    # Gera senhas fortes
    function New-StrongPassword {
        $bytes = New-Object byte[] 32
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        $rng.GetBytes($bytes)
        return [Convert]::ToBase64String($bytes)
    }
    
    $dbPassword = New-StrongPassword
    $identityPassword = New-StrongPassword
    $rootPassword = New-StrongPassword
    
    # Substitui senhas no .env
    $content = Get-Content ".env" -Raw
    $content = $content -replace 'DB_PASSWORD=.*', "DB_PASSWORD=$dbPassword"
    $content = $content -replace 'IDENTITY_DB_PASSWORD=.*', "IDENTITY_DB_PASSWORD=$identityPassword"
    $content = $content -replace 'IDENTITY_MYSQL_ROOT_PASSWORD=.*', "IDENTITY_MYSQL_ROOT_PASSWORD=$rootPassword"
    $content | Set-Content ".env"
    
    Write-Host "  ✓ Senhas geradas automaticamente" -ForegroundColor Green
    Write-Host "    IMPORTANTE: Arquivo .env criado com senhas seguras!" -ForegroundColor Yellow
} else {
    Write-Host "  ✓ Arquivo .env já existe" -ForegroundColor Green
}

Write-Host ""

# Certificados SSL
Write-Host "3. Gerando certificados SSL..." -ForegroundColor Yellow

if (!(Test-Path "ops/ssl")) {
    New-Item -ItemType Directory -Path "ops/ssl" -Force | Out-Null
}

if (!(Test-Path "ops/ssl/server.crt")) {
    Write-Host "  Gerando certificado auto-assinado..." -ForegroundColor Gray
    
    $certParams = @{
        Subject = "CN=localhost,O=EyeO Platform,C=BR"
        DnsName = @("localhost", "127.0.0.1", "api-gateway", "eyeo.local")
        CertStoreLocation = "Cert:\CurrentUser\My"
        NotAfter = (Get-Date).AddYears(1)
        KeyAlgorithm = "RSA"
        KeyLength = 2048
    }
    
    $cert = New-SelfSignedCertificate @certParams
    
    # Export certificate
    $certPath = "ops/ssl/server.crt"
    $keyPath = "ops/ssl/server.key"
    
    Export-Certificate -Cert $cert -FilePath $certPath -Force | Out-Null
    
    # Export private key (PFX)
    $pfxPath = "ops/ssl/server.pfx"
    $tempPassword = ConvertTo-SecureString -String "temp123" -Force -AsPlainText
    Export-PfxCertificate -Cert $cert -FilePath $pfxPath -Password $tempPassword -Force | Out-Null
    
    # Converte PFX para PEM (key)
    # Nota: Requer OpenSSL, se disponível
    if (Get-Command openssl -ErrorAction SilentlyContinue) {
        openssl pkcs12 -in $pfxPath -nocerts -out $keyPath -nodes -password pass:temp123 2>$null
    }
    
    Write-Host "  ✓ Certificados gerados" -ForegroundColor Green
} else {
    Write-Host "  ✓ Certificados já existem" -ForegroundColor Green
}

Write-Host ""

# Build Docker images
Write-Host "4. Construindo imagens Docker..." -ForegroundColor Yellow
Write-Host "  (Isso pode levar alguns minutos...)" -ForegroundColor Gray

docker-compose build --no-cache 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Imagens construídas com sucesso" -ForegroundColor Green
} else {
    Write-Host "  ✗ Erro ao construir imagens" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Inicialização
Write-Host "5. Iniciando serviços..." -ForegroundColor Yellow

docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Serviços iniciados" -ForegroundColor Green
} else {
    Write-Host "  ✗ Erro ao iniciar serviços" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Aguarda serviços ficarem prontos
Write-Host "6. Aguardando serviços ficarem prontos..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

$services = @("sentinel-db", "crypto-core", "middleware")
foreach ($service in $services) {
    Write-Host "  Verificando $service..." -ForegroundColor Gray
    $retries = 0
    while ($retries -lt 30) {
        $health = docker-compose ps --filter "name=$service" --format json | ConvertFrom-Json
        if ($health.State -eq "running") {
            Write-Host "  ✓ $service está rodando" -ForegroundColor Green
            break
        }
        Start-Sleep -Seconds 2
        $retries++
    }
}

Write-Host ""

# Resumo
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "✓ Setup concluído com sucesso!" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Yellow
Write-Host "  1. Acesse: https://localhost/login" -ForegroundColor White
Write-Host "  2. Gere ou use sua Seed Key (mínimo 16 caracteres)" -ForegroundColor White
Write-Host "  3. Explore o Dashboard" -ForegroundColor White
Write-Host ""
Write-Host "Comandos úteis:" -ForegroundColor Yellow
Write-Host "  docker-compose ps               # Ver status dos serviços" -ForegroundColor White
Write-Host "  docker-compose logs -f          # Ver logs em tempo real" -ForegroundColor White
Write-Host "  docker-compose down             # Parar todos os serviços" -ForegroundColor White
Write-Host "  docker-compose restart [serviço] # Reiniciar serviço específico" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  IMPORTANTE: Guarde sua Seed Key em local seguro!" -ForegroundColor Red
Write-Host ""
