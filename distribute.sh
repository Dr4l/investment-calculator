#!/bin/bash

# Investment Calculator Run Script
# This script compiles and runs the Investment Calculator application

echo "Building Investment Calculator..."
echo "Compiling Java files..."

# Compile the Java files
javac -cp "lib/*" -d . src/main/java/com/investmentcalc/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Starting packaging..."
    
    mkdir dist
    cd dist
    rm -r *
    # Extract dependencies
    mkdir flatlaf
    cd flatlaf
        unzip ../../lib/flatlaf*
    cd ..

    mkdir jcommon
    cd jcommon
        unzip ../../lib/jcommon*
    cd ..

    mkdir jfreechart
    cd jfreechart
        unzip ../../lib/jfreechart*
    cd ..
    
    mkdir investment-calculator
    cd investment-calculator
        mkdir com
        cd com
            cp -r ../../flatlaf/com/* .
            cp -r ../../jcommon/com/* .
            cp -r ../../jfreechart/com/* .
            cp -r ../../../com/* .
        cd ..
        mkdir org
        cd org
            cp -r ../../flatlaf/org/* .
            cp -r ../../jcommon/org/* .
            cp -r ../../jfreechart/org/* .
        cd ..
        mkdir META-INF
        cd META-INF
            echo "Mainfest-Version: 1.0" > MANIFEST.MF
            echo "Main-Class: com.investmentcalc.InvestmentCalculator" >> MANIFEST.MF
            unix2dos MANIFEST.MF
        cd ..
        zip -r -9 investment-calc.jar *
     cd ..
     mv investment-calculator/investment-calc.jar .
else
    echo "Compilation failed. Please check for errors."
    exit 1
fi
