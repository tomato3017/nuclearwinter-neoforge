package net.tomato3017.nuclearwinter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Top-level structure of a {@code degradation_rules/<stage>.json} file.
 * Contains an inheritance flag and a list of rule entries for that stage.
 */
public record DegradationStageData(
        boolean inherit,
        List<DegradationRuleEntry> rules
) {
    public static final Codec<DegradationStageData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("inherit", true).forGetter(DegradationStageData::inherit),
            DegradationRuleEntry.CODEC.listOf().fieldOf("rules").forGetter(DegradationStageData::rules)
    ).apply(i, DegradationStageData::new));
}
