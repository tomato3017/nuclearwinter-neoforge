package net.tomato3017.nuclearwinter.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.network.GeigerLevel;

import java.util.EnumMap;
import java.util.function.Supplier;

/**
 * Registers custom {@link SoundEvent} entries for NuclearWinter.
 * Call {@link #SOUND_EVENTS}{@code .register(modEventBus)} in the mod constructor.
 */
public class NWSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, NuclearWinter.MODID);

    private static final EnumMap<GeigerLevel, GeigerSoundSet> GEIGER_SOUNDS = registerGeigerSounds();

    public static SoundEvent getGeigerSound(GeigerLevel level, int variantIndex) {
        GeigerSoundSet sounds = GEIGER_SOUNDS.get(level);
        if (sounds == null) {
            throw new IllegalArgumentException("No Geiger sounds registered for level: " + level);
        }
        return sounds.get(variantIndex);
    }

    private static EnumMap<GeigerLevel, GeigerSoundSet> registerGeigerSounds() {
        EnumMap<GeigerLevel, GeigerSoundSet> soundsByLevel = new EnumMap<>(GeigerLevel.class);
        for (GeigerLevel level : GeigerLevel.values()) {
            if (level == GeigerLevel.NONE) {
                continue;
            }

            int soundLevel = level.getLevelIndex() + 1;
            soundsByLevel.put(level, new GeigerSoundSet(
                    registerGeigerVariant(soundLevel, 1),
                    registerGeigerVariant(soundLevel, 2),
                    registerGeigerVariant(soundLevel, 3)
            ));
        }
        return soundsByLevel;
    }

    private static Supplier<SoundEvent> registerGeigerVariant(int level, int variant) {
        String name = "geiger_level_" + level + "_var" + variant;
        final ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(NuclearWinter.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(loc));
    }

    private record GeigerSoundSet(
            Supplier<SoundEvent> variant1,
            Supplier<SoundEvent> variant2,
            Supplier<SoundEvent> variant3
    ) {
        private SoundEvent get(int variantIndex) {
            return switch (variantIndex) {
                case 0 -> variant1.get();
                case 1 -> variant2.get();
                case 2 -> variant3.get();
                default -> throw new IllegalArgumentException("Invalid Geiger variant index: " + variantIndex);
            };
        }
    }
}
