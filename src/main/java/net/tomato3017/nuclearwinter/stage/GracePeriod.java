package net.tomato3017.nuclearwinter.stage;

import net.tomato3017.nuclearwinter.Config;

public class GracePeriod extends StageBase {
    public static final int INDEX = 1;

    public GracePeriod() {
        super(INDEX, Config.GRACE_DURATION.get(), 0.0);
    }
}
