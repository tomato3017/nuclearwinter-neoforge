package net.tomato3017.nuclearwinter.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.tomato3017.nuclearwinter.NuclearWinter;

public final class NWEntityTypeTags {
    public static final TagKey<EntityType<?>> SURFACE_RADIATION_IMMUNE = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, "surface_radiation_immune"));

    private NWEntityTypeTags() {
    }
}
