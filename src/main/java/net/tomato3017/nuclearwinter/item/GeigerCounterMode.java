package net.tomato3017.nuclearwinter.item;

/**
 * Calibration mode for {@link GeigerCounterItem}. Both modes measure raw sky emission via
 * {@code RadiationEmitter.raycastDown()} (before suit protection), but differ in the
 * Rads/sec thresholds at which each {@link net.tomato3017.nuclearwinter.network.GeigerLevel}
 * triggers — LOW_RANGE is tuned for detecting small indoor leaks; HIGH_RANGE is tuned for
 * surface environments where a hazmat-equipped player needs to detect hot zones.
 */
public enum GeigerCounterMode {
    LOW_RANGE(50.0, 300.0),
    HIGH_RANGE(500.0, 3000.0);

    private final double thresholdMed;
    private final double thresholdHigh;

    GeigerCounterMode(double thresholdMed, double thresholdHigh) {
        this.thresholdMed = thresholdMed;
        this.thresholdHigh = thresholdHigh;
    }

    public double thresholdMed() {
        return thresholdMed;
    }

    public double thresholdHigh() {
        return thresholdHigh;
    }
}
