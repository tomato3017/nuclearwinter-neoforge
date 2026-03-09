package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;

public abstract class StageBase {
    protected final int stageIndex;
    protected long initTick;
    protected final long duration;
    protected final double skyEmission;

    protected StageBase(int stageIndex, long duration, double skyEmission) {
        this.stageIndex = stageIndex;
        this.duration = duration;
        this.skyEmission = skyEmission;
    }

    public void init(ServerLevel level, long currentTick) {
        this.initTick = currentTick;
    }

    public void tick(ServerLevel level, long currentTick) {
        // Subclasses override for stage-specific behavior
    }

    public void unload() {
        // Subclasses override for cleanup
    }

    public boolean isExpired(long currentTick) {
        if (duration <= 0) return false;
        return (currentTick - initTick) >= duration;
    }

    public int getStageIndex() { return stageIndex; }
    public long getDuration() { return duration; }
    public double getSkyEmission() { return skyEmission; }
    public long getInitTick() { return initTick; }
    public void setInitTick(long initTick) { this.initTick = initTick; }
}
