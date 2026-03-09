---
tags:
  - nuclearwinter
  - design
created: 2026-03-08T20:05:00
type: reference
---


# Nuclear Winter -- Design Document


## Overview

Nuclear Winter is a Minecraft NeoForge mod (1.21.1) that simulates survival after a nuclear apocalypse. Players manage radiation exposure, reinforce shelters, and scavenge the surface as the world progressively deteriorates through a series of timed stages.

**Data persistence:** World-level state (current stage index, stage start tick), chunk-level state (nuked flag), and player-level state (radiation pool) are stored using NeoForge's **Data Attachment** system (`AttachmentType<T>`, registered via `RegisterAttachmentTypesEvent`). These attachments serialize to NBT automatically and persist across world saves/loads.

Inspired by the [Solar Apocalypse](https://modrinth.com/mod/solar-apocalypse) mod, Nuclear Winter extends the concept from daytime solar damage into persistent, invisible fallout. Radiation is always present once the apocalypse begins -- time of day is irrelevant. The environment itself is the enemy: approaching a cave entrance or standing near exposed sky is dangerous, and the world visibly dies around the player as stages advance.

**Core fantasy:** Race against the clock. Players must prepare before each stage transition forces adaptation or death.

---

## Table of Contents

- [Overview](#overview)
- [1. Staging System](#1.-staging-system)
- [2. Radiation System](#2.-radiation-system)
- [3. Player Effects](#3.-player-effects)
- [4. Equipment](#4.-equipment)
- [5. Treatment -- RadAway](#5.-treatment----radaway)
- [6. Block Degradation](#6.-block-degradation)
- [7. Configuration Reference](#7.-configuration-reference)
- [8. Post-MVP / Future Work](#8.-post-mvp--future-work)
- [9. Design Notes / Open Suggestions](#9.-design-notes--open-suggestions)

---

## 1. Staging System

### Overview

4 stages with fixed timers, all configurable in duration. Stage 0 is the mod loaded but inactive. A grace period sits between Stage 0 and Stage 1 where the countdown is active but radiation is negligible.

**Staging is per-dimension.** Each loaded dimension (`ResourceKey<Level>`) maintains its own independent stage, stored in a `WorldDataAttachment` on that dimension's `Level`. The `StageManager` holds a map of dimension ID → active `StageBase` instance. When a dimension loads, the manager reads its attachment and instantiates the correct stage; when a dimension unloads, the manager tears down that entry. This means the Overworld, Nether, End, and any modded dimensions can each be at different stages simultaneously.

### Stage Structure

| Stage | Radiation | Rad Storms | World                              | Mobs                       | Sky/Weather              |
|-------|-----------|------------|------------------------------------|-----------------------------|--------------------------|
| 0     | 0%        | No         | Normal                             | Normal                      | Normal                   |
| 1     | 1%        | No         | Grass/plants stop spreading        | --                          | --                       |
| 2     | 10%       | No         | Plants/grass begin dying           | Surface animals dying off   | --                       |
| 3     | 50%       | No         | Surface blocks convert to wasteland| All surface mobs gone       | Visibly toxic/hazy       |
| 4     | 100%      | Yes        | Chunk nuking routine begins        | --                          | Rad storms begin         |

**TBD:** Mob behavior specifics (what "animals behave oddly" and "dying off" look like mechanically). Sky/weather visual implementation details for Grace, Stage 3, and Stage 4.

### Stage Durations

Total default arc: **~12 hours** of server uptime. Front-loaded so players have time to prepare during early stages, with the endgame accelerating to feel relentless. All durations are configurable.

| Period  | Default Duration | Rationale                                             |
|---------|-----------------|-------------------------------------------------------|
| Grace   | 3 hours          | Longest window -- pure preparation, no real threat    |
| Stage 1 | 3 hours          | Radiation negligible, but clock is visibly ticking    |
| Stage 2 | 2.5 hours        | World changing, pressure building                     |
| Stage 3 | 2 hours          | Deadly outside, players scrambling to reinforce       |
| Stage 4 | 1.5 hours        | Endgame -- just surviving                             |

### Stage Trigger

- **Stage 0 -> Grace Period:** Admin command targeting a specific dimension (e.g. `/nuclearwinter start overworld`) or automatic on world creation (configurable).
- **Grace -> Stage 1 and beyond:** Fixed timer per stage durations above, tracked independently per dimension.

### Chunk Nuking Routine (Stage 4)

Applies to both already-visited and newly generated chunks.

- Newly generated chunks generate naturally first, then immediately have wasteland corruption applied.
- Preserves underlying biome geography -- dead forests, dried riverbeds, cracked plains rather than a uniform grey wasteland.
- The natural biome underneath gives the wasteland character and a sense of history.

### Cut (For Now)

- Fallout blocks
- Hostile mob spawn rate changes
- Food poisoning

---

## 2. Radiation System

### Core Formula

```
RadiationLeft = CurrentRadiation * 0.5 * (1 / BlockResistanceMod)
```

Applied per block in a downward raycast from the sky to the player. The sky is the sole radiation source in the current design.

### Raycast Behavior

- A full raycast is always performed from sky to player, regardless of sky light level. Sky light was considered as a proxy but rejected because it does not account for block material type.
- Air blocks are skipped (not counted in the attenuation formula).
- The raycast exits early once `RadiationLeft` drops below the floor value. With the sky-to-player direction, this means radiation was fully attenuated before reaching the player -- the common case for well-sheltered players and the primary performance optimization.
- Calculated on an interval (every 5-10 ticks) and cached until the player moves blocks or the column above changes.

### Floor Constant

- **Value:** 50 Rads
- Fixed -- does not scale with stage.
- Any radiation value below the floor during the raycast is set to zero and the raycast exits.
- Intentionally fixed rather than scaling -- the emergent progression from each stage's sky emission handles difficulty scaling naturally.
- Players can always go deeper to find safety -- no stage makes shelter impossible.

### Sky Emission by Stage

The canonical radiation values. All exposure times and shelter math derive from these.

| Stage | Sky Emission (Rads/sec) |
|-------|------------------------|
| 1     | ~28                    |
| 2     | ~83 *                  |
| 3     | ~333                   |
| 4     | ~5,000                 |

\* *Stage 2 value is a placeholder -- not yet decided.*

### Block Resistance

| Material             | BlockResistanceMod | Notes                                           |
|----------------------|-------------------|-------------------------------------------------|
| Air                  | --                | Skipped in raycast                              |
| Dirt / Gravel        | 0.5               | Worst shelter, desperation only                 |
| Wood                 | 0.6               | Slightly better than dirt, still poor           |
| Stone                | 1.0               | Baseline, abundant early game                   |
| Deepslate / Obsidian | 2.0               | Natural deep-underground advantage              |
| Reinforced Concrete  | 2.5               | Craftable mid-tier, early investment reward      |
| Iron Block           | 4.0               | ~4 blocks matches ~10 stone                     |
| Water                | 8.0               | Rewards underwater builds                       |
| Lead Block           | 16.0              | ~2 blocks matches ~10 stone, expensive endgame  |

Stone is the 1.0 baseline -- intentionally weaker than real life to keep it viable as abundant early-game material. All block resistance values are configurable. Modded blocks inherit via Forge/vanilla tags, falling back to stone-type (1.0) if no tag match exists.

### Shelter Math

Each stone block (BlockResistanceMod = 1.0) halves radiation:

```
0.5^10 = 1/1024
```

10 stone blocks = player receives ~0.098% of surface radiation.

Lead at 16.0 means 2 blocks matches 10 stone -- a massive advantage, expensive to obtain. Deepslate/Obsidian at 2.0 rewards building deep underground naturally. Reinforced Concrete at 2.5 is a craftable mid-tier between stone and iron.

### Surface vs Sheltered Exposure

Assumes full surface exposure (no overhead blocks) starting at 0 Rads. Death = pool at 100% + 10 seconds of 2 HP/sec damage.

| Stage | Surface Rads/sec | Time to Fill Pool | Time to Death (surface) | 10 Stone Rads/sec | Time to Death (10 stone) | Safe Depth (stone) |
|-------|-----------------|-------------------|-------------------------|--------------------|--------------------------|---------------------|
| 1     | ~28             | ~1 hour           | ~1 hr 10 sec            | 0 (safe)           | Indefinite               | 7 blocks            |
| 2     | ~83 *           | ~20 min *         | ~20 min 10 sec *        | 0 (safe)           | Indefinite               | 8 blocks            |
| 3     | ~333            | ~5 min            | ~5 min 10 sec           | 0 (safe)           | Indefinite               | 10 blocks           |
| 4     | ~5,000          | ~20 sec           | ~30 sec                 | ~4.88              | ~5.7 hours               | 11 blocks           |

\* *Stage 2 values are placeholders -- not yet decided.*

Stage 4 safe depth is only 1 block deeper than Stage 3. This is intentional -- Stage 4 differentiates through rad storms and chunk nuking, not shelter depth.

### Intended Player Progression

- **Early stages:** Basic stone shelters hit the floor quickly and feel safe.
- **Mid stages:** Players must reinforce with denser materials.
- **Stage 4:** Only well-engineered bunkers with lead/iron/water are viable -- the surface is lethal.

### Radiation Units

- Radiation units are **Rads** (displayed to the player as `Rads`).
- Internally stored as `double`.

---

## 3. Player Effects

### Overview

Player effects are driven by an accumulated radiation pool (0-100%). The pool fills while the player is exposed and drains only once active exposure drops to zero. Effects trigger at discrete thresholds -- no gradual scaling.

### Radiation Pool

- **Max capacity:** 100,000 Rads
- **Storage:** The player's accumulated radiation pool is persisted via a `PlayerDataAttachment` (NeoForge Data Attachment on the player entity). This survives logouts, dimension changes, and server restarts.
- **Drain rate (when unexposed):** ~100 Rads/sec (~17 min to clear from full)
- **Drain behavior:** While exposed, the pool only climbs -- drain does not run. Once exposure stops, passive drain begins at a flat rate regardless of current tier.

### Threshold Effects

| Pool %  | Name         | Effects                                                |
|---------|--------------|--------------------------------------------------------|
| 0-15%   | Clean        | Nothing                                                |
| 15-35%  | Contaminated | Subtle screen vignette. Healing reduced 25%.           |
| 35-60%  | Irradiated   | Nausea. Healing reduced 75%. Hunger drains faster.     |
| 60-80%  | Poisoned     | No natural healing. Weakness I.                        |
| 80-99%  | Critical     | No healing. Blindness (heavy darkening).               |
| 100%    | Fatal        | No healing. 2 HP/sec damage. Blindness.               |

### Drain Rate Reference

| Situation               | Rate                   | Full -> Clean |
|-------------------------|------------------------|---------------|
| Sheltered, no treatment | ~100 Rads/sec          | ~17 min       |
| RadAway active          | 50,000 Rads over 3 min | Depends on current pool level |

### Blindness

- Applied as a standard Minecraft blindness effect (heavy darkening, not full black).
- Refreshed each tick while the player remains at Critical or Fatal.
- On dropping below 80%, the effect is **not immediately cleared** -- it runs out naturally (~10 seconds residual).

### HP Damage (MVP)

In the MVP, the player only receives HP damage from radiation at 100% pool (Fatal), at a rate of 2 HP/sec.

### HP Damage (Post-MVP)

A graduated damage system is planned as a follow-up:

| Pool %  | Effect                                       |
|---------|----------------------------------------------|
| 0-80%   | No damage                                    |
| 80-99%  | 1 HP every 4 seconds (light burn)            |
| 100%    | 2 HP/sec -- dead in ~10 seconds from full HP |


---

## 4. Equipment

Equipment falls into two categories: **informational** (tells the player what they cannot otherwise know) and **protective** (reduces radiation received). Without informational items, the player has no visibility into their current exposure or accumulation.

### Informational

#### Geiger Counter
- Must be **held in hand** to activate.
- Displays **raw Rads/sec** at the player's current position.
- Measures the player's current location -- not directional.

#### Dosimeter
- **Hotbar passive** -- must be in the hotbar to function.
- No visibility at all if not in hotbar.
- Displays a smooth color gradient from `RGB(0, 255, 0)` to `RGB(255, 0, 0)` as accumulation rises.
- Default max (full red) = **80,000 Rads** -- configurable by server admin.
- 80,000 is chosen to align with the Critical threshold -- red means act now.

### Protective

Protection is applied as a **percentage reduction after the raycast exits** -- the raycast handles block-based attenuation, the suit takes a cut of whatever got through. Two independent systems.

#### Suit Tiers

| Tier | Protection | Comfortable In | Surface Time |
|------|-----------|----------------|--------------|
| 1    | ~67%      | Stage 2        | ~1 hour      |
| 2    | ~92%      | Stage 3        | ~1 hour      |
| 3    | ~99%      | Stage 4        | ~30 min      |

- Tier 3 does not make Stage 4 safe indefinitely -- it extends surface time from ~20 seconds to ~30 minutes.
- No degradation in MVP -- each tier provides a fixed protection value.

**TBD:** Crafting recipes and materials for all equipment.

---

## 5. Treatment -- RadAway

A consumable item inspired by Fallout's RadAway. Removes accumulated radiation from the pool over time.

**Implementation:** RadAway is implemented as a custom `MobEffect` (`RadAwayEffect`). When the player uses a RadAway item, the effect is applied with a fixed duration. Each tick, the effect drains radiation from the player's `PlayerDataAttachment`. This leverages Minecraft's built-in effect system for duration tracking, stacking behavior, and HUD display.

| Property       | Value                                                       |
|---------------|-------------------------------------------------------------|
| Pool reduction | 50,000 Rads total                                           |
| Duration       | 3 minutes (~278 Rads drained per second)                    |
| While exposed  | Yes -- drains pool even while radiation is actively filling |
| Stacking       | Does not stack. A second RadAway resets the timer.          |

**Intent:** A genuine lifesaver, not a full reset. A player at Critical (80,000 Rads) using RadAway lands at ~30,000 Rads (Contaminated) -- stable but not safe. Buys time to reach shelter, does not substitute for it.

One tier in MVP. Additional tiers (faster drain, larger reduction) are post-MVP.

**TBD:** Crafting recipe.

---

## 6. Block Degradation

### Overview

The chunk processor starts in Stage 2, casting rays downward per column until hitting the first solid block. That block is checked against the degradation config and converted if a mapping exists. All degradation is permanent -- no reversal in current scope.

### Degradation Tiers

Each material has two degradation tiers, triggered at Stage 2 and Stage 4 respectively. Stage 2 is the first moment the world visibly changes -- Grace Period and Stage 1 leave the world intact.

### Block Type Categories

Degradation is defined by **block type category** rather than individual blocks, leveraging existing Minecraft/Forge block tags where possible. All blocks in a category share the same degradation path.

| Block Type  | Examples                                        | Stage 2 (Tier 1)  | Stage 4 (Tier 2)   |
|-------------|------------------------------------------------|--------------------|---------------------|
| Grass-type  | Grass, mycelium, podzol                        | Dead Grass         | Wasteland Dust      |
| Dirt-type   | Dirt, coarse dirt, rooted dirt                  | Parched Dirt       | Wasteland Dust      |
| Stone-type  | Stone, cobblestone, andesite, granite, diorite | Cracked Stone      | Wasteland Rubble    |
| Log-type    | Any wood log                                   | Deadwood           | *(no change)*       |
| Leaf-type   | Any leaves                                     | Dead Leaves        | Air                 |
| Plank-type  | Any wood planks                                | Ruined Planks      | *(no change)*       |

**Fixed output** -- all blocks in a category collapse to the same wasteland variant regardless of input variant (e.g. granite and andesite both become Cracked Stone).

### Custom Blocks Required

Dead Grass, Wasteland Dust, Parched Dirt, Cracked Stone, Wasteland Rubble, Deadwood, Dead Leaves, Ruined Planks.

### Custom Block Descriptions

**Dead Grass** -- Dried, dying grass in a grey-brown coloration. The first visible sign that the world is succumbing to radiation. Behaves identically to vanilla grass. Appears at Stage 2.

**Dead Leaves** -- Browning, grey-tinged leaves clinging to irradiated trees. A transitional state before full leaf death. Behaves like vanilla leaves. Appears at Stage 2, converts to Air at Stage 4.

**Parched Dirt** -- Greyed-out dirt leeched of all moisture and organic content by radiation. A visual step between normal dirt and full ash conversion. Visual direction not yet finalised. Appears at Stage 2.

**Wasteland Dust** -- Fine grey ash replacing all grass and dirt-type blocks at Stage 4. Behaves like sand -- affected by gravity, falls when unsupported. No movement penalty currently.

**Cracked Stone** -- Stone stressed by radiation and environmental decay. Visually fractured with surface cracks. No change to physical properties. Appears at Stage 2.

**Wasteland Rubble** -- Final state of stone-type degradation at Stage 4. Broken, collapsed stone debris. Visual direction undecided -- cobble-like with ash colour palette, or sandstone-like texture. No change to physical properties.

**Deadwood** -- Bleached grey dead tree logs. Drops nothing when broken -- too irradiated and brittle to yield usable material. Appears at Stage 2, no further degradation.

**Ruined Planks** -- Grey, deteriorating planks degraded in place. Visually rotten and falling apart. Retains plank shape and collision. Appears at Stage 2, no further degradation.

### Water

Water does not degrade. It remains a valid shielding and building material at all stages. Players could trivially bypass surface water degradation by placing a stone layer above it anyway, making the mechanic meaningless.

### Modded Block Fallback

1. **Tag-based inheritance** -- if a modded block carries a recognized vanilla or Forge tag (e.g. `forge:stone`, `c:logs`), it inherits that category's resistance and degradation path automatically.
2. **Final fallback** -- if no tag match exists, treat as stone-type: reasonable resistance, degrades to Cracked Stone / Wasteland Rubble.

---

## 7. Configuration Reference

All values listed are defaults. All are configurable.

### Staging

| Config                    | Default                       | Notes                                    |
|---------------------------|-------------------------------|------------------------------------------|
| Stage 0 -> Grace trigger  | Automatic on world creation   | Can be set to admin command instead      |
| Grace Period duration      | 3 hours                       | Server uptime                            |
| Stage 1 duration           | 3 hours                       | Server uptime                            |
| Stage 2 duration           | 2.5 hours                     | Server uptime                            |
| Stage 3 duration           | 2 hours                       | Server uptime                            |
| Stage 4 duration           | 1.5 hours                     | Server uptime                            |

### Radiation

| Config                       | Default          | Notes                                               |
|------------------------------|------------------|-----------------------------------------------------|
| Stage 1 sky emission          | ~28 Rads/sec     | Canonical value -- all shelter math derives from this|
| Stage 2 sky emission          | ~83 Rads/sec     | Placeholder -- not finalized                        |
| Stage 3 sky emission          | ~333 Rads/sec    |                                                     |
| Stage 4 sky emission          | ~5,000 Rads/sec  |                                                     |
| Floor constant                | 50 Rads          | Raycast exits and sets radiation to 0 below this    |
| Raycast calculation interval  | Every 5-10 ticks | Cached until player moves or column above changes   |

### Block Resistance

| Material             | BlockResistanceMod               |
|----------------------|----------------------------------|
| Air                  | Ignored (skipped in raycast)     |
| Dirt / Gravel        | 0.5                              |
| Wood                 | 0.6                              |
| Stone                | 1.0                              |
| Deepslate / Obsidian | 2.0                              |
| Reinforced Concrete  | 2.5                              |
| Iron Block           | 4.0                              |
| Water                | 8.0                              |
| Lead Block           | 16.0                             |

### Player

| Config                  | Default         | Notes                                          |
|-------------------------|-----------------|-------------------------------------------------|
| Player pool max          | 100,000 Rads    |                                                |
| Passive drain rate       | ~100 Rads/sec   | ~17 min to clear from full when unexposed      |
| Clean threshold          | 0-15%           | No effects                                     |
| Contaminated threshold   | 15-35%          | Vignette, healing -25%                         |
| Irradiated threshold     | 35-60%          | Nausea, healing -75%, faster hunger            |
| Poisoned threshold       | 60-80%          | No natural healing, Weakness I                 |
| Critical threshold       | 80-99%          | No healing, blindness                          |
| Fatal threshold          | 100%            | No healing, 2 HP/sec damage, blindness         |

### RadAway

| Config          | Default       | Notes                            |
|-----------------|---------------|----------------------------------|
| Pool reduction   | 50,000 Rads   | Total removed over full duration |
| Duration         | 3 minutes     | ~278 Rads drained per second     |

### Equipment

| Config                       | Default      | Notes                            |
|------------------------------|--------------|----------------------------------|
| Dosimeter full-red threshold  | 80,000 Rads  | Aligns with Critical threshold  |
| Suit Tier 1 protection        | 67%          | Radiation reduction after raycast|
| Suit Tier 2 protection        | 92%          | Radiation reduction after raycast|
| Suit Tier 3 protection        | 99%          | Radiation reduction after raycast|

### Block Degradation

All block category to degradation output mappings are configurable.

| Block Type  | Stage 2 Output    | Stage 4 Output     |
|-------------|--------------------|--------------------|
| Grass-type  | Dead Grass         | Wasteland Dust     |
| Dirt-type   | Parched Dirt       | Wasteland Dust     |
| Stone-type  | Cracked Stone      | Wasteland Rubble   |
| Log-type    | Deadwood           | *(no change)*      |
| Leaf-type   | Dead Leaves        | Air                |
| Plank-type  | Ruined Planks      | *(no change)*      |

---

## 8. Post-MVP / Future Work

Items explicitly deferred from the MVP, collected from across the design:

- **Radiation ramping:** Does radiation jump instantly to a stage's value on transition, or ramp gradually within the stage?
- **Graduated HP damage:** Critical threshold (80-99%) deals 1 HP every 4 seconds in addition to Fatal's 2 HP/sec.
- **Suit degradation:** Hazmat suits lose effectiveness as they take damage rather than providing fixed protection.
- **Additional RadAway tiers:** Faster drain, larger reduction.
- **Variant-matched degradation:** Cracked granite, cracked andesite, etc. instead of fixed output per category.
- **Radiation-at-impact degradation:** Block degradation tier driven by the radiation value at impact rather than stage config, using existing raycast math.

---

## 9. Design Notes / Open Suggestions

Items observed during document compilation. These are not part of the spec -- they are flagged for consideration.

- **Floor constant vs safe depths:** The floor constant (50 Rads) and the safe depth column (7, 8, 10, 11 stone blocks) do not align mathematically. Stage 1 sky emission (~28 Rads/sec) is already below the 50 Rad floor before hitting any block, and Stage 3 (~333 Rads/sec) would cross the 50 Rad floor after only ~3 stone blocks rather than the listed 10. The safe depths are internally consistent with a floor around ~0.5 Rads. One of these values needs adjustment during implementation -- either the floor constant or the safe depth expectations.
- **Stage naming:** The design uses Stage 0 / Grace / 1 / 2 / 3 / 4. The current codebase uses `PREAPOC` / `APOCLOW` / `APOCMED` / `APOCHIGH` / `POSTAPOC`. The canonical naming should be reconciled.
- **Sound design:** No audio design is documented. Geiger counter clicks, ambient radiation hum, and rad storm audio could significantly enhance the experience.
- **Multiplayer considerations:** Radiation is per-player (each player has their own pool and raycast). Staging is per-dimension — each loaded dimension (`ResourceKey<Level>`) maintains its own independent stage via its `WorldDataAttachment`, and the `StageManager` tracks all loaded dimensions in a map. Admin commands target a specific dimension.