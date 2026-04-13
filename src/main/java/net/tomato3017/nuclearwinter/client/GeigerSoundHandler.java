package net.tomato3017.nuclearwinter.client;

import net.minecraft.client.Minecraft;
import net.tomato3017.nuclearwinter.network.GeigerLevel;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Client-side manager for the Geiger counter ambient audio.
 * Responds to {@link GeigerLevel} updates from the server and plays non-looping clips,
 * restarting on each completion with a randomly chosen variant that differs from the
 * previous one. All methods must be called on the main client thread.
 */
public class GeigerSoundHandler {

    private static final int VARIANT_COUNT = 3;
    private static final int LEVEL_COUNT = 6;

    private static GeigerLevel currentLevel = GeigerLevel.NONE;
    private static GeigerLoopSound activeSound = null;

    /**
     * Tracks the last-played variant index for each level (0-based).
     * -1 means no variant has been played for that level yet.
     */
    private static final int[] lastVariantByLevel = new int[LEVEL_COUNT];

    static {
        java.util.Arrays.fill(lastVariantByLevel, -1);
    }

    private GeigerSoundHandler() {}

    /**
     * Updates the looping sound to match the given level.
     * Stops the current clip if the level has changed, then starts the new one.
     */
    public static void setLevel(GeigerLevel level) {
        if (level == currentLevel) return;

        stopActiveSound();
        currentLevel = level;

        if (level != GeigerLevel.NONE) {
            playNextVariant();
        }
    }

    /**
     * Must be called every client tick. Detects when a non-looping clip has finished
     * and immediately starts the next variant so playback continues seamlessly.
     */
    public static void tick() {
        if (activeSound == null || currentLevel == GeigerLevel.NONE) return;
        if (!Minecraft.getInstance().getSoundManager().isActive(activeSound)) {
            playNextVariant();
        }
    }

    /**
     * Stops any playing Geiger sound immediately, e.g. on client disconnect.
     */
    public static void stopAll() {
        stopActiveSound();
        currentLevel = GeigerLevel.NONE;
        java.util.Arrays.fill(lastVariantByLevel, -1);
    }

    private static void playNextVariant() {
        int levelIdx = currentLevel.getLevelIndex();
        int variant = pickVariant(lastVariantByLevel[levelIdx]);
        lastVariantByLevel[levelIdx] = variant;
        activeSound = new GeigerLoopSound(currentLevel.getVariantSound(variant));
        Minecraft.getInstance().getSoundManager().play(activeSound);
    }

    private static void stopActiveSound() {
        if (activeSound != null) {
            Minecraft.getInstance().getSoundManager().stop(activeSound);
            activeSound = null;
        }
    }

    /**
     * Picks a random variant index in [0, VARIANT_COUNT) that is not {@code excludeIndex}.
     * When {@code excludeIndex} is -1 (no prior play), all variants are eligible.
     */
    private static int pickVariant(int excludeIndex) {
        int pool = excludeIndex < 0 ? VARIANT_COUNT : VARIANT_COUNT - 1;
        int pick = ThreadLocalRandom.current().nextInt(pool);
        if (excludeIndex >= 0 && pick >= excludeIndex) pick++;
        return pick;
    }
}
