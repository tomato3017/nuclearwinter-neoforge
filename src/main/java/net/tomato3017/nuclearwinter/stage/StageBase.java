package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.chunk.ChunkProcessor;
import net.tomato3017.nuclearwinter.stage.events.StageEvent;
import net.tomato3017.nuclearwinter.stage.events.StageEventFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final List<StageEventFactory> stageEventFactories;

    private ChunkProcessor chunkProcessor;
    private final Map<StageEventFactory, Long> lastStageEventTicks = new HashMap<>();
    private StageEvent currentStageEvent;

    private StageBase(Builder builder) {
        this.stageIndex = builder.stageIndex;
        this.duration = builder.duration;
        this.skyEmission = builder.skyEmission;
        this.chunkProcessingEnabled = builder.chunkProcessingEnabled;
        this.chunkProcessingIntervalMultiplier = builder.chunkProcessingIntervalMultiplier;
        this.nukeMode = builder.nukeMode;
        this.stageEventFactories = List.copyOf(builder.stageEventFactories);
    }

    public void init(ServerLevel level, long currentTick) {
        this.initTick = currentTick;
        if (chunkProcessingEnabled) {
            chunkProcessor = new ChunkProcessor(stageIndex, level, nukeMode, chunkProcessingIntervalMultiplier);
        }
        registerStageEvents(currentTick);
    }

    public void tick(ServerLevel level, long currentTick) {
        if (chunkProcessor != null) {
            chunkProcessor.tick(level);
        }

        fireStageEvents(currentTick);

        //Fire off the StageEvent if it's active.
        if (currentStageEvent != null) {
            var shouldContinue = currentStageEvent.onTick(level);
            if (!shouldContinue) {
                currentStageEvent.onEnd();
                currentStageEvent = null;
            }
        }
    }

    public void unload() {
        unregisterStageEvents();
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
            chunkProcessor.loadChunk(level, chunk.getPos());
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
        private final List<StageEventFactory> stageEventFactories = new ArrayList<>();
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

        public Builder registerStageFactory(StageEventFactory factory) {
            this.stageEventFactories.add(factory);
            return this;
        }

        @SafeVarargs
        public final Builder withStageEvents(StageEventFactory... factories) {
            for (var stageFactory : factories) {
                registerStageFactory(stageFactory);
            }
            return this;
        }

        public StageBase build() {
            return new StageBase(this);
        }

    }

    private void registerStageEvents(long currentTick) {
        if (stageEventFactories.isEmpty()) {
            return;
        }

        lastStageEventTicks.clear();
        for (StageEventFactory stageEventFactory : stageEventFactories) {
            lastStageEventTicks.put(stageEventFactory, currentTick);
        }
    }


    private void fireStageEvents(long currentTick) {
        if (this.stageEventFactories.isEmpty()) {
            return;
        }

        if (currentTick % 200 == 0 && currentStageEvent == null) { // Every 10 seconds we check for stage events.
            tryStartStageEvents(currentTick);
        }
    }

    private void tryStartStageEvents(long currentTick) {
        List<StageEventFactory> firableEvents = new ArrayList<>();
        for (StageEventFactory stageEventFactory : stageEventFactories) {
            long lastTick = lastStageEventTicks.getOrDefault(stageEventFactory, initTick);
            if (!stageEventFactory.canStageActivate(currentTick, lastTick)) {
                continue;
            }

            firableEvents.add(stageEventFactory);
        }

        for (StageEventFactory stageEventFactory : firableEvents) {
            if (stageEventFactory.tryActivate()) {
                StageEvent stageEvent = stageEventFactory.create();
                NuclearWinter.LOGGER.info("Firing stage event: " + stageEvent.getClass().getSimpleName());
                lastStageEventTicks.put(stageEventFactory, currentTick);
                stageEvent.onStart();
                currentStageEvent = stageEvent;

                break;
            }
        }
    }

    private void unregisterStageEvents() {
        if (stageEventFactories.isEmpty()) {
            return;
        }

        if (currentStageEvent != null) {
            currentStageEvent.onEnd();
            currentStageEvent = null;
        }

        lastStageEventTicks.clear();
    }

    public static Builder builder(int stageIndex) {
        return new Builder(stageIndex);
    }
}
