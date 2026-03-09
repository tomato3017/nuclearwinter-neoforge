package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

public class Stage3 extends StageBase {
    public static final int INDEX = 4;

    public Stage3() {
        super(INDEX, Config.STAGE3_DURATION.get(), Config.STAGE3_SKY_EMISSION.get());
    }
}
