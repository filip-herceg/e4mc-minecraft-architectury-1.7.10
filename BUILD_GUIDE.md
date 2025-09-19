# E4MC Minecraft 1.7.10 - Build and Release Guide

## Problem Overview
This project requires Java 8 for building but cannot use JAVA_HOME environment variable. This document provides alternative solutions for building and releasing the mod.

## GitHub Actions Solution (Recommended)

The project is configured with GitHub Actions that automatically build and release the mod when tags are pushed. This is the **preferred method** for creating releases.

### Creating a Release via GitHub Actions

1. **Commit and Push Changes**:
   ```bash
   git add .
   git commit -m "Ready for release v5.4.1"
   git push origin rererewrite
   ```

2. **Create and Push a Tag**:
   ```bash
   git tag v5.4.1
   git push origin v5.4.1
   ```

3. **Automatic Build**: GitHub Actions will automatically:
   - Set up Java 8
   - Build the project
   - Create a release with downloadable JAR files

### Manual GitHub Release

If you prefer manual control:

1. Go to your GitHub repository
2. Click "Releases" → "Create a new release"
3. Click "Choose a tag" → "Create new tag": `v5.4.1`
4. Set "Release title": `E4MC v5.4.1 for Minecraft 1.7.10`
5. GitHub Actions will automatically trigger and build the assets

## Local Development (Without Building)

For local development, you can:

1. **Edit Code**: Use VS Code with the configured Java 8 settings
2. **Run Tests**: The test framework is set up but may require manual execution
3. **Code Analysis**: SonarQube and linting work without building

### VS Code Setup

The project includes:
- `.vscode/settings.json` - Java 8 configuration
- `.vscode/tasks.json` - Build tasks (may not work locally due to JAVA_HOME restriction)
- `.vscode/launch.json` - Debug configurations

## Alternative Local Build Methods

### Method 1: Docker Build (If Available)

Create a Docker container with Java 8:
```dockerfile
FROM openjdk:8-jdk
COPY . /workspace
WORKDIR /workspace
RUN ./gradlew clean setupDevWorkspace build
```

### Method 2: Manual Java Path Configuration

If you have access to modify system settings temporarily:

1. Find your Java 8 installation path
2. Temporarily set JAVA_HOME for the build session
3. Run the build
4. Reset JAVA_HOME

### Method 3: Virtual Environment

Use a VM or container with Java 8 properly configured.

## File Structure

```
e4mc-minecraft-architectury-1.7.10/
├── src/main/java/link/e4mc/          # Main mod source code
├── src/test/java/link/e4mc/          # Unit tests
├── src/main/resources/               # Mod resources
├── .vscode/                          # VS Code configuration
├── .github/workflows/                # GitHub Actions
├── build.gradle                      # Gradle build script
├── gradle.properties                 # Build properties
└── PORT_REVIEW.md                   # Comprehensive port documentation
```

## Release Assets

Each release will contain:
- `e4mc_minecraft-1.7.10-{version}.jar` - Main mod file
- Source code (automatic GitHub archive)
- Release notes (auto-generated)

## Installation Instructions for Users

1. **Download**: Get the latest JAR from the Releases page
2. **Install Forge**: Ensure Minecraft Forge 10.13.4.1614 for 1.7.10 is installed
3. **Add Mod**: Place the JAR file in the `mods/` folder
4. **Launch**: Start Minecraft 1.7.10 with Forge

## Development Notes

### Completed Features
- ✅ Full port from 1.20.2 to 1.7.10
- ✅ Java 8 compatibility
- ✅ Comprehensive test suite
- ✅ GitHub Actions CI/CD
- ✅ VS Code development environment

### Known Limitations
- Local builds require Java 8 with proper JAVA_HOME
- ForgeGradle 1.2 compatibility issues with modern systems
- Manual testing required for some functionality

## Troubleshooting

### "Could not determine java version from '24.0.2'"
This means Gradle is detecting a newer Java version. The GitHub Actions will handle this automatically.

### "JAVA_HOME is set to an invalid directory"
This is expected in environments where JAVA_HOME cannot be modified. Use GitHub Actions for building.

### Test Execution Issues
Tests are designed for Java 8 compatibility but may require specific environment setup.

## Contact and Support

For issues with the build process or mod functionality:
1. Check existing GitHub Issues
2. Create a new Issue with detailed description
3. Use GitHub Discussions for general questions

---

**Recommended Workflow**: Use GitHub Actions for all builds and releases. Local development should focus on code editing and testing without full builds.
