#!/bin/bash
# E4MC 1.7.10 Build Script for Unix/Linux systems

echo "Setting up E4MC 1.7.10 Build Environment"
echo ""

# Set Java 8 environment
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

# Navigate to script directory
cd "$(dirname "$0")"

echo "Java version being used:"
./gradlew -version

echo ""
echo "Cleaning previous builds..."
./gradlew clean

echo ""
echo "Building project..."
./gradlew build -x setupDevWorkspace

if [ $? -eq 0 ]; then
    echo ""
    echo "Build completed successfully!"
    echo "JAR files are located in: build/libs/"
    echo ""
    
    # List generated JAR files
    if ls build/libs/*.jar >/dev/null 2>&1; then
        ls -la build/libs/*.jar
    else
        echo "No JAR files found in build/libs/"
    fi
else
    echo "ERROR: Build failed"
    exit 1
fi

echo ""
echo "Build script completed!"
