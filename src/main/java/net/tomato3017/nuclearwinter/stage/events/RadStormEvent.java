package net.tomato3017.nuclearwinter.stage.events;

import net.minecraft.server.level.ServerLevel;
import net.tomato3017.nuclearwinter.NuclearWinter;

public class RadStormEvent implements StageEvent {

    @Override
    public void onStart() {
        NuclearWinter.LOGGER.info("RadStorm start event");
    }

    @Override
    public void onEnd() {
        NuclearWinter.LOGGER.info("RadStorm end event");
    }

    @Override
    public boolean onTick(ServerLevel level) {
        if (level.getGameTime() % 20 == 0)
            NuclearWinter.LOGGER.info("RadStorm tick event");
        return true;
    }
}
