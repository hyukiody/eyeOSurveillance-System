#!/bin/bash
# TeraAPI Development Setup Script
# Copyright (c) 2026 YiStudIo Software Inc. All rights reserved.

set -e

echo "=========================================="
echo "TeraAPI Development Setup"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}Checking prerequisites...${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${YELLOW}Java not found. Please install Java 17+${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java found: $(java -version 2>&1 | head -n1)${NC}"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${YELLOW}Maven not found. Please install Maven 3.8+${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven found: $(mvn -v | head -n1)${NC}"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}Docker not found. Some features may not work.${NC}"
else
    echo -e "${GREEN}✓ Docker found: $(docker --version)${NC}"
fi

echo ""
echo -e "${BLUE}Building services...${NC}"

# Build parent project
mvn clean install -f pom.xml -DskipTests

echo ""
echo -e "${GREEN}✓ Build complete!${NC}"
echo ""
echo "To start services with Docker Compose:"
echo "  cd docker"
echo "  docker-compose up -d"
echo ""
echo "For more information, see docs/README.md"
