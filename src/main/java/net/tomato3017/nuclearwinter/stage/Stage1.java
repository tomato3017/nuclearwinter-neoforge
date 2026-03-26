package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.chunk.ChunkProcessor;

public class Stage1 extends StageBase {
    private ChunkProcessor chunkProcessor;

    public Stage1() {
        super(StageType.STAGE_1.getIndex(), Config.STAGE1_DURATION.get(), Config.STAGE1_SKY_EMISSION.get());
    }

    @Override
    public void init(ServerLevel level, long currentTick) {
        super.init(level, currentTick);
        chunkProcessor = new ChunkProcessor(stageIndex, level, false);
    }

    @Override
    public void tick(ServerLevel level, long currentTick) {
        if (chunkProcessor == null) return;
        chunkProcessor.tick(level);
    }

    @Override
    public void onChunkLoaded(ServerLevel level, LevelChunk chunk) {
        if (chunkProcessor != null) {
            chunkProcessor.loadChunk(chunk.getPos());
        }
    }

    @Override
    public void onChunkUnloaded(ServerLevel level, LevelChunk chunk) {
        if (chunkProcessor != null) {
            chunkProcessor.unloadChunk(chunk.getPos());
        }
    }

    @Override
    public void unload() {
        chunkProcessor = null;
    }
}
