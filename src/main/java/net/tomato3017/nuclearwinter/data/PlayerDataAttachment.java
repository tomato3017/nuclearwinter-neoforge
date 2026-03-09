package net.tomato3017.nuclearwinter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PlayerDataAttachment(double radiationPool) {
    public static final PlayerDataAttachment DEFAULT = new PlayerDataAttachment(0.0);

    public static final Codec<PlayerDataAttachment> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("radiationPool").forGetter(PlayerDataAttachment::radiationPool)
            ).apply(instance, PlayerDataAttachment::new)
    );

    public PlayerDataAttachment withRadiationPool(double radiationPool) {
        return new PlayerDataAttachment(radiationPool);
    }
}
