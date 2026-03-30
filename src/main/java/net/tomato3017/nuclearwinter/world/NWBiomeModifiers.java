package net.tomato3017.nuclearwinter.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;

/**
 * Datapack biome modifier serializers for NuclearWinter. {@link LeadOreBiomeModifier} mirrors
 * NeoForge's {@code neoforge:add_features} but skips registration when {@link Config#GENERATE_LEAD_ORE} is false.
 */
public final class NWBiomeModifiers {
    private NWBiomeModifiers() {}

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, NuclearWinter.MODID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<LeadOreBiomeModifier>> LEAD_ORE_TYPE =
            SERIALIZERS.register("lead_ore", () -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Biome.LIST_CODEC.fieldOf("biomes").forGetter(LeadOreBiomeModifier::biomes),
                            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(LeadOreBiomeModifier::features),
                            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(LeadOreBiomeModifier::step))
                    .apply(instance, LeadOreBiomeModifier::new)));

    public record LeadOreBiomeModifier(
            HolderSet<Biome> biomes,
            HolderSet<PlacedFeature> features,
            GenerationStep.Decoration step) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (!Config.GENERATE_LEAD_ORE.get()) {
                return;
            }
            if (phase == Phase.ADD && this.biomes.contains(biome)) {
                BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
                this.features.forEach(holder -> generationSettings.addFeature(this.step, holder));
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return LEAD_ORE_TYPE.get();
        }
    }
}
