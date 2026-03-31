package net.tomato3017.nuclearwinter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Top-level structure of a {@code radiation_resistance/<name>.json} file.
 * Contains an optional default resistance fallback and a list of per-block/tag entries.
 * Multiple files across namespaces are merged; only one file should set {@code default_resistance}.
 */
public record RadiationResistanceData(
        Optional<Double> defaultResistance,
        List<RadiationResistanceEntry> entries
) {
    public static final Codec<RadiationResistanceData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("default_resistance").forGetter(RadiationResistanceData::defaultResistance),
            RadiationResistanceEntry.CODEC.listOf().fieldOf("entries").forGetter(RadiationResistanceData::entries)
    ).apply(i, RadiationResistanceData::new));
}
