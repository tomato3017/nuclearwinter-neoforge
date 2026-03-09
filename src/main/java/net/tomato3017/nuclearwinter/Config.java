package net.tomato3017.nuclearwinter;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Staging Durations (ticks; 20 ticks = 1 second) ---
    // 3 hours = 216,000 ticks
    public static final ModConfigSpec.LongValue GRACE_DURATION;
    public static final ModConfigSpec.LongValue STAGE1_DURATION;
    public static final ModConfigSpec.LongValue STAGE2_DURATION;
    public static final ModConfigSpec.LongValue STAGE3_DURATION;
    public static final ModConfigSpec.LongValue STAGE4_DURATION;

    // --- Sky Emission (Rads/sec) ---
    public static final ModConfigSpec.DoubleValue STAGE1_SKY_EMISSION;
    public static final ModConfigSpec.DoubleValue STAGE2_SKY_EMISSION;
    public static final ModConfigSpec.DoubleValue STAGE3_SKY_EMISSION;
    public static final ModConfigSpec.DoubleValue STAGE4_SKY_EMISSION;

    // --- Radiation ---
    public static final ModConfigSpec.DoubleValue FLOOR_CONSTANT;
    public static final ModConfigSpec.IntValue RAYCAST_INTERVAL_TICKS;

    // --- Player ---
    public static final ModConfigSpec.DoubleValue PLAYER_POOL_MAX;
    public static final ModConfigSpec.DoubleValue PASSIVE_DRAIN_RATE;

    // --- Thresholds (percent of pool max) ---
    public static final ModConfigSpec.DoubleValue THRESHOLD_CONTAMINATED;
    public static final ModConfigSpec.DoubleValue THRESHOLD_IRRADIATED;
    public static final ModConfigSpec.DoubleValue THRESHOLD_POISONED;
    public static final ModConfigSpec.DoubleValue THRESHOLD_CRITICAL;

    // --- RadAway ---
    public static final ModConfigSpec.DoubleValue RADAWAY_REDUCTION;
    public static final ModConfigSpec.IntValue RADAWAY_DURATION_TICKS;

    // --- Equipment ---
    public static final ModConfigSpec.DoubleValue DOSIMETER_FULL_RED;
    public static final ModConfigSpec.DoubleValue SUIT_TIER1_PROTECTION;
    public static final ModConfigSpec.DoubleValue SUIT_TIER2_PROTECTION;
    public static final ModConfigSpec.DoubleValue SUIT_TIER3_PROTECTION;

    // --- Block Resistance ---
    public static final ModConfigSpec.DoubleValue RESISTANCE_DIRT;
    public static final ModConfigSpec.DoubleValue RESISTANCE_WOOD;
    public static final ModConfigSpec.DoubleValue RESISTANCE_STONE;
    public static final ModConfigSpec.DoubleValue RESISTANCE_DEEPSLATE;
    public static final ModConfigSpec.DoubleValue RESISTANCE_REINFORCED_CONCRETE;
    public static final ModConfigSpec.DoubleValue RESISTANCE_IRON;
    public static final ModConfigSpec.DoubleValue RESISTANCE_WATER;
    public static final ModConfigSpec.DoubleValue RESISTANCE_LEAD;

    static {
        BUILDER.push("staging");
        GRACE_DURATION = BUILDER.comment("Grace period duration in ticks (default 3h = 216000)")
                .defineInRange("graceDuration", 216_000L, 0L, Long.MAX_VALUE);
        STAGE1_DURATION = BUILDER.comment("Stage 1 duration in ticks (default 3h = 216000)")
                .defineInRange("stage1Duration", 216_000L, 0L, Long.MAX_VALUE);
        STAGE2_DURATION = BUILDER.comment("Stage 2 duration in ticks (default 2.5h = 180000)")
                .defineInRange("stage2Duration", 180_000L, 0L, Long.MAX_VALUE);
        STAGE3_DURATION = BUILDER.comment("Stage 3 duration in ticks (default 2h = 144000)")
                .defineInRange("stage3Duration", 144_000L, 0L, Long.MAX_VALUE);
        STAGE4_DURATION = BUILDER.comment("Stage 4 duration in ticks (default 1.5h = 108000)")
                .defineInRange("stage4Duration", 108_000L, 0L, Long.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("radiation");
        STAGE1_SKY_EMISSION = BUILDER.comment("Stage 1 sky radiation emission (Rads/sec)")
                .defineInRange("stage1SkyEmission", 28.0, 0.0, Double.MAX_VALUE);
        STAGE2_SKY_EMISSION = BUILDER.comment("Stage 2 sky radiation emission (Rads/sec)")
                .defineInRange("stage2SkyEmission", 83.0, 0.0, Double.MAX_VALUE);
        STAGE3_SKY_EMISSION = BUILDER.comment("Stage 3 sky radiation emission (Rads/sec)")
                .defineInRange("stage3SkyEmission", 333.0, 0.0, Double.MAX_VALUE);
        STAGE4_SKY_EMISSION = BUILDER.comment("Stage 4 sky radiation emission (Rads/sec)")
                .defineInRange("stage4SkyEmission", 5000.0, 0.0, Double.MAX_VALUE);
        FLOOR_CONSTANT = BUILDER.comment("Radiation floor constant (Rads). Raycast exits below this.")
                .defineInRange("floorConstant", 50.0, 0.0, Double.MAX_VALUE);
        RAYCAST_INTERVAL_TICKS = BUILDER.comment("Ticks between radiation raycasts per player")
                .defineInRange("raycastIntervalTicks", 10, 1, 100);
        BUILDER.pop();

        BUILDER.push("player");
        PLAYER_POOL_MAX = BUILDER.comment("Maximum radiation pool capacity (Rads)")
                .defineInRange("poolMax", 100_000.0, 1.0, Double.MAX_VALUE);
        PASSIVE_DRAIN_RATE = BUILDER.comment("Passive radiation drain rate when unexposed (Rads/sec)")
                .defineInRange("passiveDrainRate", 100.0, 0.0, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("thresholds");
        THRESHOLD_CONTAMINATED = BUILDER.comment("Contaminated threshold (percent of pool)")
                .defineInRange("contaminated", 0.15, 0.0, 1.0);
        THRESHOLD_IRRADIATED = BUILDER.comment("Irradiated threshold (percent of pool)")
                .defineInRange("irradiated", 0.35, 0.0, 1.0);
        THRESHOLD_POISONED = BUILDER.comment("Poisoned threshold (percent of pool)")
                .defineInRange("poisoned", 0.60, 0.0, 1.0);
        THRESHOLD_CRITICAL = BUILDER.comment("Critical threshold (percent of pool)")
                .defineInRange("critical", 0.80, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("radaway");
        RADAWAY_REDUCTION = BUILDER.comment("Total Rads removed by one RadAway over its duration")
                .defineInRange("reduction", 50_000.0, 0.0, Double.MAX_VALUE);
        RADAWAY_DURATION_TICKS = BUILDER.comment("RadAway effect duration in ticks (default 3 min = 3600)")
                .defineInRange("durationTicks", 3600, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("equipment");
        DOSIMETER_FULL_RED = BUILDER.comment("Dosimeter shows full red at this Rad value")
                .defineInRange("dosimeterFullRed", 80_000.0, 1.0, Double.MAX_VALUE);
        SUIT_TIER1_PROTECTION = BUILDER.comment("Hazmat Suit Tier 1 radiation reduction (0.0 to 1.0)")
                .defineInRange("suitTier1Protection", 0.67, 0.0, 1.0);
        SUIT_TIER2_PROTECTION = BUILDER.comment("Hazmat Suit Tier 2 radiation reduction (0.0 to 1.0)")
                .defineInRange("suitTier2Protection", 0.92, 0.0, 1.0);
        SUIT_TIER3_PROTECTION = BUILDER.comment("Hazmat Suit Tier 3 radiation reduction (0.0 to 1.0)")
                .defineInRange("suitTier3Protection", 0.99, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("blockResistance");
        RESISTANCE_DIRT = BUILDER.comment("Dirt/gravel radiation resistance modifier")
                .defineInRange("dirt", 0.5, 0.01, 100.0);
        RESISTANCE_WOOD = BUILDER.comment("Wood radiation resistance modifier")
                .defineInRange("wood", 0.6, 0.01, 100.0);
        RESISTANCE_STONE = BUILDER.comment("Stone radiation resistance modifier (baseline)")
                .defineInRange("stone", 1.0, 0.01, 100.0);
        RESISTANCE_DEEPSLATE = BUILDER.comment("Deepslate/obsidian radiation resistance modifier")
                .defineInRange("deepslate", 2.0, 0.01, 100.0);
        RESISTANCE_REINFORCED_CONCRETE = BUILDER.comment("Reinforced concrete radiation resistance modifier")
                .defineInRange("reinforcedConcrete", 2.5, 0.01, 100.0);
        RESISTANCE_IRON = BUILDER.comment("Iron block radiation resistance modifier")
                .defineInRange("iron", 4.0, 0.01, 100.0);
        RESISTANCE_WATER = BUILDER.comment("Water radiation resistance modifier")
                .defineInRange("water", 8.0, 0.01, 100.0);
        RESISTANCE_LEAD = BUILDER.comment("Lead block radiation resistance modifier")
                .defineInRange("lead", 16.0, 0.01, 100.0);
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
