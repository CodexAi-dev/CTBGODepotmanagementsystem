# GitHub Setup Guide - Depot Management System

This guide will help you push the Depot Management System project to GitHub and set up automated builds.

## Prerequisites

- Git installed on your computer: https://git-scm.com/download/win
- GitHub account: https://github.com

## Step-by-Step Setup

### 1. Initialize Git Locally

Open PowerShell in the project directory:

```powershell
cd "c:\Users\Harshana\Documents\NetBeansProjects\DepotManagementSystem"
```

Initialize Git repository:

```powershell
# Initialize git
git init

# Configure your Git identity
git config user.name "Your Name"
git config user.email "your.email@gmail.com"

# Show git status
git status
```

### 2. Create GitHub Repository

1. Go to https://github.com/new
2. Create a new repository:
   - **Repository name:** `DepotManagementSystem`
   - **Description:** Sri Lankan Bus Depot Management System with GPS Tracking
   - **Visibility:** Public (or Private if you prefer)
   - **DO NOT initialize with README** (we'll add our own)
   - Click **Create Repository**

3. Copy the repository URL (it will look like):
   ```
   https://github.com/YOUR_USERNAME/DepotManagementSystem.git
   ```

### 3. Add Remote and Push

Back in PowerShell:

```powershell
# Add remote repository (replace URL with yours)
git remote add origin https://github.com/YOUR_USERNAME/DepotManagementSystem.git

# Verify remote was added
git remote -v

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: Depot Management System with GPS Tracking

- JavaFX desktop application for bus depot management
- Real-time GPS tracking simulation
- SQL Server database integration
- Employee and route management
- Fuel consumption tracking"

# Push to GitHub (this will prompt for credentials)
git branch -M main
git push -u origin main
```

**Note:** If prompted for credentials, use:
- Username: Your GitHub username
- Password: Use a Personal Access Token (see below)

### 4. Create Personal Access Token (If Needed)

If Git asks for password, GitHub no longer accepts passwords directly. Create a token:

1. Go to https://github.com/settings/tokens/new
2. Click **Generate new token (classic)**
3. Select scopes:
   - ✅ repo
   - ✅ workflow
4. Click **Generate token**
5. Copy the token and use it as the password when Git prompts

## Verify GitHub Setup

1. Go to your GitHub repository: `https://github.com/YOUR_USERNAME/DepotManagementSystem`
2. You should see all your files in the main branch
3. Check **Actions** tab to see the build status

## GitHub Actions Build Process

The `.github/workflows/build-exe.yml` file automatically:

✅ **Triggers on:**
- Every push to `main` or `develop` branch
- Every pull request to `main`
- Manual workflow dispatch

✅ **Does the following:**
1. Checks out your code
2. Sets up Java 21
3. Runs `mvn clean package`
4. Builds the JAR file
5. Uploads artifacts
6. Creates releases (when you tag)

## Creating a Release Build

To create a tagged release that will be available for download:

```powershell
# Add all changes
git add .

# Commit
git commit -m "Version 1.0.0 - Release"

# Create a tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tag to GitHub
git push origin v1.0.0
```

After pushing a tag, GitHub Actions will:
1. Build the project
2. Create a Release on GitHub
3. Attach the JAR file to the release
4. Users can download it from the Releases page

## View Build Results

1. Go to your GitHub repository
2. Click **Actions** tab
3. Click on a workflow run to see:
   - Build status (✅ Success or ❌ Failed)
   - Build logs
   - Download artifacts

## Download Built Artifacts

### Option 1: From Actions Tab
1. Go to **Actions** → Click latest workflow run
2. Scroll to **Artifacts** section
3. Download `DepotManagementSystem-JAR`

### Option 2: From Releases (Tagged Builds)
1. Go to **Releases** page
2. Find the version you want
3. Download the JAR file

## Running the Application

Users can run your application with:

```powershell
java -jar BusTrackingSystem.jar
```

Or create a shortcut batch file:

```batch
@echo off
java -jar BusTrackingSystem.jar
pause
```

## Troubleshooting

### Issue: "fatal: Not a git repository"
**Solution:** Make sure you're in the project directory and ran `git init`

### Issue: "Permission denied" when pushing
**Solution:** Use a Personal Access Token instead of password (see Step 4)

### Issue: GitHub Actions build fails
**Solution:** 
1. Check the build logs in Actions tab
2. Common issues:
   - Java version mismatch (need Java 21)
   - Missing dependencies (check Maven output)
   - Database not available (expected - simulator will run with mock data)

### Issue: Can't see .github folder on GitHub
**Solution:** GitHub shows hidden folders starting with `.` in the file viewer. The workflow file is there even if not visible.

## Next Steps

1. **Add a proper README.md** to your repository with screenshots and instructions
2. **Add project documentation** in a `/docs` folder
3. **Enable GitHub Pages** if you want to host documentation
4. **Set up branch protection rules** to require reviews before merging
5. **Configure CodeQL** for security scanning

## GitHub Actions Workflow Details

The workflow file (`.github/workflows/build-exe.yml`) includes:

- **Caching:** Maven dependencies are cached for faster builds
- **Multiple JDK support:** Uses official Oracle JDK 21
- **Artifact retention:** Artifacts kept for 30 days
- **Release automation:** Automatic release creation when you push tags

## Contact & Support

For issues with GitHub Actions:
- Check: https://github.com/features/actions
- Docs: https://docs.github.com/en/actions

---

**Last Updated:** 2026-08-25
