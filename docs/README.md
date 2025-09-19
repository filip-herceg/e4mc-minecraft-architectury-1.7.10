# Documentation Structure

This directory contains all project documentation organized by topic.

## Directory Structure

### `/build/`
Build system documentation and guides:
- `BUILD_SUCCESS_REPORT.md` - Complete JAVA_HOME independence solution report
- `BUILD_GUIDE.md` - Step-by-step build instructions
- `BUILD.md` - General build information

### `/development/`  
Development and porting documentation:
- `PORT_REVIEW.md` - Code review and porting analysis
- `PORT_SUMMARY.md` - Port summary and implementation notes

### `/release/`
Release management documentation:
- `RELEASE_SUMMARY.md` - Release pipeline and automation guide

### `/scripts/`
Build scripts and utilities (moved from root to reduce clutter):
- `build-direct.bat` - Direct build script
- `build-mod.bat` - Mod build script  
- `build.bat` - General build script
- `build.ps1` - PowerShell build script
- `build.sh` - Shell build script
- `gradle-java8.bat` - Java 8 Gradle caller (backup/alternative)

## Root Directory
The root directory now contains only essential project files:
- Core Gradle files (`build.gradle`, `gradle.properties`, `gradlew*`)
- Source code (`src/`)
- Configuration (`.vscode/`, `.github/`)
- License and README
- Main build wrapper (`gradlew-java8.bat` - JAVA_HOME independent)

## Usage
Most documentation is self-contained. Refer to the specific files for detailed information on each topic.
