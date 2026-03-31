package net.tomato3017.nuclearwinter.debug;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks blocks that fall through radiation resistance and degradation rule lookups when
 * capture is active. Call {@link #start()} to begin recording, {@link #stop()} to end it
 * and retrieve the collected data. Thread-safe; designed for minimal overhead when inactive.
 */
public final class BlockCaptureManager {
    private static volatile boolean active = false;
    private static final Set<ResourceLocation> raycastMisses = ConcurrentHashMap.newKeySet();
    private static final Set<ResourceLocation> degradationMisses = ConcurrentHashMap.newKeySet();

    private BlockCaptureManager() {
    }

    /**
     * Begins a new capture session, clearing any previously recorded data.
     */
    public static void start() {
        raycastMisses.clear();
        degradationMisses.clear();
        active = true;
    }

    /**
     * Ends the current capture session.
     *
     * @return a snapshot of the captured data, sorted alphabetically
     */
    public static CaptureSnapshot stop() {
        active = false;
        return new CaptureSnapshot(
                new TreeSet<>(raycastMisses),
                new TreeSet<>(degradationMisses)
        );
    }

    /**
     * Records an occluding block that had no explicit radiation resistance entry (fell through
     * to {@code defaultResistance}). No-op when capture is inactive.
     */
    public static void recordRaycastMiss(Block block) {
        if (!active) return;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id != null) {
            raycastMisses.add(id);
        }
    }

    /**
     * Records a block that had no matching degradation rule for the current stage. No-op when
     * capture is inactive.
     */
    public static void recordDegradationMiss(Block block) {
        if (!active) return;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id != null) {
            degradationMisses.add(id);
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static int raycastMissCount() {
        return raycastMisses.size();
    }

    public static int degradationMissCount() {
        return degradationMisses.size();
    }

    /**
     * Immutable snapshot of captured block IDs returned by {@link #stop()}.
     */
    public record CaptureSnapshot(
            Set<ResourceLocation> raycastMisses,
            Set<ResourceLocation> degradationMisses
    ) {
    }
}
