package net.tomato3017.nuclearwinter.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Persistent state stored on a dosimeter {@link net.minecraft.world.item.ItemStack}.
 * Tracks how many rads the badge has cumulatively absorbed ({@link #absorbedRads}).
 * Serialized to item NBT; synced to the client so the durability bar renders correctly.
 */
public record DosimeterData(double absorbedRads) {
    public static final DosimeterData DEFAULT = new DosimeterData(0.0);

    public static final Codec<DosimeterData> CODEC = Codec.DOUBLE
            .fieldOf("absorbed_rads")
            .xmap(DosimeterData::new, DosimeterData::absorbedRads)
            .codec();

    public static final StreamCodec<ByteBuf, DosimeterData> STREAM_CODEC =
            ByteBufCodecs.DOUBLE.map(DosimeterData::new, DosimeterData::absorbedRads);

    public boolean isSaturated(double maxRads) {
        return absorbedRads >= maxRads;
    }
}
