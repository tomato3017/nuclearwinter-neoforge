# Coding Standards

Conventions and patterns used in this codebase. Follow these when writing new code.

## Formatting

- 4 spaces indentation (no tabs)
- Egyptian brace style (opening brace on same line)
- One statement per line

## Naming

| Element | Convention | Examples |
|---------|-----------|----------|
| Classes | PascalCase, descriptive nouns | `StageManager`, `ChunkProcessor` |
| Registration holders classes | `NW` prefix | `NWItems`, `NWBlocks`, `NWBiomes` |
| Suffixes | Role-based | `*Item`, `*Handler`, `*Loader`, `*Effect` |
| Methods | camelCase verbs | `onPlayerTick()`, `parseAndValidateRules()` |
| Event handlers | `on` prefix | `onServerStarting()`, `onChunkLoad()` |
| Instance fields | camelCase, private/protected | `stageIndex`, `skyEmission` |
| Constants | UPPER_SNAKE_CASE | `MODID`, `COLUMNS_PER_TICK` |
| Config values | UPPER_SNAKE_CASE with section prefix | `STAGE1_DURATION`, `RESISTANCE_LEAD` |
| DeferredHolder names | UPPER_SNAKE_CASE matching registry id | `DEAD_GRASS`, `HAZMAT_T1_HELMET` |
| Enums | PascalCase class, UPPER_SNAKE_CASE values | `HazmatTier.TIER_1`, `StageType.GRACE_PERIOD` |
| Packages | flat under `net.tomato3017.nuclearwinter` | `.stage`, `.radiation`, `.chunk` |

## Resource/Asset Naming

- File names: `snake_case.json` — `dead_grass.json`, `stage1.json`
- Language keys: dot-hierarchical — `block.nuclearwinter.dead_grass`, `nuclearwinter.configuration.staging.stage1_duration`
- Blockstate/model paths: `assets/nuclearwinter/blockstates/<block>.json`, `assets/nuclearwinter/models/block/<block>.json`
- Data paths: `data/nuclearwinter/<type>/<file>.json`

## Class Organization

Field and method order within a class:

1. Static constants (`public static final`)
2. Static fields
3. Instance fields (protected before private)
4. Constructor(s)
5. Public methods (getters/setters, then event handlers, then utilities)
6. Protected methods
7. Private helpers
8. Inner classes/records/interfaces (at end)

## Access Modifiers

- `public static final` for constants, DeferredHolders, loggers
- `protected` for fields/methods intended for subclass override (e.g. `StageBase` fields)
- `private` for encapsulated state
- `private static volatile` for thread-safe shared state (e.g. `BlockResolver` maps)

## Logging

Use the mod-wide SLF4J logger:

```java
// Defined once in NuclearWinter.java
public static final Logger LOGGER = LogUtils.getLogger();

// Access everywhere via:
NuclearWinter.LOGGER.info("Loaded stage {} for dimension {}", stageName, dimKey.location());
NuclearWinter.LOGGER.warn("Skipping invalid rule in {}: '{}'", source, rawRule);
NuclearWinter.LOGGER.error("Failed to parse: {}", result.error().get().message());
```

Log levels: `info` for lifecycle/state changes, `warn` for skippable data issues, `error` for failures, `trace` for debug detail.

## Config Access

Config values are static `ModConfigSpec.*Value` fields in `Config.java`. Access at runtime via `.get()`:

```java
double pool = Config.PLAYER_POOL_MAX.get();
long duration = Config.STAGE1_DURATION.get();
```

Organize new values with `.push("section")` / `.pop()` in the static builder, with `.comment()` and `.translation()`.

## Registration

All game objects use NeoForge `DeferredRegister`. Hold results as static final `DeferredHolder`/`DeferredItem`/`DeferredBlock`:

```java
// Blocks
public static final DeferredBlock<Block> DEAD_GRASS = BLOCKS.registerSimpleBlock("dead_grass",
    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.6f).sound(SoundType.GRASS));

// Block items
public static final DeferredItem<BlockItem> DEAD_GRASS_ITEM =
    ITEMS.registerSimpleBlockItem("dead_grass", NWBlocks.DEAD_GRASS);

// Custom items
public static final DeferredItem<RadAwayItem> RADAWAY = ITEMS.register("radaway",
    () -> new RadAwayItem(new Item.Properties().stacksTo(16)));
```

Block properties chain order: `.mapColor()` → `.strength()` → `.sound()` → `.noOcclusion()` → `.requiresCorrectToolForDrops()`.

## Event Handling

Two patterns depending on context:

**Instance handlers** (runtime events on `NeoForge.EVENT_BUS`):
```java
// Register in constructor: NeoForge.EVENT_BUS.register(this);
@SubscribeEvent
public void onServerStarting(ServerStartingEvent event) { ... }
```

**Static handlers** (mod lifecycle on mod bus):
```java
@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class SomeClass {
    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) { ... }
}
```

Client-only static handlers use `@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)`.

## Data Classes

Use **records** for immutable data with Codec support:

```java
public record DegradationRuleEntry(
    String match,
    String replacement,
    double probability,
    boolean passthrough
) {
    public static final Codec<DegradationRuleEntry> CODEC = RecordCodecBuilder.create(
        i -> i.group(
            Codec.STRING.fieldOf("match").forGetter(DegradationRuleEntry::match),
            Codec.STRING.fieldOf("replacement").forGetter(DegradationRuleEntry::replacement),
            Codec.DOUBLE.optionalFieldOf("probability", 1.0).forGetter(DegradationRuleEntry::probability),
            Codec.BOOL.optionalFieldOf("passthrough", false).forGetter(DegradationRuleEntry::passthrough)
        ).apply(i, DegradationRuleEntry::new)
    );
}
```

Use **classes** when mutable state or complex behavior is needed (`StageBase`, `ChunkProcessor`).

## Error Handling

- Early return on null/invalid: `if (stage == null) return;`
- Validate data with logging, skip bad entries: `LOGGER.warn(...); return null;`
- Wrap reflection failures as `IllegalStateException`
- Use `DataResult` for Codec parsing — check `.error().isPresent()` before proceeding
- Continue processing partial failures (don't abort entire load for one bad rule)
