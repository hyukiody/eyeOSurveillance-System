@echo off
REM REST API Testing Script for Image Inverter (Windows Batch)
REM This script demonstrates how to test all REST API endpoints using curl

setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8080/api/images

echo.
echo ==========================================
echo Image Inverter REST API Test Script
echo ==========================================
echo.

REM Test 1: Health Check
echo.
echo [Test 1] Health Check
echo Endpoint: GET %BASE_URL%/health
echo.
echo Command: curl -X GET %BASE_URL%/health
echo.
curl -X GET %BASE_URL%/health
echo.

REM Test 2: API Information
echo.
echo [Test 2] API Information
echo Endpoint: GET %BASE_URL%/info
echo.
echo Command: curl -X GET %BASE_URL%/info
echo.
curl -X GET %BASE_URL%/info
echo.

REM Test 3: Invert Image - Binary Response
echo.
echo [Test 3] Invert Image (Binary Response)
echo Endpoint: POST %BASE_URL%/invert
echo.
echo Command: curl -X POST -F "file=@test-image.png" %BASE_URL%/invert --output inverted.png
echo.
echo Note: Replace 'test-image.png' with your actual image file
echo This will save the inverted image to 'inverted.png'
echo.

REM Test 4: Invert Image - JSON Response
echo.
echo [Test 4] Invert Image (JSON Response)
echo Endpoint: POST %BASE_URL%/invert/json
echo.
echo Command: curl -X POST -F "file=@test-image.png" %BASE_URL%/invert/json
echo.
curl -X POST -F "file=@test-image.png" %BASE_URL%/invert/json 2>nul
echo.

REM Test 5: Invert Image - XML Response
echo.
echo [Test 5] Invert Image (XML Response)
echo Endpoint: POST %BASE_URL%/invert/xml
echo.
echo Command: curl -X POST -F "file=@test-image.png" -H "Accept: application/xml" %BASE_URL%/invert/xml
echo.
curl -X POST -F "file=@test-image.png" -H "Accept: application/xml" %BASE_URL%/invert/xml 2>nul
echo.

echo.
echo ==========================================
echo Testing Complete!
echo ==========================================
echo.
echo Tips:
echo - Ensure the server is running on localhost:8080
echo - Replace 'test-image.png' with your actual image file
echo - You can use jq for pretty JSON formatting (if installed)
echo.

endlocal
