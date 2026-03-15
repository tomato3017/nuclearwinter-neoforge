package net.tomato3017.nuclearwinter.stage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.tomato3017.nuclearwinter.Config;

public class GracePeriod extends StageBase {
    public GracePeriod() {
        super(StageType.GRACE_PERIOD.getIndex(), Config.GRACE_DURATION.get(), 0.0);
    }

    @Override
    public void tick(ServerLevel level, long currentTick) {

    }

    @Override
    public void onChunkLoaded(ServerLevel level, LevelChunk chunk) {

    }

    @Override
    public void onChunkUnloaded(ServerLevel level, LevelChunk chunk) {

    }
}
