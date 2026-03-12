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
        BUILDER.comment("Controls how long each apocalypse stage lasts.")
                .translation("nuclearwinter.configuration.staging")
                .push("staging");
        GRACE_DURATION = BUILDER.comment("Grace period duration in ticks (default 3h = 216000)")
                .translation("nuclearwinter.configuration.staging.graceDuration")
                .defineInRange("graceDuration", 216_000L, 0L, Long.MAX_VALUE);
        STAGE1_DURATION = BUILDER.comment("Stage 1 duration in ticks (default 3h = 216000)")
                .translation("nuclearwinter.configuration.staging.stage1Duration")
                .defineInRange("stage1Duration", 216_000L, 0L, Long.MAX_VALUE);
        STAGE2_DURATION = BUILDER.comment("Stage 2 duration in ticks (default 2.5h = 180000)")
                .translation("nuclearwinter.configuration.staging.stage2Duration")
                .defineInRange("stage2Duration", 180_000L, 0L, Long.MAX_VALUE);
        STAGE3_DURATION = BUILDER.comment("Stage 3 duration in ticks (default 2h = 144000)")
                .translation("nuclearwinter.configuration.staging.stage3Duration")
                .defineInRange("stage3Duration", 144_000L, 0L, Long.MAX_VALUE);
        STAGE4_DURATION = BUILDER.comment("Stage 4 duration in ticks (default 1.5h = 108000)")
                .translation("nuclearwinter.configuration.staging.stage4Duration")
                .defineInRange("stage4Duration", 108_000L, 0L, Long.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Controls radiation emission levels and raycast behaviour.")
                .translation("nuclearwinter.configuration.radiation")
                .push("radiation");
        STAGE1_SKY_EMISSION = BUILDER.comment("Stage 1 sky radiation emission (Rads/sec)")
                .translation("nuclearwinter.configuration.radiation.stage1SkyEmission")
                .defineInRange("stage1SkyEmission", 28.0, 0.0, Double.MAX_VALUE);
        STAGE2_SKY_EMISSION = BUILDER.comment("Stage 2 sky radiation emission (Rads/sec)")
                .translation("nuclearwinter.configuration.radiation.stage2SkyEmission")
                .defineInRange("stage2SkyEmission", 83.0, 0.0, Double.MAX_VALUE);
        STAGE3_SKY_EMISSION = BUILDER.comment("Stage 3 sky radiation emission (Rads/sec)")
                .translation("nuclearwinter.configuration.radiation.stage3SkyEmission")
                .defineInRange("stage3SkyEmission", 333.0, 0.0, Double.MAX_VALUE);
        STAGE4_SKY_EMISSION = BUILDER.comment("Stage 4 sky radiation emission (Rads/sec)")
                .translation("nuclearwinter.configuration.radiation.stage4SkyEmission")
                .defineInRange("stage4SkyEmission", 5000.0, 0.0, Double.MAX_VALUE);
        FLOOR_CONSTANT = BUILDER.comment("Radiation floor constant (Rads). Raycast exits below this.")
                .translation("nuclearwinter.configuration.radiation.floorConstant")
                .defineInRange("floorConstant", 50.0, 0.0, Double.MAX_VALUE);
        RAYCAST_INTERVAL_TICKS = BUILDER.comment("Ticks between radiation raycasts per player")
                .translation("nuclearwinter.configuration.radiation.raycastIntervalTicks")
                .defineInRange("raycastIntervalTicks", 10, 1, 100);
        BUILDER.pop();

        BUILDER.comment("Controls player radiation pool and passive drain.")
                .translation("nuclearwinter.configuration.player")
                .push("player");
        PLAYER_POOL_MAX = BUILDER.comment("Maximum radiation pool capacity (Rads)")
                .translation("nuclearwinter.configuration.player.poolMax")
                .defineInRange("poolMax", 100_000.0, 1.0, Double.MAX_VALUE);
        PASSIVE_DRAIN_RATE = BUILDER.comment("Passive radiation drain rate when unexposed (Rads/sec)")
                .translation("nuclearwinter.configuration.player.passiveDrainRate")
                .defineInRange("passiveDrainRate", 100.0, 0.0, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Radiation exposure thresholds as a fraction of the player's pool maximum.")
                .translation("nuclearwinter.configuration.thresholds")
                .push("thresholds");
        THRESHOLD_CONTAMINATED = BUILDER.comment("Contaminated threshold (percent of pool)")
                .translation("nuclearwinter.configuration.thresholds.contaminated")
                .defineInRange("contaminated", 0.15, 0.0, 1.0);
        THRESHOLD_IRRADIATED = BUILDER.comment("Irradiated threshold (percent of pool)")
                .translation("nuclearwinter.configuration.thresholds.irradiated")
                .defineInRange("irradiated", 0.35, 0.0, 1.0);
        THRESHOLD_POISONED = BUILDER.comment("Poisoned threshold (percent of pool)")
                .translation("nuclearwinter.configuration.thresholds.poisoned")
                .defineInRange("poisoned", 0.60, 0.0, 1.0);
        THRESHOLD_CRITICAL = BUILDER.comment("Critical threshold (percent of pool)")
                .translation("nuclearwinter.configuration.thresholds.critical")
                .defineInRange("critical", 0.80, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.comment("Controls RadAway item behaviour.")
                .translation("nuclearwinter.configuration.radaway")
                .push("radaway");
        RADAWAY_REDUCTION = BUILDER.comment("Total Rads removed by one RadAway over its duration")
                .translation("nuclearwinter.configuration.radaway.reduction")
                .defineInRange("reduction", 50_000.0, 0.0, Double.MAX_VALUE);
        RADAWAY_DURATION_TICKS = BUILDER.comment("RadAway effect duration in ticks (default 3 min = 3600)")
                .translation("nuclearwinter.configuration.radaway.durationTicks")
                .defineInRange("durationTicks", 3600, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Controls dosimeter display and hazmat suit protection values.")
                .translation("nuclearwinter.configuration.equipment")
                .push("equipment");
        DOSIMETER_FULL_RED = BUILDER.comment("Dosimeter shows full red at this Rad value")
                .translation("nuclearwinter.configuration.equipment.dosimeterFullRed")
                .defineInRange("dosimeterFullRed", 80_000.0, 1.0, Double.MAX_VALUE);
        SUIT_TIER1_PROTECTION = BUILDER.comment("Hazmat Suit Tier 1 radiation reduction (0.0 to 1.0)")
                .translation("nuclearwinter.configuration.equipment.suitTier1Protection")
                .defineInRange("suitTier1Protection", 0.67, 0.0, 1.0);
        SUIT_TIER2_PROTECTION = BUILDER.comment("Hazmat Suit Tier 2 radiation reduction (0.0 to 1.0)")
                .translation("nuclearwinter.configuration.equipment.suitTier2Protection")
                .defineInRange("suitTier2Protection", 0.92, 0.0, 1.0);
        SUIT_TIER3_PROTECTION = BUILDER.comment("Hazmat Suit Tier 3 radiation reduction (0.0 to 1.0)")
                .translation("nuclearwinter.configuration.equipment.suitTier3Protection")
                .defineInRange("suitTier3Protection", 0.99, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.comment("Radiation resistance multipliers for different block materials.")
                .translation("nuclearwinter.configuration.blockResistance")
                .push("blockResistance");
        RESISTANCE_DIRT = BUILDER.comment("Dirt/gravel radiation resistance modifier")
                .translation("nuclearwinter.configuration.blockResistance.dirt")
                .defineInRange("dirt", 0.5, 0.01, 100.0);
        RESISTANCE_WOOD = BUILDER.comment("Wood radiation resistance modifier")
                .translation("nuclearwinter.configuration.blockResistance.wood")
                .defineInRange("wood", 0.6, 0.01, 100.0);
        RESISTANCE_STONE = BUILDER.comment("Stone radiation resistance modifier (baseline)")
                .translation("nuclearwinter.configuration.blockResistance.stone")
                .defineInRange("stone", 1.0, 0.01, 100.0);
        RESISTANCE_DEEPSLATE = BUILDER.comment("Deepslate/obsidian radiation resistance modifier")
                .translation("nuclearwinter.configuration.blockResistance.deepslate")
                .defineInRange("deepslate", 2.0, 0.01, 100.0);
        RESISTANCE_REINFORCED_CONCRETE = BUILDER.comment("Reinforced concrete radiation resistance modifier")
                .translation("nuclearwinter.configuration.blockResistance.reinforcedConcrete")
                .defineInRange("reinforcedConcrete", 2.5, 0.01, 100.0);
        RESISTANCE_IRON = BUILDER.comment("Iron block radiation resistance modifier")
                .translation("nuclearwinter.configuration.blockResistance.iron")
                .defineInRange("iron", 4.0, 0.01, 100.0);
        RESISTANCE_WATER = BUILDER.comment("Water radiation resistance modifier")
                .translation("nuclearwinter.configuration.blockResistance.water")
                .defineInRange("water", 8.0, 0.01, 100.0);
        RESISTANCE_LEAD = BUILDER.comment("Lead block radiation resistance modifier")
                .translation("nuclearwinter.configuration.blockResistance.lead")
                .defineInRange("lead", 16.0, 0.01, 100.0);
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
