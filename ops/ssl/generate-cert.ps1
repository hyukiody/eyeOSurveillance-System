# Self-Signed SSL Certificate Generator
# Execute este script para gerar certificados SSL de desenvolvimento

Write-Host "Gerando certificado SSL auto-assinado..." -ForegroundColor Cyan

$certParams = @{
    Subject = "CN=localhost,O=EyeO Platform,C=BR"
    DnsName = @("localhost", "127.0.0.1", "api-gateway")
    CertStoreLocation = "Cert:\CurrentUser\My"
    NotAfter = (Get-Date).AddYears(1)
    KeyAlgorithm = "RSA"
    KeyLength = 2048
}

$cert = New-SelfSignedCertificate @certParams

# Export certificate
$certPath = Join-Path $PSScriptRoot "server.crt"
$keyPath = Join-Path $PSScriptRoot "server.key"

Export-Certificate -Cert $cert -FilePath $certPath -Type CERT
$mypwdSecure = ConvertTo-SecureString -String "temp" -Force -AsPlainText
Export-PfxCertificate -Cert $cert -FilePath ($certPath -replace '.crt', '.pfx') -Password $mypwdSecure

Write-Host "✓ Certificados gerados em: $PSScriptRoot" -ForegroundColor Green
Write-Host "  - server.crt (Certificado)" -ForegroundColor Yellow
Write-Host "  - server.pfx (Formato Windows)" -ForegroundColor Yellow
Write-Host "`nNOTA: Para produção, substitua por certificados válidos (Let's Encrypt)" -ForegroundColor Red
