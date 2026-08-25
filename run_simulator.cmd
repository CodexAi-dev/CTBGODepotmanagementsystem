@echo off
setlocal enabledelayedexpansion

REM Set JAVA_HOME
for /f "tokens=*" %%i in ('where java') do set JAVA_PATH=%%i
for /f "tokens=*" %%i in ('%JAVA_PATH% -XshowSettings:properties -version 2^>^&1 ^| find "java.home"') do set JAVA_HOME=%%i
if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME not found
    pause
    exit /b 1
)

cd /d "%~dp0"
setlocal enabledelayedexpansion

echo Building project...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Running GPS Simulator...
java -cp "target\classes;target\dependency\*" lk.bustracking.depotmanagementsystem.simulator.GPSSimulator

pause
