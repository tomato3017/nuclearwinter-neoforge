package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;

public abstract class StageBase {
    /** Zero-based index identifying this stage in the apocalypse progression */
    protected final int stageIndex;
    /** Game tick at which this stage was initialized, set by init() */
    protected long initTick;
    /** Duration of this stage in game ticks (20 ticks = 1 second), or 0 for infinite duration */
    protected final long duration;
    /** Light emission level from the sky during this stage, affects radiation intensity */
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

    public boolean isShouldStageExpire() {
        return duration > 0;
    }

    public int getStageIndex() { return stageIndex; }
    public long getDuration() { return duration; }
    public double getSkyEmission() { return skyEmission; }
    public long getInitTick() { return initTick; }
    public void setInitTick(long initTick) { this.initTick = initTick; }
}
