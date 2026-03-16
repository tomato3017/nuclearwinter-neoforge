package net.tomato3017.nuclearwinter.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
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
import java.lang.reflect.Method;

/**
 * Processes loaded chunks to degrade surface blocks based on the current stage index.
 * At Stage 4, it performs a one-time full-column nuke pass and marks the chunk via
 * {@link ChunkDataAttachment} so it is never re-processed on reload.
 */
public final class ChunkProcessor {
    private static final Method CHUNK_MAP_GET_CHUNKS_METHOD = resolveChunkMapGetChunksMethod();

    private final int stageIndex;
    private final boolean chunkNukingEnabled;
    private static final int COLUMNS_PER_TICK = 16;

    private final Set<ChunkPos> loadedChunks = new HashSet<>();
    private final Queue<ChunkPos> chunks = new LinkedBlockingQueue<>();

    public ChunkProcessor(int stageIndex, ServerLevel level, boolean chunkNukingEnabled) {
        this.stageIndex = stageIndex;
        this.chunkNukingEnabled = chunkNukingEnabled;

        for (ChunkHolder chunkHolder : getLoadedChunkHolders(level)) {
            LevelChunk tickingChunk = chunkHolder.getTickingChunk();
            if (tickingChunk == null) continue;

            ChunkPos chunkPos = tickingChunk.getPos();
            loadedChunks.add(chunkPos);
            chunks.add(chunkPos);
        }
    }

    private static Method resolveChunkMapGetChunksMethod() {
        try {
            Method method = ChunkMap.class.getDeclaredMethod("getChunks");
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to access ChunkMap#getChunks", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Iterable<ChunkHolder> getLoadedChunkHolders(ServerLevel level) {
        try {
            return (Iterable<ChunkHolder>) CHUNK_MAP_GET_CHUNKS_METHOD.invoke(level.getChunkSource().chunkMap);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to enumerate loaded chunks for chunk processing", exception);
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

            if (state.isAir()) {
                continue;
            }

            if (Config.CHUNK_PROCESSING_STOP_AT_FLUIDS.get() && !state.getFluidState().isEmpty()) {
                break;
            }

            BlockResolver.DegradationResult degradation = BlockResolver.getDegradationResult(state, stageIndex);
            if (degradation != null) {
                if (state.getBlock() != degradation.replacement()) {
                    level.setBlock(pos, degradation.replacement().defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                }

                if (!degradation.passthrough()) {
                    break;
                }

                continue;
            }

            break;
        }
    }
}
