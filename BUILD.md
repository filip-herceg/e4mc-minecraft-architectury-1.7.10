# E4MC Minecraft 1.7.10 - Build Instructions

## Prerequisites

1. **Java 8** - Eclipse Adoptium JDK 8.0.432.6 or similar
   - Download from: https://adoptium.net/temurin/releases/?version=8
   - Install to: `C:\Program Files\Eclipse Adoptium\jdk-8.0.432.6-hotspot`

## Build Methods

### Method 1: VS Code Tasks (Recommended)

1. Open VS Code in the project directory
2. Open Command Palette (`Ctrl+Shift+P`)
3. Run: `Tasks: Run Task`
4. Select one of:
   - **"Setup and Build All"** - Complete build process
   - **"Build Project"** - Quick build
   - **"Clean Project"** - Clean previous builds

### Method 2: Automated Build Script

Run the automated build script:
```cmd
build.bat
```

This script will:
- Clean previous builds
- Build the project (skipping setupDevWorkspace due to compatibility issues)
- Generate JAR files in `build/libs/`

### Method 3: Manual Gradle Commands

```cmd
# Clean
gradlew.bat clean

# Build (skip setupDevWorkspace)
gradlew.bat build -x setupDevWorkspace

# Run tests
gradlew.bat test
```

### Method 4: Direct Java Build

If Gradle fails due to version conflicts:
```cmd
build-direct.bat
```

## Build Output

Successful builds generate:
- `build/libs/e4mc_minecraft-1.7.10-5.4.1-1.7.10.jar` - Main mod file
- Additional JAR variants may be present

## Troubleshooting

### Java Version Issues
- Ensure Java 8 is installed and configured in `gradle.properties`
- Check that `JAVA_HOME` points to Java 8 installation
- ForgeGradle 1.2 only supports Java 8

### setupDevWorkspace Failures
- This is expected on modern systems due to ForgeGradle 1.2 limitations
- The build process skips this step automatically
- Final JAR will still be functional

### Dependencies
- Apache HttpClient 4.5.13 (Java 8 compatible)
- Gson 2.8.0
- JUnit 4.12, Mockito 1.10.19 (for tests)

## GitHub Actions

The project includes automated CI/CD:
- Builds on every push to `rererewrite` branch
- Creates releases on version tags
- Uses Ubuntu + Java 8 for consistent builds
- Uploads artifacts automatically

## Testing

Tests can be run with:
```cmd
gradlew.bat test
```

Test classes:
- `ConfigTest` - Configuration system tests
- `E4mcCommandTest` - Command system tests  
- `TextHelperTest` - Translation system tests
- `RelaySessionTest` - Networking tests
- `IntegrationTest` - System integration tests

## Deployment

The built JAR file is compatible with:
- Minecraft 1.7.10
- Minecraft Forge 10.13.4.1614
- Java 8 runtime

Place the JAR file in the `mods/` folder of a Minecraft 1.7.10 installation.
