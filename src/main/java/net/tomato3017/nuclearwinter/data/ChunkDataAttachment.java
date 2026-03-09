package net.tomato3017.nuclearwinter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ChunkDataAttachment(boolean nuked) {
    public static final ChunkDataAttachment DEFAULT = new ChunkDataAttachment(false);

    public static final Codec<ChunkDataAttachment> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("nuked").forGetter(ChunkDataAttachment::nuked)
            ).apply(instance, ChunkDataAttachment::new)
    );
}
