package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.network.GeigerLevel;

/**
 * Calibration mode for {@link GeigerCounterItem}. Both modes measure raw sky emission via
 * {@code RadiationEmitter.raycastDown()} (before suit protection), but differ in their
 * maximum Rads/sec range. The range {@code (0, maxRads]} is divided into six equal bands
 * for {@link GeigerLevel} mapping — LOW_RANGE is tuned for detecting small indoor leaks;
 * HIGH_RANGE is tuned for surface environments where a hazmat-equipped player needs
 * to detect hot zones. Bounds are read from {@link Config}.
 */
public enum GeigerCounterMode {
    LOW_RANGE,
    HIGH_RANGE;

    public double maxRads() {
        return switch (this) {
            case LOW_RANGE -> Config.GEIGER_LOW_RANGE_MAX_RADS.get();
            case HIGH_RANGE -> Config.GEIGER_HIGH_RANGE_MAX_RADS.get();
        };
    }
}
