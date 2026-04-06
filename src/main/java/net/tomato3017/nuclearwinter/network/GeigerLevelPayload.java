package net.tomato3017.nuclearwinter.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.tomato3017.nuclearwinter.NuclearWinter;

/**
 * Server-to-client packet carrying the current {@link GeigerLevel} for the held Geiger counter.
 * Sent whenever the level changes or the player deselects the item.
 * The client-side handler is registered in {@code NuclearWinter#onRegisterPayloadHandlers}.
 */
public record GeigerLevelPayload(GeigerLevel level) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GeigerLevelPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, "geiger_level"));

    public static final StreamCodec<ByteBuf, GeigerLevelPayload> STREAM_CODEC =
            ByteBufCodecs.BYTE.map(
                    b -> new GeigerLevelPayload(GeigerLevel.values()[b & 0xFF]),
                    p -> (byte) p.level().ordinal());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
