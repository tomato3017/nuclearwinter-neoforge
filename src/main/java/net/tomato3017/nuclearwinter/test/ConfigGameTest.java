package net.tomato3017.nuclearwinter.test;

import net.tomato3017.nuclearwinter.Config;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("nuclearwinter")
@PrefixGameTestTemplate(false)
public class ConfigGameTest {

    @GameTest(template = "empty_1x1")
    public void configDefaults(GameTestHelper helper) {
        helper.assertTrue(Config.FLOOR_CONSTANT.get() == 50.0, "Floor constant should be 50");
        helper.assertTrue(Config.PLAYER_POOL_MAX.get() == 100000.0, "Pool max should be 100000");
        helper.assertTrue(Config.PASSIVE_DRAIN_RATE.get() == 100.0, "Drain rate should be 100");
        helper.assertTrue(Config.STAGE1_SKY_EMISSION.get() == 28.0, "Stage 1 emission should be 28");
        helper.succeed();
    }
}
