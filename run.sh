#!/bin/bash

# Investment Calculator Run Script
# This script compiles and runs the Investment Calculator application

echo "Building Investment Calculator..."
echo "Compiling Java files..."

# Compile the Java files
javac -cp "lib/*" -d . src/main/java/com/investmentcalc/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Starting Investment Calculator..."
    
    # Run the application
    java -cp ".:lib/*" com.investmentcalc.InvestmentCalculator
else
    echo "Compilation failed. Please check for errors."
    exit 1
fi
