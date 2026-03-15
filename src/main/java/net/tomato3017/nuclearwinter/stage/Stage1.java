package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.tomato3017.nuclearwinter.Config;

public class Stage1 extends StageBase {
    public Stage1() {
        super(StageType.STAGE_1.getIndex(), Config.STAGE1_DURATION.get(), Config.STAGE1_SKY_EMISSION.get());
    }

    @Override
    public void tick(ServerLevel level, long currentTick) {
        // No-op: Stage 1 has no chunk processing
    }

    @Override
    public void onChunkLoaded(ServerLevel level, LevelChunk chunk) {
        // No-op: Stage 1 doesn't track chunks
    }

    @Override
    public void onChunkUnloaded(ServerLevel level, LevelChunk chunk) {
        // No-op: Stage 1 doesn't track chunks
    }
}
