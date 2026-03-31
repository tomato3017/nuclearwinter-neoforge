package net.tomato3017.nuclearwinter.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Marker effect applied to players on respawn to grant full radiation immunity for a configurable
 * duration. While active, no radiation pool accumulation, mob effects, or heal reduction occurs.
 * Presence is checked via {@code player.hasEffect(NWMobEffects.RADIATION_IMMUNITY)}.
 */
public class RadiationImmunityEffect extends MobEffect {
    public RadiationImmunityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00BFFF);
    }
}
