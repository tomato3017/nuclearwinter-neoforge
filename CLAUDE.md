# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NuclearWinter is a Minecraft 1.21.1 NeoForge mod. The concept: once an apocalypse begins, radiation is always present regardless of time of day — cave entrances and exposed sky are dangerous, and the world visually deteriorates as stages advance.

## Build & Run Commands

```bash
./gradlew build              # Build and produce the mod jar
./gradlew runClient          # Launch Minecraft client with mod loaded
./gradlew runServer          # Launch dedicated server with mod loaded
./gradlew runData            # Run data generator (produces resources)
./gradlew runGameTestServer  # Run game tests and exit
./gradlew clean              # Clean build artifacts
```

Key versions are in `gradle.properties`: Minecraft 1.21.1, NeoForge 21.1.219, Java 21.

## Architecture

**Main package:** `net.tomato3017.nuclearwinter`

**Three core Java files:**
- `NuclearWinter.java` — Mod entry point (`@Mod("nuclearwinter")`). Owns all `DeferredRegister` instances for blocks, items, and creative tabs. Registers server-side event handlers (server startup, config reload).
- `NuclearWinterClient.java` — Client-only code (`@Mod(dist = Dist.CLIENT)`). Uses `@EventBusSubscriber` for static event handler registration.
- `Config.java` — `ModConfigSpec`-based config. Accessed via static fields after mod load.

**Registration pattern:** All game objects (blocks, items, etc.) must use NeoForge's `DeferredRegister` — call `register()` at class load time and hold a `DeferredHolder` reference. Never register directly.

**Event system:** Two event buses:
- `NeoForge.EVENT_BUS` — runtime game events (server startup, player events, etc.)
- `FMLJavaModLoadingContext.get().getModEventBus()` — mod lifecycle events (setup, config load, creative tab population). Use `@EventBusSubscriber(bus = Bus.MOD)` for static registration on the mod bus.

**Client/server separation:** Any code using client-only classes (rendering, keybinds, etc.) must live in `NuclearWinterClient` or a class only loaded on `Dist.CLIENT`. Common code in `NuclearWinter` runs on both sides.

**Mixin support:** Configured via `src/main/resources/nuclearwinter.mixins.json`. Add mixin classes under the `mixins` package and register them there.

**Mod metadata:** `src/main/templates/META-INF/neoforge.mods.toml` is a template processed by Gradle using values from `gradle.properties`. Edit metadata (description, dependencies, version range) there, not in the output file.

## Repo Rules

- Don't worry about backwards compatibility. This is an indev mod.
- Don't touch the gradle files ever unless asked.
