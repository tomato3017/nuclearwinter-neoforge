# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NuclearWinter is a Minecraft 1.21.1 NeoForge mod. The concept: once an apocalypse begins, radiation is always present
regardless of time of day — cave entrances and exposed sky are dangerous, and the world visually deteriorates as stages
advance.

## Build & Run Commands

```bash
./gradlew build              # Build and produce the mod jar
./gradlew runClient          # Launch Minecraft client with mod loaded
./gradlew runServer          # Launch dedicated server with mod loaded
./gradlew runData            # Run data generator (produces resources)
./gradlew clean              # Clean build artifacts
```

Key versions are in `gradle.properties`: Minecraft 1.21.1, NeoForge 21.1.219, Java 21.

## Architecture

**Main package:** `net.tomato3017.nuclearwinter`

**Three core Java files:**

- `NuclearWinter.java` — Mod entry point (`@Mod("nuclearwinter")`). Owns all `DeferredRegister` instances for blocks,
  items, and creative tabs. Registers server-side event handlers (server startup, config reload).
- `NuclearWinterClient.java` — Client-only code (`@Mod(dist = Dist.CLIENT)`). Uses `@EventBusSubscriber` for static
  event handler registration.
- `Config.java` — `ModConfigSpec`-based config. Accessed via static fields after mod load.

**Registration pattern:** All game objects (blocks, items, etc.) must use NeoForge's `DeferredRegister` — call
`register()` at class load time and hold a `DeferredHolder` reference. Never register directly.

**Event system:** Two event buses:

- `NeoForge.EVENT_BUS` — runtime game events (server startup, player events, etc.)
- `FMLJavaModLoadingContext.get().getModEventBus()` — mod lifecycle events (setup, config load, creative tab
  population). Use `@EventBusSubscriber(bus = Bus.MOD)` for static registration on the mod bus.

**Client/server separation:** Any code using client-only classes (rendering, keybinds, etc.) must live in
`NuclearWinterClient` or a class only loaded on `Dist.CLIENT`. Common code in `NuclearWinter` runs on both sides.

**Mixin support:** Configured via `src/main/resources/nuclearwinter.mixins.json`. Add mixin classes under the `mixins`
package and register them there.

**Mod metadata:** `src/main/templates/META-INF/neoforge.mods.toml` is a template processed by Gradle using values from
`gradle.properties`. Edit metadata (description, dependencies, version range) there, not in the output file.

## Key Subsystems

### Stage System (`stage/`)

Apocalypse progression: `INACTIVE → GRACE_PERIOD → STAGE_1 → STAGE_2 → STAGE_3 → STAGE_4`. Each stage has configurable
duration and sky emission. `StageManager` maps dimensions (`ResourceKey<Level>`) to `StageBase` instances and
auto-advances stages when expired. Stage state persists via `WorldDataAttachment(stageIndex, stageStartTick)`.

### Radiation System (`radiation/`)

- `RadiationEmitter.raycastDown()` — casts from world ceiling to player, attenuating sky emission by each block's
  resistance: `currentRad *= Math.pow(0.5, resistance * mult)`.
- `PlayerRadHandler` — per-player tick handler that accumulates radiation into a pool (persisted via
  `PlayerDataAttachment`), subtracts hazmat suit protection, and applies tier-based effects.
- `RadiationTier` — enum thresholds (CLEAN → FATAL) resolved from pool percentage.
- `BlockResolver` — central registry mapping blocks/tags to radiation resistance values and degradation rules.

### Block Degradation (`chunk/ChunkProcessor`, `data/DegradationRuleLoader`)

Queue-based chunk processing on configurable interval. Per chunk, iterates columns top-down applying degradation rules
from `BlockResolver`. Rules are data-driven JSON at `data/nuclearwinter/degradation_rules/stage[1-4].json` with
inheritance support (each stage can inherit previous stage's rules). At Stage 4, chunks get fully "nuked" with biome
replacement to wasteland.

### Data Attachments (`data/`)

NeoForge attachment types with Codec serialization:

- `WORLD_DATA` — stage progression per dimension
- `CHUNK_DATA` — whether a chunk has been nuked
- `PLAYER_DATA` — radiation pool

### Items & Blocks (`item/`, `block/`)

`NWItems` and `NWBlocks` own all deferred registrations. Items include degradation blocks (dead grass, dead leaves,
etc.), shielding blocks (lead, reinforced concrete), hazmat suits (3 tiers × 4 slots), RadAway, Dosimeter, and Geiger
Counter. Textures/models live under `src/main/resources/assets/nuclearwinter/`.

### Commands (`command/`)

`/nuclearwinter` (permission level 2): `start`, `stop`, `status`, `setstage`, `stages`, `advancetime`. Debug subcommands
in `DebugCommand`.

## Coding Standards

See [CODING_STANDARDS.md](CODING_STANDARDS.md) for naming conventions, formatting, registration patterns, event
handling, data classes, and other conventions used in this codebase. Treat `CODING_STANDARDS.md` as mandatory
context for any code-related task.

## Repo Rules

- Don't worry about backwards compatibility. This is an indev mod.
- Don't touch the gradle files ever unless asked.
- Any architectural changes or balancing changes should be updated in the design doc @docs/Design Document.md
