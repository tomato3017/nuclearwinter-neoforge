package net.tomato3017.nuclearwinter.stage;

import java.util.function.Supplier;

public enum StageType {
    INACTIVE    (0, "Inactive",     Stage0::new),
    GRACE_PERIOD(1, "Grace Period", GracePeriod::new),
    STAGE_1     (2, "Stage 1",      Stage1::new),
    STAGE_2     (3, "Stage 2",      Stage2::new),
    STAGE_3     (4, "Stage 3",      Stage3::new),
    STAGE_4     (5, "Stage 4",      Stage4::new);

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
