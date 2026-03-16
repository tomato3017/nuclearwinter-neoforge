package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.chunk.ChunkProcessor;

public class Stage4 extends StageBase {
    private ChunkProcessor chunkProcessor;
    private int tickCounter = 0;

    public Stage4() {
        super(StageType.STAGE_4.getIndex(), 0, Config.STAGE4_SKY_EMISSION.get());
    }

    @Override
    public void init(ServerLevel level, long currentTick) {
        super.init(level, currentTick);
        chunkProcessor = new ChunkProcessor(stageIndex, level, true);
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
