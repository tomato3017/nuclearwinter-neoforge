package net.tomato3017.nuclearwinter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.tomato3017.nuclearwinter.network.GeigerLevel;

/**
 * Client-side manager for the Geiger counter looping ambient track.
 * Responds to {@link GeigerLevel} updates from the server and swaps the active
 * {@link GeigerLoopSound} accordingly. All methods must be called on the main client thread.
 */
public class GeigerSoundHandler {

    private static GeigerLevel currentLevel = GeigerLevel.NONE;
    private static GeigerLoopSound activeSound = null;

    private GeigerSoundHandler() {}

    /**
     * Updates the looping sound to match the given level.
     * Stops the current track if the level has changed, then starts the new one.
     */
    public static void setLevel(GeigerLevel level) {
        if (level == currentLevel) return;

        stopActiveSound();
        currentLevel = level;

        SoundEvent soundEvent = level.getSoundEvent();
        if (soundEvent != null) {
            activeSound = new GeigerLoopSound(soundEvent);
            Minecraft.getInstance().getSoundManager().play(activeSound);
        }
    }

    /**
     * Stops any playing Geiger sound immediately, e.g. on client disconnect.
     */
    public static void stopAll() {
        stopActiveSound();
        currentLevel = GeigerLevel.NONE;
    }

    private static void stopActiveSound() {
        if (activeSound != null) {
            Minecraft.getInstance().getSoundManager().stop(activeSound);
            activeSound = null;
        }
    }
}
