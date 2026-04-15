package net.tomato3017.nuclearwinter.stage.events;

import net.minecraft.server.level.ServerLevel;

public interface StageEvent {
    void onStart();

    void onEnd();

    // returns true if the stage should continue firing
    boolean onTick(ServerLevel level);
}
