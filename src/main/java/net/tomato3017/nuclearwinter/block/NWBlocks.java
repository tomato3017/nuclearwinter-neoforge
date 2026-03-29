package net.tomato3017.nuclearwinter.block;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NWBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NuclearWinter.MODID);

    // --- Degradation blocks ---

    public static final DeferredBlock<Block> DEAD_GRASS = BLOCKS.registerSimpleBlock("dead_grass",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.6f).sound(SoundType.GRASS));

    public static final DeferredBlock<Block> DEAD_LEAVES = BLOCKS.registerSimpleBlock("dead_leaves",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.2f).sound(SoundType.GRASS)
                    .noOcclusion());

    public static final DeferredBlock<Block> WASTELAND_DIRT = BLOCKS.registerSimpleBlock("wasteland_dirt",
            BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5f).sound(SoundType.GRAVEL));

    public static final DeferredBlock<Block> WASTELAND_RUBBLE = BLOCKS.registerSimpleBlock("wasteland_rubble",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5f, 6.0f).sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<RotatedPillarBlock> DEADWOOD = BLOCKS.register("deadwood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(2.0f).sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> RUINED_PLANKS = BLOCKS.registerSimpleBlock("ruined_planks",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD));

    // --- Shielding blocks ---

    public static final DeferredBlock<Block> LEAD_BLOCK = BLOCKS.registerSimpleBlock("lead_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(5.0f, 6.0f)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> REINFORCED_CONCRETE = BLOCKS.registerSimpleBlock("reinforced_concrete",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0f, 12.0f)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops());

    // --- Ore blocks ---

    public static final DeferredBlock<Block> LEAD_ORE = BLOCKS.registerSimpleBlock("lead_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 3.0f)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> DEEPSLATE_LEAD_ORE = BLOCKS.registerSimpleBlock("deepslate_lead_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(4.5f, 3.0f)
                    .sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());
}
