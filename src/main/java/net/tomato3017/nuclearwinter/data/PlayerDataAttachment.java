package net.tomato3017.nuclearwinter.data;

import com.mojang.serialization.Codec;

/**
 * Per-player radiation state persisted via {@link NWAttachmentTypes#PLAYER_DATA}.
 * Only {@link #radiationPool} is written to disk; {@link #lastReceivedRads} is transient
 * (resets to 0.0 on load) and is written each tick by {@link net.tomato3017.nuclearwinter.radiation.PlayerRadHandler}.
 */
public record PlayerDataAttachment(double radiationPool, double lastReceivedRads) {
    public static final PlayerDataAttachment DEFAULT = new PlayerDataAttachment(0.0, 0.0);

    public static final Codec<PlayerDataAttachment> CODEC = Codec.DOUBLE
            .fieldOf("radiationPool")
            .xmap(
                    pool -> new PlayerDataAttachment(pool, 0.0),
                    PlayerDataAttachment::radiationPool
            )
            .codec();

    public PlayerDataAttachment withRadiationPool(double pool) {
        return new PlayerDataAttachment(pool, this.lastReceivedRads);
    }

    public PlayerDataAttachment withLastReceivedRads(double rads) {
        return new PlayerDataAttachment(this.radiationPool, rads);
    }
}
