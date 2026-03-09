package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

public class Stage1 extends StageBase {
    public static final int INDEX = 2;

    public Stage1() {
        super(INDEX, Config.STAGE1_DURATION.get(), Config.STAGE1_SKY_EMISSION.get());
    }
}
