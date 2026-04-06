package net.tomato3017.nuclearwinter.network;

import net.minecraft.sounds.SoundEvent;
import net.tomato3017.nuclearwinter.item.NWSounds;

import java.util.function.Supplier;

/**
 * Represents the radiation intensity level as seen by a held Geiger counter.
 * Determines which looping ambient track the client plays.
 */
public enum GeigerLevel {
    NONE(null),
    LOW(NWSounds.GEIGER_COUNTER_LOW),
    MED(NWSounds.GEIGER_COUNTER_MED),
    HIGH(NWSounds.GEIGER_COUNTER_HIGH);

    private final Supplier<SoundEvent> soundSupplier;

    GeigerLevel(Supplier<SoundEvent> soundSupplier) {
        this.soundSupplier = soundSupplier;
    }

    /**
     * Returns the sound event for this level, or {@code null} for {@link #NONE}.
     */
    public SoundEvent getSoundEvent() {
        return soundSupplier == null ? null : soundSupplier.get();
    }

    /**
     * Maps a rads/sec value to the appropriate {@link GeigerLevel} using caller-supplied thresholds.
     * Both thresholds refer to raw sky emission before suit protection is applied.
     */
    public static GeigerLevel fromRadsPerSec(double radsPerSec, double thresholdMed, double thresholdHigh) {
        if (radsPerSec <= 0) return NONE;
        if (radsPerSec >= thresholdHigh) return HIGH;
        if (radsPerSec >= thresholdMed) return MED;
        return LOW;
    }
}
