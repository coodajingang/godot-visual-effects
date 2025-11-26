#!/bin/bash

# Pipeline Framework Build Script

echo "=== Pipeline Framework Build Script ==="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Create build directory
mkdir -p build/classes
mkdir -p build/test-classes

echo "Compiling main classes..."

# Compile main classes (without dependencies for now)
find src/main/java -name "*.java" -print0 | xargs -0 javac -d build/classes -cp "build/classes" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ Main classes compiled successfully"
else
    echo "✗ Failed to compile main classes"
    exit 1
fi

echo "Compiling test classes..."

# Compile test classes
find src/main/java/com/dus/pipeline/test -name "*.java" -print0 | xargs -0 javac -d build/test-classes -cp "build/classes:build/test-classes" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ Test classes compiled successfully"
else
    echo "✗ Failed to compile test classes (may need JUnit)"
fi

echo "Compilation complete!"
echo "Main classes: $(find build/classes -name "*.class" | wc -l) files"
echo "Test classes: $(find build/test-classes -name "*.class" | wc -l) files"

# Show directory structure
echo ""
echo "=== Framework Structure ==="
find src/main/java/com/dus/pipeline -type f -name "*.java" | sort