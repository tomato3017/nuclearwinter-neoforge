package net.tomato3017.nuclearwinter.data;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NWAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NuclearWinter.MODID);

    public static final Supplier<AttachmentType<WorldDataAttachment>> WORLD_DATA =
            ATTACHMENT_TYPES.register("world_data", () ->
                    AttachmentType.builder(() -> WorldDataAttachment.DEFAULT)
                            .serialize(WorldDataAttachment.CODEC)
                            .build()
            );

    public static final Supplier<AttachmentType<ChunkDataAttachment>> CHUNK_DATA =
            ATTACHMENT_TYPES.register("chunk_data", () ->
                    AttachmentType.builder(() -> ChunkDataAttachment.DEFAULT)
                            .serialize(ChunkDataAttachment.CODEC)
                            .build()
            );

    public static final Supplier<AttachmentType<PlayerDataAttachment>> PLAYER_DATA =
            ATTACHMENT_TYPES.register("player_data", () ->
                    AttachmentType.builder(() -> PlayerDataAttachment.DEFAULT)
                            .serialize(PlayerDataAttachment.CODEC)
                            .build()
            );
}
