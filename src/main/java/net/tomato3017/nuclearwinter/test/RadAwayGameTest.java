package net.tomato3017.nuclearwinter.test;

import net.tomato3017.nuclearwinter.Config;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * GameTest stubs for RadAway and hazmat math. Not runnable until the empty_1x1 template exists.
 */
@GameTestHolder("nuclearwinter")
@PrefixGameTestTemplate(false)
public class RadAwayGameTest {

    @GameTest(template = "empty_1x1")
    public void radAwayDrainRate(GameTestHelper helper) {
        double totalReduction = Config.RADAWAY_REDUCTION.get();
        int durationTicks = Config.RADAWAY_DURATION_TICKS.get();
        double drainPerTick = totalReduction / durationTicks;
        helper.assertTrue(Math.abs(drainPerTick - (50000.0 / 3600.0)) < 1.0,
                "Drain per tick should be ~13.89, got " + drainPerTick);
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void hazmatTier1Protection(GameTestHelper helper) {
        double protection = Config.SUIT_TIER1_PROTECTION.get();
        helper.assertTrue(Math.abs(protection - 0.67) < 0.01,
                "Tier 1 protection should be 0.67, got " + protection);

        double rads = 333.0;
        double reduced = rads * (1.0 - protection);
        helper.assertTrue(reduced < 120.0 && reduced > 100.0,
                "333 Rads with Tier 1 should give ~110, got " + reduced);
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void hazmatTier3Protection(GameTestHelper helper) {
        double protection = Config.SUIT_TIER3_PROTECTION.get();
        double rads = 5000.0;
        double reduced = rads * (1.0 - protection);
        helper.assertTrue(reduced < 60.0,
                "5000 Rads with Tier 3 should give ~50, got " + reduced);
        helper.succeed();
    }
}
