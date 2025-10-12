@echo off
echo Building Investment Calculator...
echo Compiling Java files...

rem Compile the Java files
javac -cp "lib\*" -d . src\main\java\com\investmentcalc\*.java

if %errorlevel% equ 0 (
    echo Compilation successful!
    echo Starting Investment Calculator...
    
    rem Run the application
    java -cp ".;lib\*" com.investmentcalc.InvestmentCalculator
) else (
    echo Compilation failed. Please check for errors.
    pause
    exit /b 1
)
