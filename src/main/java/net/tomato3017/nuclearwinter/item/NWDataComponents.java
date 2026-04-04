package net.tomato3017.nuclearwinter.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tomato3017.nuclearwinter.NuclearWinter;

/**
 * Registry for custom item {@link DataComponentType}s used to store per-stack state.
 * Register via {@link #DATA_COMPONENTS} on the mod event bus in {@link NuclearWinter}.
 */
public class NWDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NuclearWinter.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DosimeterData>> DOSIMETER_DATA =
            DATA_COMPONENTS.register("dosimeter_data", () ->
                    DataComponentType.<DosimeterData>builder()
                            .persistent(DosimeterData.CODEC)
                            .networkSynchronized(DosimeterData.STREAM_CODEC)
                            .build()
            );
}
