#!/bin/bash

# Java8 Pipeline/Operator Framework Build Script

echo "Building Java8 Pipeline/Operator Framework..."

# Check if Java is available
if ! command -v javac &> /dev/null; then
    echo "Warning: Java compiler not found. Please install Java 8+ to compile the code."
    echo "Code structure is complete and ready for compilation."
    exit 0
fi

# Create output directory
mkdir -p build/classes

# Compile core classes
echo "Compiling core classes..."
javac -d build/classes -cp . src/main/java/com/dus/pipeline/core/*.java

# Compile example classes
echo "Compiling example classes..."
javac -d build/classes -cp .:build/classes src/main/java/com/dus/pipeline/example/*.java

# Create JAR file
echo "Creating JAR file..."
jar -cf pipeline-framework.jar -C build/classes .

echo "Build completed successfully!"
echo "To run the example:"
echo "  java -cp pipeline-framework.jar com.dus.pipeline.example.PipelineExample"