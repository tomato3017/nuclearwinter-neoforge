package net.tomato3017.nuclearwinter.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * A looping, player-relative sound instance for the Geiger counter ambient track.
 * Lifecycle is managed externally by {@link GeigerSoundHandler}; {@link #tick()} is a no-op.
 */
public class GeigerLoopSound extends AbstractTickableSoundInstance {

    public GeigerLoopSound(SoundEvent soundEvent) {
        super(soundEvent, SoundSource.PLAYERS, RandomSource.create());
        this.looping = true;
        this.relative = true;
        this.volume = 1.0f;
        this.pitch = 1.0f;
    }

    @Override
    public void tick() {
        // Lifecycle is managed by GeigerSoundHandler; nothing to do per-tick.
    }
}
