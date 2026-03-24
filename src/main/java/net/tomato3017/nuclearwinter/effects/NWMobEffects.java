package net.tomato3017.nuclearwinter.effects;

import net.tomato3017.nuclearwinter.NuclearWinter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration for mod mob effects.
 */
public class NWMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, NuclearWinter.MODID);

    public static final DeferredHolder<MobEffect, RadAwayEffect> RADAWAY =
            MOB_EFFECTS.register("radaway", RadAwayEffect::new);
}
