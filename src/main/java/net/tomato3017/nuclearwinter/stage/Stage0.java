package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public class Stage0 extends StageBase {
    public Stage0() {
        super(StageType.INACTIVE.getIndex(), 0, 0.0);
    }

    @Override
    public void tick(ServerLevel level, long currentTick) {
        // No-op: inactive stage has no tick behavior
    }

    @Override
    public void onChunkLoaded(ServerLevel level, LevelChunk chunk) {
        // No-op: inactive stage doesn't track chunks
    }

    @Override
    public void onChunkUnloaded(ServerLevel level, LevelChunk chunk) {
        // No-op: inactive stage doesn't track chunks
    }
}
