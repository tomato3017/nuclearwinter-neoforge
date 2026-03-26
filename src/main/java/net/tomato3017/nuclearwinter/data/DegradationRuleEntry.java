package net.tomato3017.nuclearwinter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A single degradation rule entry from a datapack JSON file.
 * Maps a block or tag matcher string to an optional replacement block id with optional passthrough and probability.
 * When {@code replacement} is absent, the rule is a no-op passthrough and requires {@code passthrough: true}.
 */
public record DegradationRuleEntry(
        String match,
        String replacement,
        boolean passthrough,
        double probability
) {
    public static final Codec<DegradationRuleEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("match").forGetter(DegradationRuleEntry::match),
            Codec.STRING.optionalFieldOf("replacement", "").forGetter(DegradationRuleEntry::replacement),
            Codec.BOOL.optionalFieldOf("passthrough", false).forGetter(DegradationRuleEntry::passthrough),
            Codec.DOUBLE.optionalFieldOf("probability", 1.0).forGetter(DegradationRuleEntry::probability)
    ).apply(i, DegradationRuleEntry::new));
}
