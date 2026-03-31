package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

import java.util.function.Supplier;

public enum StageType {
    INACTIVE    (0, "Inactive",
            () -> StageBase.builder(0).build()),
    GRACE_PERIOD(1, "Grace Period",
            () -> StageBase.builder(1)
                    .duration(Config.GRACE_DURATION.get())
                    .build()),
    STAGE_1     (2, "Stage 1",
            () -> StageBase.builder(2)
                    .duration(Config.STAGE1_DURATION.get())
                    .skyEmission(Config.STAGE1_SKY_EMISSION.get())
                    .withChunkProcessing()
                    .chunkProcessingIntervalMultiplier(1)
                    .build()),
    STAGE_2     (3, "Stage 2",
            () -> StageBase.builder(3)
                    .duration(Config.STAGE2_DURATION.get())
                    .skyEmission(Config.STAGE2_SKY_EMISSION.get())
                    .withChunkProcessing()
                    .chunkProcessingIntervalMultiplier(1)
                    .build()),
    STAGE_3     (4, "Stage 3",
            () -> StageBase.builder(4)
                    .duration(Config.STAGE3_DURATION.get())
                    .skyEmission(Config.STAGE3_SKY_EMISSION.get())
                    .withChunkProcessing()
                    .chunkProcessingIntervalMultiplier(1)
                    .build()),
    STAGE_4     (5, "Stage 4",
            () -> StageBase.builder(5)
                    .skyEmission(Config.STAGE4_SKY_EMISSION.get())
                    .withNukeMode()
                    .chunkProcessingIntervalMultiplier(3)
                    .build());

    public static final int MAX_INDEX = STAGE_4.index;

    private final int index;
    private final String displayName;
    private final Supplier<StageBase> factory;

    StageType(int index, String displayName, Supplier<StageBase> factory) {
        this.index = index;
        this.displayName = displayName;
        this.factory = factory;
    }

    public int getIndex()          { return index; }
    public String getDisplayName() { return displayName; }
    public StageBase create()      { return factory.get(); }

    public boolean isAtLeast(StageType other) {
        return this.index >= other.index;
    }

    public static StageType fromIndex(int index) {
        for (StageType t : values()) if (t.index == index) return t;
        throw new IllegalArgumentException("Unknown stage index: " + index);
    }

    public static StageType fromName(String name) {
        for (StageType t : values())
            if (t.name().equalsIgnoreCase(name) || t.displayName.equalsIgnoreCase(name))
                return t;
        throw new IllegalArgumentException("Unknown stage name: " + name);
    }
}
