package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.network.GeigerLevel;

/**
 * Calibration mode for {@link GeigerCounterItem}. Both modes measure raw sky emission via
 * {@code RadiationEmitter.raycastDown()} (before suit protection), but differ in their
 * maximum Rads/sec range. The range {@code (0, maxRads]} is divided into six equal bands
 * for {@link GeigerLevel} mapping — LOW_RANGE is tuned for detecting small indoor leaks;
 * HIGH_RANGE is tuned for surface environments where a hazmat-equipped player needs
 * to detect hot zones.
 */
public enum GeigerCounterMode {
    LOW_RANGE(300.0),
    HIGH_RANGE(3000.0);

    private final double maxRads;

    GeigerCounterMode(double maxRads) {
        this.maxRads = maxRads;
    }

    public double maxRads() {
        return maxRads;
    }
}
