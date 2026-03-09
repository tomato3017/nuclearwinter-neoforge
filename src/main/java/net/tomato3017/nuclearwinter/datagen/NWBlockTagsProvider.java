package net.tomato3017.nuclearwinter.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.block.NWBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NWBlockTagsProvider extends BlockTagsProvider {

    public NWBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, NuclearWinter.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(NWBlocks.DEADWOOD.get())
                .add(NWBlocks.RUINED_PLANKS.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(NWBlocks.CRACKED_STONE.get())
                .add(NWBlocks.WASTELAND_RUBBLE.get())
                .add(NWBlocks.LEAD_BLOCK.get())
                .add(NWBlocks.REINFORCED_CONCRETE.get());
    }
}
