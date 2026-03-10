package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

public class Stage2 extends StageBase {
    public Stage2() {
        super(StageType.STAGE_2.getIndex(), Config.STAGE2_DURATION.get(), Config.STAGE2_SKY_EMISSION.get());
    }
}
