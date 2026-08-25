@echo off
REM GitHub Setup Script for Depot Management System (Batch version)
REM This script initializes Git and pushes to GitHub

setlocal enabledelayedexpansion

cls
echo ========================================
echo Depot Management System - GitHub Setup
echo ========================================
echo.

REM Check if Git is installed
echo Checking if Git is installed...
git --version >nul 2>&1
if errorlevel 1 (
    echo Error: Git not installed!
    echo Download from: https://git-scm.com/download/win
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('git --version') do set GIT_VERSION=%%i
echo ✓ Git found: %GIT_VERSION%
echo.

echo Step 1: Configure Git User
echo ========================================
set /p userName="Enter your GitHub username: "
set /p userEmail="Enter your GitHub email: "

git config user.name "%userName%"
git config user.email "%userEmail%"
echo ✓ Git user configured: %userName%
echo.

echo Step 2: Initialize Repository
echo ========================================
if exist ".git" (
    echo ! Repository already initialized
) else (
    git init
    echo ✓ Git repository initialized
)
echo.

echo Step 3: Check Git Status
echo ========================================
git status
echo.

echo Step 4: Add Remote Repository
echo ========================================
git remote -v >nul 2>&1
if errorlevel 1 (
    set /p repoUrl="Enter your GitHub repository URL: "
    git remote add origin "!repoUrl!"
    echo ✓ Remote added
) else (
    echo ! Remote already exists
    set /p useExisting="Use existing remote? (y/n): "
    if /i "!useExisting!"=="n" (
        set /p repoUrl="Enter your GitHub repository URL: "
        git remote remove origin
        git remote add origin "!repoUrl!"
        echo ✓ Remote updated
    )
)
echo.

echo Step 5: Add Files to Git
echo ========================================
git add .
echo ✓ All files staged for commit
echo.

echo Step 6: Create Initial Commit
echo ========================================
git commit -m "Initial commit: Depot Management System with GPS Tracking

- JavaFX desktop application for bus depot management
- Real-time GPS tracking simulation
- SQL Server database integration
- Employee and route management
- GitHub Actions automated builds"

if errorlevel 1 (
    echo ✗ Commit failed
    pause
    exit /b 1
)
echo ✓ Initial commit created
echo.

echo Step 7: Push to GitHub
echo ========================================
git branch -M main
echo ✓ Branch set to 'main'

echo Pushing to GitHub...
echo (You may be prompted for credentials)
git push -u origin main

if errorlevel 1 (
    echo ✗ Push failed
    echo Create a token at: https://github.com/settings/tokens/new
    pause
    exit /b 1
)

echo ✓ Successfully pushed to GitHub!
echo.

echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Go to: https://github.com/%userName%/DepotManagementSystem
echo 2. Check the Actions tab for build status
echo 3. Read GITHUB_SETUP.md for more info
echo.

pause
