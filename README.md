# e4mc - Minecraft 1.7.10 Port

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/qANg5Jrr?color=%2300af5c&logo=modrinth&style=for-the-badge)](https://modrinth.com/project/qANg5Jrr)
[![Modrinth Followers](https://img.shields.io/modrinth/followers/qANg5Jrr?color=00af5c&logo=modrinth&style=for-the-badge)](https://modrinth.com/project/qANg5Jrr)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/849519?style=for-the-badge&logo=curseforge&logoColor=f16436&color=f16436)](https://curseforge.com/minecraft/mc-mods/e4mc)

Open a LAN server to anyone, anywhere, anytime.

**This is the Minecraft 1.7.10 port** of the e4mc mod using Minecraft Forge 10.13.4.1614.

## Building

This project uses a **JAVA_HOME-independent build system**. No environment variables required!

### Quick Start
```batch
# Clean and build
gradlew-java8.bat clean build

# Setup development workspace (requires Minecraft assets)
gradlew-java8.bat setupDevWorkspace
```

### Requirements
- Java 8 (automatically detected from common installation paths)
- Windows (batch scripts) - see `docs/scripts/` for cross-platform alternatives

## Install

[Modrinth](https://modrinth.com/project/qANg5Jrr)

### Maven

e4mc is available in [Skyeven](https://maven.skye.vg) under the coordinates `link.e4mc:e4mc_minecraft-[platform]:[version]`.

## Documentation

All documentation is organized in the `docs/` directory:

- **[`docs/build/`](docs/build/)** - Build system guides and troubleshooting
- **[`docs/development/`](docs/development/)** - Development and porting documentation  
- **[`docs/release/`](docs/release/)** - Release management and automation
- **[`docs/scripts/`](docs/scripts/)** - Alternative build scripts and utilities

## Usage

Open to LAN as normal

## Contributing

Please contribute

## License

[MIT](LICENSE)
