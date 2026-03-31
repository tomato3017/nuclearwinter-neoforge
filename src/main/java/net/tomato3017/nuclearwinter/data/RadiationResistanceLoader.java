package net.tomato3017.nuclearwinter.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.radiation.BlockResolver;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads radiation resistance values from {@code data/<namespace>/radiation_resistance/*.json} datapacks.
 * Parses entries via {@link RadiationResistanceData} Codecs, resolves block/tag matchers using
 * {@link BlockResolver}, and pushes the merged results to {@link BlockResolver#setRadiationResistance}.
 */
public class RadiationResistanceLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final double FALLBACK_DEFAULT_RESISTANCE = 1.0;

    public RadiationResistanceLoader() {
        super(GSON, "radiation_resistance");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        LinkedHashMap<Block, Double> blockResistances = new LinkedHashMap<>();
        LinkedHashMap<TagKey<Block>, Double> tagResistances = new LinkedHashMap<>();
        double defaultResistance = FALLBACK_DEFAULT_RESISTANCE;
        boolean defaultResistanceSet = false;

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            RadiationResistanceData data = parseFile(fileId, entry.getValue());
            if (data == null) continue;

            if (data.defaultResistance().isPresent()) {
                double value = data.defaultResistance().get();
                if (defaultResistanceSet) {
                    NuclearWinter.LOGGER.warn(
                            "Multiple radiation_resistance files set default_resistance; {} overrides previous value of {}",
                            fileId, defaultResistance);
                }
                defaultResistance = value;
                defaultResistanceSet = true;
            }

            String sourceName = fileId.toString();
            for (RadiationResistanceEntry resistanceEntry : data.entries()) {
                double resistance = resistanceEntry.resistance();
                if (resistance < 0.0) {
                    NuclearWinter.LOGGER.warn("Skipping entry in {}: resistance {} is negative (match={})",
                            sourceName, resistance, resistanceEntry.match());
                    continue;
                }

                BlockResolver.Matcher matcher = BlockResolver.parseMatcher(resistanceEntry.match(), sourceName);
                if (matcher == null) continue;

                if (matcher instanceof BlockResolver.BlockMatcher blockMatcher) {
                    blockResistances.put(blockMatcher.block(), resistance);
                } else if (matcher instanceof BlockResolver.TagMatcher tagMatcher) {
                    tagResistances.put(tagMatcher.tag(), resistance);
                }
            }
        }

        BlockResolver.setRadiationResistance(blockResistances, tagResistances, defaultResistance);

        NuclearWinter.LOGGER.info("Loaded radiation resistance: {} block entries, {} tag entries, default={}",
                blockResistances.size(), tagResistances.size(), defaultResistance);
    }

    private RadiationResistanceData parseFile(ResourceLocation fileId, JsonElement json) {
        DataResult<RadiationResistanceData> result = RadiationResistanceData.CODEC.parse(JsonOps.INSTANCE, json);
        if (result.error().isPresent()) {
            NuclearWinter.LOGGER.error("Failed to parse radiation resistance file {}: {}",
                    fileId, result.error().get().message());
            return null;
        }
        return result.result().orElse(null);
    }
}
