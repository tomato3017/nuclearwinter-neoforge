package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.block.NWBlocks;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NWItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearWinter.MODID);

    // --- Block items for degradation blocks ---
    public static final DeferredItem<BlockItem> DEAD_GRASS = ITEMS.registerSimpleBlockItem("dead_grass", NWBlocks.DEAD_GRASS);
    public static final DeferredItem<BlockItem> DEAD_LEAVES = ITEMS.registerSimpleBlockItem("dead_leaves", NWBlocks.DEAD_LEAVES);
    public static final DeferredItem<BlockItem> PARCHED_DIRT = ITEMS.registerSimpleBlockItem("parched_dirt", NWBlocks.PARCHED_DIRT);
    public static final DeferredItem<BlockItem> WASTELAND_DUST = ITEMS.registerSimpleBlockItem("wasteland_dust", NWBlocks.WASTELAND_DUST);
    public static final DeferredItem<BlockItem> CRACKED_STONE = ITEMS.registerSimpleBlockItem("cracked_stone", NWBlocks.CRACKED_STONE);
    public static final DeferredItem<BlockItem> WASTELAND_RUBBLE = ITEMS.registerSimpleBlockItem("wasteland_rubble", NWBlocks.WASTELAND_RUBBLE);
    public static final DeferredItem<BlockItem> DEADWOOD = ITEMS.registerSimpleBlockItem("deadwood", NWBlocks.DEADWOOD);
    public static final DeferredItem<BlockItem> RUINED_PLANKS = ITEMS.registerSimpleBlockItem("ruined_planks", NWBlocks.RUINED_PLANKS);

    // --- Block items for shielding blocks ---
    public static final DeferredItem<BlockItem> LEAD_BLOCK = ITEMS.registerSimpleBlockItem("lead_block", NWBlocks.LEAD_BLOCK);
    public static final DeferredItem<BlockItem> REINFORCED_CONCRETE = ITEMS.registerSimpleBlockItem("reinforced_concrete", NWBlocks.REINFORCED_CONCRETE);
}
