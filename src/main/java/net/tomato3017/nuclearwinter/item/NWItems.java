package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.block.NWBlocks;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NWItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearWinter.MODID);

    // --- Block items for degradation blocks ---
    public static final DeferredItem<BlockItem> DEAD_GRASS = ITEMS.registerSimpleBlockItem("dead_grass", NWBlocks.DEAD_GRASS);
    public static final DeferredItem<BlockItem> DEAD_LEAVES = ITEMS.registerSimpleBlockItem("dead_leaves", NWBlocks.DEAD_LEAVES);
    public static final DeferredItem<BlockItem> WASTELAND_DIRT = ITEMS.registerSimpleBlockItem("wasteland_dirt", NWBlocks.WASTELAND_DIRT);
    public static final DeferredItem<BlockItem> WASTELAND_RUBBLE = ITEMS.registerSimpleBlockItem("wasteland_rubble", NWBlocks.WASTELAND_RUBBLE);
    public static final DeferredItem<BlockItem> DEADWOOD = ITEMS.registerSimpleBlockItem("deadwood", NWBlocks.DEADWOOD);
    public static final DeferredItem<BlockItem> RUINED_PLANKS = ITEMS.registerSimpleBlockItem("ruined_planks", NWBlocks.RUINED_PLANKS);

    // --- Block items for shielding blocks ---
    public static final DeferredItem<BlockItem> LEAD_BLOCK = ITEMS.registerSimpleBlockItem("lead_block", NWBlocks.LEAD_BLOCK);
    public static final DeferredItem<BlockItem> REINFORCED_CONCRETE = ITEMS.registerSimpleBlockItem("reinforced_concrete", NWBlocks.REINFORCED_CONCRETE);

    public static final DeferredItem<Item> RADAWAY = ITEMS.register("radaway",
            () -> new RadAwayItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter",
            () -> new GeigerCounterItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> DOSIMETER = ITEMS.register("dosimeter",
            () -> new DosimeterItem(new Item.Properties().stacksTo(1)));

    // --- Hazmat Tier 1 ---
    public static final DeferredItem<ArmorItem> HAZMAT_T1_HELMET = ITEMS.register("hazmat_t1_helmet",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER1, ArmorItem.Type.HELMET, new Item.Properties(), HazmatTier.TIER_1));
    public static final DeferredItem<ArmorItem> HAZMAT_T1_CHESTPLATE = ITEMS.register("hazmat_t1_chestplate",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER1, ArmorItem.Type.CHESTPLATE, new Item.Properties(), HazmatTier.TIER_1));
    public static final DeferredItem<ArmorItem> HAZMAT_T1_LEGGINGS = ITEMS.register("hazmat_t1_leggings",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER1, ArmorItem.Type.LEGGINGS, new Item.Properties(), HazmatTier.TIER_1));
    public static final DeferredItem<ArmorItem> HAZMAT_T1_BOOTS = ITEMS.register("hazmat_t1_boots",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER1, ArmorItem.Type.BOOTS, new Item.Properties(), HazmatTier.TIER_1));

    // --- Hazmat Tier 2 ---
    public static final DeferredItem<ArmorItem> HAZMAT_T2_HELMET = ITEMS.register("hazmat_t2_helmet",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER2, ArmorItem.Type.HELMET, new Item.Properties(), HazmatTier.TIER_2));
    public static final DeferredItem<ArmorItem> HAZMAT_T2_CHESTPLATE = ITEMS.register("hazmat_t2_chestplate",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER2, ArmorItem.Type.CHESTPLATE, new Item.Properties(), HazmatTier.TIER_2));
    public static final DeferredItem<ArmorItem> HAZMAT_T2_LEGGINGS = ITEMS.register("hazmat_t2_leggings",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER2, ArmorItem.Type.LEGGINGS, new Item.Properties(), HazmatTier.TIER_2));
    public static final DeferredItem<ArmorItem> HAZMAT_T2_BOOTS = ITEMS.register("hazmat_t2_boots",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER2, ArmorItem.Type.BOOTS, new Item.Properties(), HazmatTier.TIER_2));

    // --- Hazmat Tier 3 ---
    public static final DeferredItem<ArmorItem> HAZMAT_T3_HELMET = ITEMS.register("hazmat_t3_helmet",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER3, ArmorItem.Type.HELMET, new Item.Properties(), HazmatTier.TIER_3));
    public static final DeferredItem<ArmorItem> HAZMAT_T3_CHESTPLATE = ITEMS.register("hazmat_t3_chestplate",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER3, ArmorItem.Type.CHESTPLATE, new Item.Properties(), HazmatTier.TIER_3));
    public static final DeferredItem<ArmorItem> HAZMAT_T3_LEGGINGS = ITEMS.register("hazmat_t3_leggings",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER3, ArmorItem.Type.LEGGINGS, new Item.Properties(), HazmatTier.TIER_3));
    public static final DeferredItem<ArmorItem> HAZMAT_T3_BOOTS = ITEMS.register("hazmat_t3_boots",
            () -> new HazmatSuitItem(NWArmorMaterials.HAZMAT_TIER3, ArmorItem.Type.BOOTS, new Item.Properties(), HazmatTier.TIER_3));
}
