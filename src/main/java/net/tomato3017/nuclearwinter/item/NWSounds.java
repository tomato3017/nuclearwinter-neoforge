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

    public static final Supplier<SoundEvent> GEIGER_COUNTER_LOW =
            SOUND_EVENTS.register("geiger_counter_low",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, "geiger_counter_low")));

    public static final Supplier<SoundEvent> GEIGER_COUNTER_MED =
            SOUND_EVENTS.register("geiger_counter_med",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, "geiger_counter_med")));

    public static final Supplier<SoundEvent> GEIGER_COUNTER_HIGH =
            SOUND_EVENTS.register("geiger_counter_high",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, "geiger_counter_high")));
}
