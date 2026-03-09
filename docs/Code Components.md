---
tags:
  - nuclearwinter
  - project
  - code
---
Code components for the nuclearwinter mod

**Nuclearwinter class**
Main class of the mod, brings it all together. All the main components of the mod are contained here.

**Stage Manager**
Responsible for maintaining the current stage of all loaded worlds. Internally holds a map of dimension ID (`ResourceKey<Level>`) to the active `StageBase` instance for that world. Each dimension progresses through stages independently.

- **World load:** When a world is loaded, Stage Manager reads the world's `WorldDataAttachment` (persisted in NBT) to determine which stage that dimension is in, instantiates the appropriate `StageBase`, and adds it to the map.
- **World unload:** When a world is unloaded, Stage Manager calls `unload()` on the active stage for that dimension, removes the entry from the map, and releases any associated resources.
- **Stage advancement:** Stage Manager is responsible for calling the next stage when the time is right for a given dimension — calling `unload()` on the old stage for cleanup, then `init()` on the new stage for setup, and updating the dimension's `WorldDataAttachment`.
- **Ticking:** Stage Manager calls each dimension's stage `tick()` when that dimension's next tick interval is hit.

**StageBase**
Abstract class that all stages inherit, contains default things like init tick time(so we can calculate when the next stage should be based on the init tick time and how long the stage should last. )

Stages 0-4 inherit this class. **Note**: Stage 0 is essentially a placeholder stage that we use when a world has no nbt data related to staging(because the admin hasn't started the mod on that world). The [[Nuclearwinter Command]] executes the start of the actual staging


**Commands**
Commands will be through a Main command class that subcommands inherit.

Where the base command may be `/nuclearwinter` and have the following subcommands.

- Status
- Stop
- Start
- SetStage


**Chunk Processor**
The chunk processor is initialized as a helper class for staging to automatically rain radiation down to the chunk in a random like way.

If chunk nuking is turned on(stage 4). The chunk processor will also check to see if the chunk has been nuked in the past(NBT capability). If not, it will automatically run a raycast down for each z column in the grid and then save a bool in the chunk so we know not to nuke it again. The random raining down will continue as normal after nuking.

**Radiation Emitter**
Handles the math and raycasting down from the sky to the player/block and is used by the Chunk Processor and PlayerRadEmitter class

**Effects generator**
Helper class that helps with effects like storms, player effects on the client side, applying statuses to the player, etc

**Mixins**
Mixins applied to the core minecraft code that requires code injection to stop functionality, namely the grass spreading logic when the grass can see the sky.

**Configuration**
Class holding all configuration values, both constants and configurable values per the design
