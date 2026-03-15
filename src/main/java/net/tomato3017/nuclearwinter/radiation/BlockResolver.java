package net.tomato3017.nuclearwinter.radiation;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.block.NWBlocks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps blocks and block tags to radiation resistance values loaded from {@link net.tomato3017.nuclearwinter.Config}.
 * A higher resistance value means the block attenuates more radiation per layer in the raycast.
 * Call {@link #init()} on server start; use {@link #registerBlockOverride} to add mod-specific entries afterward.
 */
public class BlockResolver {
    private static final Map<TagKey<Block>, Double> TAG_RESISTANCE_MAP = new LinkedHashMap<>();
    private static final Map<Block, Double> BLOCK_OVERRIDES = new LinkedHashMap<>();
    private static double defaultResistance = 1.0;

    private static final Map<TagKey<Block>, Block> STAGE2_TAG_DEGRADATION = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, Block> STAGE4_TAG_DEGRADATION = new LinkedHashMap<>();
    private static final Map<Block, Block> STAGE2_BLOCK_DEGRADATION = new LinkedHashMap<>();
    private static final Map<Block, Block> STAGE4_BLOCK_DEGRADATION = new LinkedHashMap<>();

    //TODO make this work with custom tagged blocks
    public static void init() {
        TAG_RESISTANCE_MAP.clear();
        BLOCK_OVERRIDES.clear();

        defaultResistance = Config.RESISTANCE_STONE.get();

        TAG_RESISTANCE_MAP.put(BlockTags.DIRT, Config.RESISTANCE_DIRT.get());
        TAG_RESISTANCE_MAP.put(BlockTags.LOGS, Config.RESISTANCE_WOOD.get());
        TAG_RESISTANCE_MAP.put(BlockTags.PLANKS, Config.RESISTANCE_WOOD.get());
        TAG_RESISTANCE_MAP.put(BlockTags.WOODEN_FENCES, Config.RESISTANCE_WOOD.get());
        TAG_RESISTANCE_MAP.put(BlockTags.WOODEN_DOORS, Config.RESISTANCE_WOOD.get());
        TAG_RESISTANCE_MAP.put(BlockTags.WOODEN_SLABS, Config.RESISTANCE_WOOD.get());
        TAG_RESISTANCE_MAP.put(BlockTags.WOODEN_STAIRS, Config.RESISTANCE_WOOD.get());
        TAG_RESISTANCE_MAP.put(BlockTags.STONE_ORE_REPLACEABLES, Config.RESISTANCE_STONE.get());
        TAG_RESISTANCE_MAP.put(BlockTags.DEEPSLATE_ORE_REPLACEABLES, Config.RESISTANCE_DEEPSLATE.get());

        BLOCK_OVERRIDES.put(Blocks.OBSIDIAN, Config.RESISTANCE_DEEPSLATE.get());
        BLOCK_OVERRIDES.put(Blocks.CRYING_OBSIDIAN, Config.RESISTANCE_DEEPSLATE.get());
        BLOCK_OVERRIDES.put(Blocks.IRON_BLOCK, Config.RESISTANCE_IRON.get());
        BLOCK_OVERRIDES.put(Blocks.WATER, Config.RESISTANCE_WATER.get());
        BLOCK_OVERRIDES.put(Blocks.GRAVEL, Config.RESISTANCE_DIRT.get());

        NuclearWinter.LOGGER.info("BlockResolver initialized with {} tag entries and {} block overrides",
                TAG_RESISTANCE_MAP.size(), BLOCK_OVERRIDES.size());

        initDegradationMaps();
    }

    private static void initDegradationMaps() {
        STAGE2_TAG_DEGRADATION.clear();
        STAGE4_TAG_DEGRADATION.clear();
        STAGE2_BLOCK_DEGRADATION.clear();
        STAGE4_BLOCK_DEGRADATION.clear();

        // Grass-type -> Dead Grass (Stage 2) -> Wasteland Dust (Stage 4)
        STAGE2_BLOCK_DEGRADATION.put(Blocks.GRASS_BLOCK, NWBlocks.DEAD_GRASS.get());
        STAGE2_BLOCK_DEGRADATION.put(Blocks.MYCELIUM, NWBlocks.DEAD_GRASS.get());
        STAGE2_BLOCK_DEGRADATION.put(Blocks.PODZOL, NWBlocks.DEAD_GRASS.get());
        STAGE4_BLOCK_DEGRADATION.put(Blocks.GRASS_BLOCK, NWBlocks.WASTELAND_DUST.get());
        STAGE4_BLOCK_DEGRADATION.put(Blocks.MYCELIUM, NWBlocks.WASTELAND_DUST.get());
        STAGE4_BLOCK_DEGRADATION.put(Blocks.PODZOL, NWBlocks.WASTELAND_DUST.get());
        STAGE4_BLOCK_DEGRADATION.put(NWBlocks.DEAD_GRASS.get(), NWBlocks.WASTELAND_DUST.get());

        // Dirt-type -> Parched Dirt (Stage 2) -> Wasteland Dust (Stage 4)
        STAGE2_TAG_DEGRADATION.put(BlockTags.DIRT, NWBlocks.PARCHED_DIRT.get());
        STAGE4_TAG_DEGRADATION.put(BlockTags.DIRT, NWBlocks.WASTELAND_DUST.get());
        STAGE4_BLOCK_DEGRADATION.put(NWBlocks.PARCHED_DIRT.get(), NWBlocks.WASTELAND_DUST.get());

        // Stone-type -> Cracked Stone (Stage 2) -> Wasteland Rubble (Stage 4)
        STAGE2_BLOCK_DEGRADATION.put(Blocks.STONE, NWBlocks.CRACKED_STONE.get());
        STAGE2_BLOCK_DEGRADATION.put(Blocks.COBBLESTONE, NWBlocks.CRACKED_STONE.get());
        STAGE2_BLOCK_DEGRADATION.put(Blocks.ANDESITE, NWBlocks.CRACKED_STONE.get());
        STAGE2_BLOCK_DEGRADATION.put(Blocks.GRANITE, NWBlocks.CRACKED_STONE.get());
        STAGE2_BLOCK_DEGRADATION.put(Blocks.DIORITE, NWBlocks.CRACKED_STONE.get());
        STAGE4_BLOCK_DEGRADATION.put(Blocks.STONE, NWBlocks.WASTELAND_RUBBLE.get());
        STAGE4_BLOCK_DEGRADATION.put(Blocks.COBBLESTONE, NWBlocks.WASTELAND_RUBBLE.get());
        STAGE4_BLOCK_DEGRADATION.put(Blocks.ANDESITE, NWBlocks.WASTELAND_RUBBLE.get());
        STAGE4_BLOCK_DEGRADATION.put(Blocks.GRANITE, NWBlocks.WASTELAND_RUBBLE.get());
        STAGE4_BLOCK_DEGRADATION.put(Blocks.DIORITE, NWBlocks.WASTELAND_RUBBLE.get());
        STAGE4_BLOCK_DEGRADATION.put(NWBlocks.CRACKED_STONE.get(), NWBlocks.WASTELAND_RUBBLE.get());

        // Log-type -> Deadwood (Stage 2), no further degradation
        STAGE2_TAG_DEGRADATION.put(BlockTags.LOGS, NWBlocks.DEADWOOD.get());

        // Leaf-type -> Dead Leaves (Stage 2) -> Air (Stage 4)
        STAGE2_TAG_DEGRADATION.put(BlockTags.LEAVES, NWBlocks.DEAD_LEAVES.get());
        STAGE4_TAG_DEGRADATION.put(BlockTags.LEAVES, Blocks.AIR);
        STAGE4_BLOCK_DEGRADATION.put(NWBlocks.DEAD_LEAVES.get(), Blocks.AIR);

        // Plank-type -> Ruined Planks (Stage 2), no further degradation
        STAGE2_TAG_DEGRADATION.put(BlockTags.PLANKS, NWBlocks.RUINED_PLANKS.get());
    }

    /**
     * Returns the block that the given state should degrade into at the given stage index,
     * or null if no degradation applies.
     * Stage index 3 = Stage 2 (first degradation tier), stage index 5 = Stage 4 (second tier).
     */
    public static Block getDegradedBlock(BlockState state, int stageIndex) {
        Block block = state.getBlock();

        Map<Block, Block> blockMap;
        Map<TagKey<Block>, Block> tagMap;
        if (stageIndex >= 5) {
            blockMap = STAGE4_BLOCK_DEGRADATION;
            tagMap = STAGE4_TAG_DEGRADATION;
        } else if (stageIndex >= 3) {
            blockMap = STAGE2_BLOCK_DEGRADATION;
            tagMap = STAGE2_TAG_DEGRADATION;
        } else {
            return null;
        }

        Block result = blockMap.get(block);
        if (result != null) return result;

        for (var entry : tagMap.entrySet()) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static void registerBlockOverride(Block block, double resistance) {
        BLOCK_OVERRIDES.put(block, resistance);
    }

    public static boolean canRadiationPassThrough(BlockState state) {
        return state.isAir() ||
                state.is(BlockTags.LEAVES) ||
                !state.canOcclude();
    }

    public static double getResistance(BlockState state) {
        Block block = state.getBlock();

        if (state.isAir()) return 0.0;

        Double override = BLOCK_OVERRIDES.get(block);
        if (override != null) return override;

        for (var entry : TAG_RESISTANCE_MAP.entrySet()) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        if (canRadiationPassThrough(state)) {
            return 0.0;
        }

        return defaultResistance;
    }
}
