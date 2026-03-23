package net.tomato3017.nuclearwinter.radiation;

import net.tomato3017.nuclearwinter.Config;

/**
 * Maps a player's radiation pool percentage to a named severity tier.
 * Call {@link #fromPool(double, double)} each tick to resolve the current tier;
 * boundaries are read from {@link Config} so they are configurable.
 */
public enum RadiationTier {
    CLEAN(0.0, 0.15),
    CONTAMINATED(0.15, 0.35),
    IRRADIATED(0.35, 0.60),
    POISONED(0.60, 0.80),
    CRITICAL(0.80, 1.0),
    FATAL(1.0, 1.0);

    private final double defaultMinPercent;
    private final double defaultMaxPercent;

    RadiationTier(double defaultMinPercent, double defaultMaxPercent) {
        this.defaultMinPercent = defaultMinPercent;
        this.defaultMaxPercent = defaultMaxPercent;
    }

    public double getMinPercent() { return defaultMinPercent; }
    public double getMaxPercent() { return defaultMaxPercent; }

    /**
     * Resolves the tier for a player whose pool currently holds {@code pool} out of {@code poolMax} Rads.
     * Boundaries are read from config, so they reflect any in-game config changes.
     */
    public static RadiationTier fromPool(double pool, double poolMax) {
        if (poolMax <= 0) return CLEAN;
        double percent = pool / poolMax;

        if (percent >= 1.0) return FATAL;

        double critical     = Config.THRESHOLD_CRITICAL.get();
        double poisoned     = Config.THRESHOLD_POISONED.get();
        double irradiated   = Config.THRESHOLD_IRRADIATED.get();
        double contaminated = Config.THRESHOLD_CONTAMINATED.get();

        if (percent >= critical)     return CRITICAL;
        if (percent >= poisoned)     return POISONED;
        if (percent >= irradiated)   return IRRADIATED;
        if (percent >= contaminated) return CONTAMINATED;
        return CLEAN;
    }
}
