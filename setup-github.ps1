# GitHub Setup Script for Depot Management System
# This script automates the initial Git setup and GitHub push

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Depot Management System - GitHub Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Git is installed
Write-Host "Checking if Git is installed..." -ForegroundColor Yellow
try {
    $gitVersion = git --version 2>&1
    Write-Host "✓ Git found: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Git not installed! Download from: https://git-scm.com/download/win" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 1: Configure Git User" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Get user information
$userName = Read-Host "Enter your GitHub username"
$userEmail = Read-Host "Enter your GitHub email"

# Configure Git
git config user.name "$userName"
git config user.email "$userEmail"

Write-Host "✓ Git user configured: $userName <$userEmail>" -ForegroundColor Green
Write-Host ""

Write-Host "Step 2: Initialize Repository" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Initialize git repository
if (Test-Path ".git") {
    Write-Host "! Repository already initialized" -ForegroundColor Yellow
} else {
    git init
    Write-Host "✓ Git repository initialized" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 3: Check Git Status" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
git status

Write-Host ""
Write-Host "Step 4: Add Remote Repository" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Check if remote already exists
$existingRemote = git remote -v 2>&1 | Select-String "origin"
if ($existingRemote) {
    Write-Host "! Remote 'origin' already exists:" -ForegroundColor Yellow
    Write-Host $existingRemote -ForegroundColor Yellow
    $useExisting = Read-Host "Use existing remote? (y/n)"
    if ($useExisting -ne "y") {
        $repoUrl = Read-Host "Enter your GitHub repository URL (e.g., https://github.com/username/DepotManagementSystem.git)"
        git remote remove origin
        git remote add origin "$repoUrl"
        Write-Host "✓ Remote updated: $repoUrl" -ForegroundColor Green
    }
} else {
    $repoUrl = Read-Host "Enter your GitHub repository URL (e.g., https://github.com/username/DepotManagementSystem.git)"
    git remote add origin "$repoUrl"
    Write-Host "✓ Remote added: $repoUrl" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 5: Add Files to Git" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
git add .
Write-Host "✓ All files staged for commit" -ForegroundColor Green

Write-Host ""
Write-Host "Step 6: Create Initial Commit" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
git commit -m "Initial commit: Depot Management System with GPS Tracking and GitHub Actions

- JavaFX desktop application for bus depot management
- Real-time GPS tracking simulation with 5-second updates
- SQL Server database integration
- Employee and route management system
- Fuel consumption tracking and analysis
- Automated builds via GitHub Actions
- Ready for EXE packaging"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Initial commit created" -ForegroundColor Green
} else {
    Write-Host "✗ Commit failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 7: Push to GitHub" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Rename branch to main if needed
git branch -M main
Write-Host "✓ Branch renamed to 'main'" -ForegroundColor Green

# Push to GitHub
Write-Host "Pushing to GitHub..." -ForegroundColor Yellow
Write-Host "(You may be prompted for credentials or a Personal Access Token)" -ForegroundColor Yellow
git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Successfully pushed to GitHub!" -ForegroundColor Green
} else {
    Write-Host "✗ Push failed. Check your credentials." -ForegroundColor Red
    Write-Host "Create a Personal Access Token at: https://github.com/settings/tokens/new" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setup Complete! ✓" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Go to: https://github.com/$userName/DepotManagementSystem" -ForegroundColor White
Write-Host "2. Check the Actions tab to see your build in progress" -ForegroundColor White
Write-Host "3. Read GITHUB_SETUP.md for more information" -ForegroundColor White
Write-Host ""
Write-Host "To create a release:" -ForegroundColor Cyan
Write-Host "  git tag -a v1.0.0 -m 'Release version 1.0.0'" -ForegroundColor White
Write-Host "  git push origin v1.0.0" -ForegroundColor White
Write-Host ""
