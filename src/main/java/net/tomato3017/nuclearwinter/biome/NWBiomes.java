package net.tomato3017.nuclearwinter.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.tomato3017.nuclearwinter.NuclearWinter;

/**
 * Registry keys for NuclearWinter's custom biomes.
 * These keys reference data-driven biome JSONs under {@code data/nuclearwinter/worldgen/biome/}.
 */
public final class NWBiomes {
    public static final ResourceKey<Biome> WASTELAND = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, "wasteland")
    );

    private NWBiomes() {}
}
