@echo off
REM TeraAPI Development Setup Script for Windows
REM Copyright (c) 2026 YiStudIo Software Inc. All rights reserved.

echo ==========================================
echo TeraAPI Development Setup
echo ==========================================
echo.

REM Check Java
where java >nul 2>nul
if errorlevel 1 (
    echo Java not found. Please install Java 17+
    exit /b 1
)
for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr version') do set JAVA_VERSION=%%i
echo [OK] Java found: %JAVA_VERSION%

REM Check Maven
where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven not found. Please install Maven 3.8+
    exit /b 1
)
for /f "tokens=*" %%i in ('mvn -v 2^>^&1 ^| findstr "Apache Maven"') do set MVN_VERSION=%%i
echo [OK] Maven found: %MVN_VERSION%

REM Check Docker
where docker >nul 2>nul
if errorlevel 1 (
    echo [WARNING] Docker not found. Some features may not work.
) else (
    for /f "tokens=*" %%i in ('docker --version') do set DOCKER_VERSION=%%i
    echo [OK] Docker found: %DOCKER_VERSION%
)

echo.
echo Building services...

REM Build parent project
call mvn clean install -f pom.xml -DskipTests

echo.
echo [OK] Build complete!
echo.
echo To start services with Docker Compose:
echo   cd docker
echo   docker-compose up -d
echo.
echo For more information, see docs\README.md
