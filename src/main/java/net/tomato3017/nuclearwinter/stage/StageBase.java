package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.tomato3017.nuclearwinter.chunk.ChunkProcessor;

/**
 * Concrete base for all apocalypse stages. Configured via {@link Builder}; stages that need
 * block degradation opt in with {@link Builder#withChunkProcessing()}, and Stage 4 additionally
 * enables full-column nuking via {@link Builder#withNukeMode()}.
 */
public final class StageBase {
    private final int stageIndex;
    private long initTick;
    private final long duration;
    private final double skyEmission;
    private final boolean chunkProcessingEnabled;
    private final int chunkProcessingIntervalMultiplier;
    private final boolean nukeMode;

    private ChunkProcessor chunkProcessor;

    private StageBase(Builder builder) {
        this.stageIndex = builder.stageIndex;
        this.duration = builder.duration;
        this.skyEmission = builder.skyEmission;
        this.chunkProcessingEnabled = builder.chunkProcessingEnabled;
        this.chunkProcessingIntervalMultiplier = builder.chunkProcessingIntervalMultiplier;
        this.nukeMode = builder.nukeMode;
    }

    public void init(ServerLevel level, long currentTick) {
        this.initTick = currentTick;
        if (chunkProcessingEnabled) {
            chunkProcessor = new ChunkProcessor(stageIndex, level, nukeMode, chunkProcessingIntervalMultiplier);
        }
    }

    public void tick(ServerLevel level, long currentTick) {
        if (chunkProcessor == null) return;
        chunkProcessor.tick(level);
    }

    public void unload() {
        chunkProcessor = null;
    }

    public boolean isExpired(long currentTick) {
        if (duration <= 0) return false;
        return (currentTick - initTick) >= duration;
    }

    public boolean isShouldStageExpire() {
        return duration > 0;
    }

    public void onChunkLoaded(ServerLevel level, LevelChunk chunk) {
        if (chunkProcessor != null) {
            chunkProcessor.loadChunk(chunk.getPos());
        }
    }

    public void onChunkUnloaded(ServerLevel level, LevelChunk chunk) {
        if (chunkProcessor != null) {
            chunkProcessor.unloadChunk(chunk.getPos());
        }
    }

    public boolean hasChunkProcessor() {
        return chunkProcessor != null;
    }

    public boolean isNukeMode() {
        return nukeMode;
    }

    public boolean requeueChunk(LevelChunk chunk) {
        if (chunkProcessor == null) {
            return false;
        }

        chunkProcessor.requeueChunk(chunk.getPos());
        return true;
    }

    public int getStageIndex() {
        return stageIndex;
    }

    public StageType getStageType() {
        return StageType.fromIndex(stageIndex);
    }

    public long getDuration() {
        return duration;
    }

    public double getSkyEmission() {
        return skyEmission;
    }

    public long getInitTick() {
        return initTick;
    }

    public void setInitTick(long initTick) {
        this.initTick = initTick;
    }

    /**
     * Fluent builder for {@link StageBase}. Only {@code stageIndex} is required; all other
     * settings default to "inactive" values (zero duration, zero emission, no chunk processing).
     */
    public static final class Builder {

        private final int stageIndex;
        private long duration = 0;
        private double skyEmission = 0.0;
        private boolean chunkProcessingEnabled = false;
        private int chunkProcessingIntervalMultiplier = 1;
        private boolean nukeMode = false;

        private Builder(int stageIndex) {
            this.stageIndex = stageIndex;
        }

        public Builder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder skyEmission(double skyEmission) {
            this.skyEmission = skyEmission;
            return this;
        }

        public Builder withChunkProcessing() {
            this.chunkProcessingEnabled = true;
            return this;
        }

        /**
         * Multiplies the base interval between chunk-processing passes; values below {@code 1}
         * are clamped so processing still runs every scheduled pass.
         */
        public Builder chunkProcessingIntervalMultiplier(int chunkProcessingIntervalMultiplier) {
            this.chunkProcessingIntervalMultiplier = Math.max(1, chunkProcessingIntervalMultiplier);
            return this;
        }

        /**
         * Enables full-column nuke processing; implies {@link #withChunkProcessing()}.
         */
        public Builder withNukeMode() {
            this.chunkProcessingEnabled = true;
            this.nukeMode = true;
            return this;
        }

        public StageBase build() {
            return new StageBase(this);
        }

    }

    public static Builder builder(int stageIndex) {
        return new Builder(stageIndex);
    }
}
