package net.tomato3017.nuclearwinter.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.tomato3017.nuclearwinter.radiation.RadiationTier;

/**
 * GameTest stubs for {@link RadiationTier}. Tests are written but not runnable until
 * the "empty_1x1" structure template is registered via the data generator.
 */
@GameTestHolder("nuclearwinter")
@PrefixGameTestTemplate(false)
public class RadiationTierGameTest {

    @GameTest(template = "empty_1x1")
    public void zeroPoolIsClean(GameTestHelper helper) {
        RadiationTier tier = RadiationTier.fromPool(0, 100_000);
        helper.assertTrue(tier == RadiationTier.CLEAN, "0 pool should be CLEAN, got " + tier);
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void fullPoolIsFatal(GameTestHelper helper) {
        RadiationTier tier = RadiationTier.fromPool(100_000, 100_000);
        helper.assertTrue(tier == RadiationTier.FATAL, "Full pool should be FATAL, got " + tier);
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void borderlineContaminated(GameTestHelper helper) {
        RadiationTier tier = RadiationTier.fromPool(15_000, 100_000);
        helper.assertTrue(tier == RadiationTier.CONTAMINATED,
                "15% pool should be CONTAMINATED, got " + tier);
        helper.succeed();
    }

    @GameTest(template = "empty_1x1")
    public void borderlineCritical(GameTestHelper helper) {
        RadiationTier tier = RadiationTier.fromPool(80_000, 100_000);
        helper.assertTrue(tier == RadiationTier.CRITICAL,
                "80% pool should be CRITICAL, got " + tier);
        helper.succeed();
    }
}
