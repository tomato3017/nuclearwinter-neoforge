package net.tomato3017.nuclearwinter.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tomato3017.nuclearwinter.NuclearWinter;

import java.util.function.Supplier;

/**
 * Registers custom {@link SoundEvent} entries for NuclearWinter.
 * Call {@link #SOUND_EVENTS}{@code .register(modEventBus)} in the mod constructor.
 */
public class NWSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, NuclearWinter.MODID);

    public static final Supplier<SoundEvent> GEIGER_CLICK =
            SOUND_EVENTS.register("geiger_click",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, "geiger_click")));
}
