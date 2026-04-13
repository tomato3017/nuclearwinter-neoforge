package net.tomato3017.nuclearwinter.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tomato3017.nuclearwinter.NuclearWinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Registers custom {@link SoundEvent} entries for NuclearWinter.
 * Call {@link #SOUND_EVENTS}{@code .register(modEventBus)} in the mod constructor.
 *
 * <p>Geiger counter sounds are organized as {@link #GEIGER_VARIANTS}: a list of 6 levels,
 * each containing 3 variant suppliers. Access via {@code GEIGER_VARIANTS.get(levelIndex).get(variantIndex)}.
 */
public class NWSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, NuclearWinter.MODID);

    /** 6 levels × 3 variants. Index as {@code GEIGER_VARIANTS.get(level).get(variant)}, both 0-based. */
    public static final List<List<Supplier<SoundEvent>>> GEIGER_VARIANTS = registerGeigerVariants();

    @SuppressWarnings("unchecked")
    private static List<List<Supplier<SoundEvent>>> registerGeigerVariants() {
        List<List<Supplier<SoundEvent>>> levels = new ArrayList<>(6);
        for (int level = 1; level <= 6; level++) {
            List<Supplier<SoundEvent>> variants = new ArrayList<>(3);
            for (int variant = 1; variant <= 3; variant++) {
                String name = "geiger_level_" + level + "_var" + variant;
                final ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, name);
                Supplier<SoundEvent> supplier = SOUND_EVENTS.register(name,
                        () -> SoundEvent.createVariableRangeEvent(loc));
                variants.add(supplier);
            }
            levels.add(Collections.unmodifiableList(variants));
        }
        return Collections.unmodifiableList(levels);
    }
}
