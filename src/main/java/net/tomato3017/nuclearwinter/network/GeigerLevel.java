package net.tomato3017.nuclearwinter.network;

import net.minecraft.sounds.SoundEvent;
import net.tomato3017.nuclearwinter.item.NWSounds;

/**
 * Represents the radiation intensity level as seen by a held Geiger counter.
 * Determines which looping ambient track the client plays.
 */
public enum GeigerLevel {
    NONE(-1),
    LEVEL_1(0),
    LEVEL_2(1),
    LEVEL_3(2),
    LEVEL_4(3),
    LEVEL_5(4),
    LEVEL_6(5);

    /**
     * 0-based Geiger sound level, or -1 for {@link #NONE}.
     */
    private final int levelIndex;

    GeigerLevel(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    /**
     * Returns the sound event for the given variant of this level.
     * Must not be called on {@link #NONE}.
     *
     * @param variantIndex 0-based variant index (0..2)
     */
    public SoundEvent getVariantSound(int variantIndex) {
        return NWSounds.getGeigerSound(this, variantIndex);
    }

    /**
     * Maps a rads/sec value to one of six intensity levels using a single max threshold.
     * The range {@code (0, maxRads]} is divided into 6 equal bands; anything at or above
     * {@code maxRads} clamps to {@link #LEVEL_6}.
     */
    public static GeigerLevel fromRadsPerSec(double radsPerSec, double maxRads) {
        if (radsPerSec <= 0) return NONE;
        int band = (int) (radsPerSec * 6.0 / maxRads);
        band = Math.min(band, 5);
        return values()[band + 1]; // +1 to skip NONE at ordinal 0
    }
}
