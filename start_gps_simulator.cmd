@echo off
REM GPS Simulator Runner - Builds and runs the simulator with proper classpath

setlocal enabledelayedexpansion

REM Set Java home if not already set
if not defined JAVA_HOME (
    REM Try to find Java in Program Files
    if exist "C:\Program Files\Java\jdk-21.0.6" (
        set JAVA_HOME=C:\Program Files\Java\jdk-21.0.6
    ) else if exist "C:\Program Files\openjdk" (
        for /d %%x in ("C:\Program Files\openjdk\*") do set JAVA_HOME=%%x
    ) else (
        echo Error: JAVA_HOME not set and Java not found in default locations
        pause
        exit /b 1
    )
)

cd /d "%~dp0"

echo Building project with Maven...
call mvnw.cmd clean package -DskipTests -q

if %ERRORLEVEL% neq 0 (
    echo Error: Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful! Starting GPS Simulator...
echo.
echo GPS Simulator is running. Press Ctrl+C to stop.
echo.

"%JAVA_HOME%\bin\java" -cp "target\classes;target\dependency\*" lk.bustracking.depotmanagementsystem.simulator.GPSSimulator

pause
