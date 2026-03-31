package net.tomato3017.nuclearwinter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A single radiation resistance entry from a datapack JSON file.
 * Maps a block id or {@code #tag} matcher string to a resistance value.
 * A resistance of {@code 0.0} means radiation passes through the block freely.
 */
public record RadiationResistanceEntry(String match, double resistance) {
    public static final Codec<RadiationResistanceEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("match").forGetter(RadiationResistanceEntry::match),
            Codec.DOUBLE.fieldOf("resistance").forGetter(RadiationResistanceEntry::resistance)
    ).apply(i, RadiationResistanceEntry::new));
}
