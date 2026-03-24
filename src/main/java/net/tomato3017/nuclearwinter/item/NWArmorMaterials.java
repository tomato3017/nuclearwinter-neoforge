package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * In-memory {@link ArmorMaterial} definitions for hazmat suit tiers.
 */
public class NWArmorMaterials {
    private static final Map<ArmorItem.Type, Integer> HAZMAT_DEFENSE = Util.make(
            new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 1);
                map.put(ArmorItem.Type.CHESTPLATE, 3);
                map.put(ArmorItem.Type.LEGGINGS, 2);
                map.put(ArmorItem.Type.BOOTS, 1);
            });

    public static final Holder<ArmorMaterial> HAZMAT_TIER1 = create("hazmat_tier1",
            HAZMAT_DEFENSE, 9, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f,
            () -> Ingredient.of(net.minecraft.world.item.Items.LEATHER));

    public static final Holder<ArmorMaterial> HAZMAT_TIER2 = create("hazmat_tier2",
            HAZMAT_DEFENSE, 15, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0f, 0.0f,
            () -> Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT));

    public static final Holder<ArmorMaterial> HAZMAT_TIER3 = create("hazmat_tier3",
            HAZMAT_DEFENSE, 25, SoundEvents.ARMOR_EQUIP_IRON, 1.0f, 0.0f,
            () -> Ingredient.of(net.minecraft.world.item.Items.DIAMOND));

    private static Holder<ArmorMaterial> create(String name,
            Map<ArmorItem.Type, Integer> defense, int enchantability,
            Holder<net.minecraft.sounds.SoundEvent> equipSound,
            float toughness, float knockbackResistance,
            Supplier<Ingredient> repairIngredient) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, name);

        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(id));

        return Holder.direct(new ArmorMaterial(defense, enchantability, equipSound, repairIngredient,
                layers, toughness, knockbackResistance));
    }
}
