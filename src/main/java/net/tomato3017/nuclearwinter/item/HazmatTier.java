package net.tomato3017.nuclearwinter.item;

import net.tomato3017.nuclearwinter.Config;

/**
 * Equipment tier for hazmat suits, mapping to the corresponding {@link Config} protection value.
 */
public enum HazmatTier {
    TIER_1,
    TIER_2,
    TIER_3;

    public double getConfigProtection() {
        return switch (this) {
            case TIER_1 -> Config.SUIT_TIER1_PROTECTION.get();
            case TIER_2 -> Config.SUIT_TIER2_PROTECTION.get();
            case TIER_3 -> Config.SUIT_TIER3_PROTECTION.get();
        };
    }
}
