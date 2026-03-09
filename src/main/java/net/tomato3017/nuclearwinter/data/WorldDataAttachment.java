package net.tomato3017.nuclearwinter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WorldDataAttachment(int stageIndex, long stageStartTick) {
    public static final WorldDataAttachment DEFAULT = new WorldDataAttachment(0, 0L);

    public static final Codec<WorldDataAttachment> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("stageIndex").forGetter(WorldDataAttachment::stageIndex),
                    Codec.LONG.fieldOf("stageStartTick").forGetter(WorldDataAttachment::stageStartTick)
            ).apply(instance, WorldDataAttachment::new)
    );

    public WorldDataAttachment withStageIndex(int stageIndex) {
        return new WorldDataAttachment(stageIndex, this.stageStartTick);
    }

    public WorldDataAttachment withStageStartTick(long stageStartTick) {
        return new WorldDataAttachment(this.stageIndex, stageStartTick);
    }
}
