package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.WorldDataAttachment;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class StageManager {
    private final Map<ResourceKey<Level>, StageBase> activeStages = new HashMap<>();
    private MinecraftServer server;

    public void init(MinecraftServer server) {
        this.server = server;
        this.activeStages.clear();
    }

    public void shutdown() {
        for (var entry : activeStages.entrySet()) {
            entry.getValue().unload();
        }
        activeStages.clear();
        server = null;
    }

    public void onWorldLoad(ServerLevel level) {
        ResourceKey<Level> dimKey = level.dimension();
        WorldDataAttachment data = level.getData(NWAttachmentTypes.WORLD_DATA);
        StageBase stage = StageFactory.create(data.stageIndex());
        stage.setInitTick(data.stageStartTick());
        activeStages.put(dimKey, stage);
        NuclearWinter.LOGGER.info("Loaded stage {} for dimension {}",
                StageFactory.getStageName(data.stageIndex()), dimKey.location());
    }

    public void onWorldUnload(ServerLevel level) {
        ResourceKey<Level> dimKey = level.dimension();
        StageBase stage = activeStages.remove(dimKey);
        if (stage != null) {
            stage.unload();
            NuclearWinter.LOGGER.info("Unloaded stage for dimension {}", dimKey.location());
        }
    }

    public void tickAllStages() {
        if (server == null) return;
        for (var entry : new HashMap<>(activeStages).entrySet()) {
            ServerLevel level = server.getLevel(entry.getKey());
            if (level == null) continue;
            StageBase stage = entry.getValue();
            long currentTick = level.getGameTime();
            stage.tick(level, currentTick);
            if (stage.isExpired(currentTick)) {
                advanceStage(level);
            }
        }
    }

    public void advanceStage(ServerLevel level) {
        ResourceKey<Level> dimKey = level.dimension();
        StageBase currentStage = activeStages.get(dimKey);
        if (currentStage == null) return;

        int nextIndex = currentStage.getStageIndex() + 1;
        if (nextIndex > StageFactory.MAX_STAGE_INDEX) return;

        currentStage.unload();
        StageBase nextStage = StageFactory.create(nextIndex);
        long currentTick = level.getGameTime();
        nextStage.init(level, currentTick);
        activeStages.put(dimKey, nextStage);

        level.setData(NWAttachmentTypes.WORLD_DATA, new WorldDataAttachment(nextIndex, currentTick));
        NuclearWinter.LOGGER.info("Dimension {} advanced to {}",
                dimKey.location(), StageFactory.getStageName(nextIndex));
    }

    public void setStage(ServerLevel level, int stageIndex) {
        ResourceKey<Level> dimKey = level.dimension();
        StageBase currentStage = activeStages.get(dimKey);
        if (currentStage != null) {
            currentStage.unload();
        }

        StageBase newStage = StageFactory.create(stageIndex);
        long currentTick = level.getGameTime();
        newStage.init(level, currentTick);
        activeStages.put(dimKey, newStage);

        level.setData(NWAttachmentTypes.WORLD_DATA, new WorldDataAttachment(stageIndex, currentTick));
        NuclearWinter.LOGGER.info("Dimension {} set to {}",
                dimKey.location(), StageFactory.getStageName(stageIndex));
    }

    public void startApocalypse(ServerLevel level) {
        ResourceKey<Level> dimKey = level.dimension();
        StageBase currentStage = activeStages.get(dimKey);
        if (currentStage != null && currentStage.getStageIndex() > 0) {
            return;
        }
        setStage(level, GracePeriod.INDEX);
    }

    public void stopApocalypse(ServerLevel level) {
        setStage(level, Stage0.INDEX);
    }

    public StageBase getStageForWorld(ResourceKey<Level> dimKey) {
        return activeStages.get(dimKey);
    }

    public Map<ResourceKey<Level>, StageBase> getAllStages() {
        return Map.copyOf(activeStages);
    }
}
