package net.tomato3017.nuclearwinter.radiation;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;

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
