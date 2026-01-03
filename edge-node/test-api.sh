#!/bin/bash

# REST API Testing Script for Image Inverter
# This script demonstrates how to test all REST API endpoints using curl

BASE_URL="http://localhost:8080/api/images"

echo "=========================================="
echo "Image Inverter REST API Test Script"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test 1: Health Check
echo -e "${BLUE}Test 1: Health Check${NC}"
echo "Endpoint: GET $BASE_URL/health"
echo "Command:"
echo "curl http://localhost:8080/api/images/health"
echo ""
curl -X GET http://localhost:8080/api/images/health | jq
echo ""

# Test 2: API Information
echo -e "${BLUE}Test 2: API Information${NC}"
echo "Endpoint: GET $BASE_URL/info"
echo "Command:"
echo "curl http://localhost:8080/api/images/info"
echo ""
curl -X GET http://localhost:8080/api/images/info | jq
echo ""

# Test 3: Invert Image - Binary Response
echo -e "${BLUE}Test 3: Invert Image (Binary Response)${NC}"
echo "Endpoint: POST $BASE_URL/invert"
echo "Command:"
echo "curl -X POST -F 'file=@test-image.png' http://localhost:8080/api/images/invert --output inverted.png"
echo ""
echo "Note: Replace 'test-image.png' with your actual image file"
echo "This will save the inverted image to 'inverted.png'"
echo ""

# Test 4: Invert Image - JSON Response
echo -e "${BLUE}Test 4: Invert Image (JSON Response)${NC}"
echo "Endpoint: POST $BASE_URL/invert/json"
echo "Command:"
echo "curl -X POST -F 'file=@test-image.png' http://localhost:8080/api/images/invert/json"
echo ""
curl -X POST -F 'file=@test-image.png' http://localhost:8080/api/images/invert/json 2>/dev/null | jq . || echo "Note: Adjust file path as needed"
echo ""

# Test 5: Invert Image - XML Response
echo -e "${BLUE}Test 5: Invert Image (XML Response)${NC}"
echo "Endpoint: POST $BASE_URL/invert/xml"
echo "Command:"
echo "curl -X POST -F 'file=@test-image.png' -H 'Accept: application/xml' http://localhost:8080/api/images/invert/xml"
echo ""
curl -X POST -F 'file=@test-image.png' -H 'Accept: application/xml' http://localhost:8080/api/images/invert/xml 2>/dev/null || echo "Note: Adjust file path as needed"
echo ""

echo "=========================================="
echo -e "${GREEN}Testing Complete!${NC}"
echo "=========================================="
echo ""
echo "Tips:"
echo "- Ensure the server is running on localhost:8080"
echo "- Replace 'test-image.png' with your actual image file"
echo "- Install 'jq' for pretty JSON formatting: https://stedolan.github.io/jq/"
echo ""
