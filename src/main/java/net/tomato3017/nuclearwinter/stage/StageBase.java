package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public abstract class StageBase {
    /**
     * Zero-based index identifying this stage in the apocalypse progression
     */
    protected final int stageIndex;
    /**
     * Game tick at which this stage was initialized, set by init()
     */
    protected long initTick;
    /**
     * Duration of this stage in game ticks (20 ticks = 1 second), or 0 for infinite duration
     */
    protected final long duration;
    /**
     * Light emission level from the sky during this stage, affects radiation intensity
     */
    protected final double skyEmission;


    protected StageBase(int stageIndex, long duration, double skyEmission) {
        this.stageIndex = stageIndex;
        this.duration = duration;
        this.skyEmission = skyEmission;
    }

    public void init(ServerLevel level, long currentTick) {
        this.initTick = currentTick;
    }

    public abstract void tick(ServerLevel level, long currentTick);

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

    /**
     * Collects all unique loaded chunks within the view distance of all players on the level.
     */
    protected List<LevelChunk> gatherNearbyChunks(ServerLevel level) {
        List<LevelChunk> chunks = new ArrayList<>();
        int viewDist = level.getServer().getPlayerList().getViewDistance();
        for (var player : level.players()) {
            var chunkPos = player.chunkPosition();
            for (int dx = -viewDist; dx <= viewDist; dx++) {
                for (int dz = -viewDist; dz <= viewDist; dz++) {
                    var chunk = level.getChunkSource().getChunkNow(chunkPos.x + dx, chunkPos.z + dz);
                    if (chunk != null && !chunks.contains(chunk)) {
                        chunks.add(chunk);
                    }
                }
            }
        }
        return chunks;
    }

    public abstract void onChunkLoaded(ServerLevel level, LevelChunk chunk);

    public abstract void onChunkUnloaded(ServerLevel level, LevelChunk chunk);

    public int getStageIndex() {
        return stageIndex;
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
}
