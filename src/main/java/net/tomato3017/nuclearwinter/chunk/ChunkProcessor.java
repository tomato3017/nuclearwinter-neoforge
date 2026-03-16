package net.tomato3017.nuclearwinter.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.data.ChunkDataAttachment;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.radiation.BlockResolver;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Processes loaded chunks to degrade surface blocks based on the current stage index.
 * At Stage 4, it performs a one-time full-column nuke pass and marks the chunk via
 * {@link ChunkDataAttachment} so it is never re-processed on reload.
 */
public final class ChunkProcessor {
    private final int stageIndex;
    private final boolean chunkNukingEnabled;
    private static final int COLUMNS_PER_TICK = 16;

    private final Set<ChunkPos> loadedChunks = new HashSet<>();
    private final Queue<ChunkPos> chunks = new LinkedBlockingQueue<>();

    public ChunkProcessor(int stageIndex, ServerLevel level, boolean chunkNukingEnabled) {
        this.stageIndex = stageIndex;
        this.chunkNukingEnabled = chunkNukingEnabled;

        for (var chunk : level.getChunkSource().chunkMap.getChunks()) {
            var tickingChunk = chunk.getTickingChunk();
            if (tickingChunk == null) continue;

            loadedChunks.add(chunk.getPos());
            chunks.add(chunk.getPos());
        }
    }

    public void loadChunk(ChunkPos chunkPos) {
        loadedChunks.add(chunkPos);
        chunks.add(chunkPos);
    }

    public void unloadChunk(ChunkPos chunkPos) {
        loadedChunks.remove(chunkPos);
        chunks.remove(chunkPos);
    }

    public void tick(ServerLevel level) {
        if (level.getGameTime() % Config.CHUNK_PROCESSING_INTERVAL_TICKS.get() != 0) return;

        if (chunks.isEmpty()) {
            chunks.addAll(loadedChunks);
        }

        int processed = 0;
        while (processed < Config.CHUNK_PROCESSING_MAX_CHUNKS_PER_INTERVAL.get()) {
            ChunkPos pos = chunks.poll();
            if (pos == null) break;

            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk != null) {
                processChunk(level, chunk);
                processed++;
            }
        }
    }

    public void processChunk(ServerLevel level, LevelChunk chunk) {
        if (chunkNukingEnabled) {
            NuclearWinter.LOGGER.trace("Chunk nuking enabled, processing chunk at [{}, {}]", chunk.getPos().x, chunk.getPos().z);
            ChunkDataAttachment data = chunk.getData(NWAttachmentTypes.CHUNK_DATA);
            if (!data.nuked()) {
                nukeChunk(level, chunk);
                return;
            }
        }
        NuclearWinter.LOGGER.trace("Processing chunk at [{}, {}]", chunk.getPos().x, chunk.getPos().z);
        degradeRandomColumns(level, chunk);
    }

    //TODO This will hit only the first block and stop for each column.
    // I think we need to continue until we hit the first SOLID block or fluid.
    // Meaning grass, leaves, etc just get deleted and moved on.
    public void nukeChunk(ServerLevel level, LevelChunk chunk) {
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                degradeColumn(level, startX + dx, startZ + dz);
            }
        }

        chunk.setData(NWAttachmentTypes.CHUNK_DATA, new ChunkDataAttachment(true));
        chunk.setUnsaved(true);
        NuclearWinter.LOGGER.debug("Nuked chunk at [{}, {}]", chunk.getPos().x, chunk.getPos().z);
    }

    private void degradeRandomColumns(ServerLevel level, LevelChunk chunk) {
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < COLUMNS_PER_TICK; i++) {
            int dx = level.random.nextInt(16);
            int dz = level.random.nextInt(16);
            degradeColumn(level, startX + dx, startZ + dz);
        }
    }

    public void degradeColumn(ServerLevel level, int x, int z) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, topY, z);

        for (int y = topY; y >= level.getMinBuildHeight(); y--) {
            pos.setY(y);
            BlockState state = level.getBlockState(pos);

            if (!state.isAir()) {
                Block degraded = BlockResolver.getDegradedBlock(state, stageIndex);
                if (degraded != null) {
                    level.setBlock(pos, degraded.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                }
                break;
            }
        }
    }
}
