package net.tomato3017.nuclearwinter.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerPlayer;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.data.ChunkDataAttachment;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.PlayerDataAttachment;
import net.tomato3017.nuclearwinter.radiation.RadiationEmitter;
import net.tomato3017.nuclearwinter.stage.StageBase;

import java.util.HashSet;
import java.util.Set;

/**
 * Debug subcommands registered under {@code /nuclearwinter debug}.
 * Provides {@code resetrad} (clear a player's radiation pool) and
 * {@code raycast}/{@code chunk} helpers for in-game testing.
 */
public class DebugCommand {
    private static final Set<ServerPlayer> raycastAutoPlayers = new HashSet<>();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("debug")
                .then(Commands.literal("resetrad")
                        .executes(ctx -> executeResetRad(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeResetRad(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("raycast")
                        .executes(DebugCommand::executeRaycast).then(
                                Commands.literal("auto").executes(DebugCommand::executeRaycastAuto))
                        .then(Commands.literal("reset").executes(DebugCommand::executeRaycastReset)))
                .then(Commands.literal("chunk")
                        .then(Commands.literal("status")
                                .executes(DebugCommand::executeChunkStatus))
                        .then(Commands.literal("requeue")
                                .executes(DebugCommand::executeChunkRequeue)));
    }

    private static int executeRaycastReset(CommandContext<CommandSourceStack> ctx) {
        raycastAutoPlayers.remove(ctx.getSource().getPlayer());
        return 1;
    }

    public static void onPlayerTick(ServerPlayer player) {
        if (player.level().getGameTime() % 20 == 0) {
            if (raycastAutoPlayers.contains(player)) {
                RaycastResult result = executeRaycastInternal(player);
                if (result != null) {
                    double poolPercent = (result.radiationPool() / Config.PLAYER_POOL_MAX.get()) * 100.0;
                    player.displayClientMessage(Component.literal(String.format(
                            "Raycast result: %.2f Rads/sec (sky emission: %.0f, pool: %.0f / %.0f%% full)",
                            result.radsPerSec(), result.skyEmission(), result.radiationPool(), poolPercent
                    )), false);
                }
            }
        }
    }

    private static int executeResetRad(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        target.setData(NWAttachmentTypes.PLAYER_DATA, PlayerDataAttachment.DEFAULT);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset radiation pool for " + target.getName().getString() + " to 0"
        ), true);
        return 1;
    }

    private static int executeRaycastAuto(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        raycastAutoPlayers.add(ctx.getSource().getPlayer());
        return executeRaycast(ctx);
    }

    private static int executeChunkStatus(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        LevelChunk chunk = getCurrentChunk(player);
        BlockPos playerPos = player.blockPosition();
        BlockPos biomeSamplePos = getSurfaceBiomeSamplePos(player.serverLevel(), playerPos);
        String biomeId = getBiomeId(player.serverLevel(), biomeSamplePos);
        boolean nuked = chunk.getData(NWAttachmentTypes.CHUNK_DATA).nuked();

        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "Chunk [%d, %d] at [%d, %d, %d] in %s: nuked=%s, surface biome=%s",
                chunk.getPos().x,
                chunk.getPos().z,
                playerPos.getX(),
                playerPos.getY(),
                playerPos.getZ(),
                player.level().dimension().location(),
                nuked,
                biomeId
        )), false);
        return 1;
    }

    private static int executeChunkRequeue(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        StageBase stage = NuclearWinter.getStageManager().getStageForWorld(player.level().dimension());
        if (stage == null) {
            ctx.getSource().sendFailure(Component.literal(
                    "No active stage data for " + player.level().dimension().location()
            ));
            return 0;
        }

        if (!stage.hasChunkProcessor()) {
            ctx.getSource().sendFailure(Component.literal(
                    "Current stage does not have chunk processing enabled"
            ));
            return 0;
        }

        if (!stage.isNukeMode()) {
            ctx.getSource().sendFailure(Component.literal(
                    "Current stage is not in chunk nuking mode"
            ));
            return 0;
        }

        LevelChunk chunk = getCurrentChunk(player);
        chunk.setData(NWAttachmentTypes.CHUNK_DATA, ChunkDataAttachment.DEFAULT);
        chunk.setUnsaved(true);
        stage.requeueChunk(chunk);

        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "Requeued chunk [%d, %d] in %s for nuking",
                chunk.getPos().x,
                chunk.getPos().z,
                player.level().dimension().location()
        )), true);
        return 1;
    }

    private static RaycastResult executeRaycastInternal(ServerPlayer player) {
        StageBase stage = NuclearWinter.getStageManager()
                .getStageForWorld(player.level().dimension());

        if (stage == null || stage.getSkyEmission() <= 0) {
            return null;
        }

        double skyEmission = stage.getSkyEmission();
        double radsPerSec = RadiationEmitter.raycastDown(player.level(), player.blockPosition(), skyEmission);
        PlayerDataAttachment data = player.getData(NWAttachmentTypes.PLAYER_DATA);

        return new RaycastResult(radsPerSec, data.radiationPool(), skyEmission);
    }

    private static int executeRaycast(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        RaycastResult result = executeRaycastInternal(player);
        if (result == null) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "No active radiation (stage inactive or sky emission = 0)"
            ), false);
            return 1;
        }

        double poolPercent = (result.radiationPool() / Config.PLAYER_POOL_MAX.get()) * 100.0;
        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                "Raycast result: %.2f Rads/sec (sky emission: %.0f, pool: %.0f / %.0f%% full)",
                result.radsPerSec(), result.skyEmission(), result.radiationPool(), poolPercent
        )), false);
        return 1;
    }

    private static LevelChunk getCurrentChunk(ServerPlayer player) {
        return player.serverLevel().getChunkSource().getChunkNow(player.chunkPosition().x, player.chunkPosition().z);
    }

    private static BlockPos getSurfaceBiomeSamplePos(ServerLevel level, BlockPos playerPos) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, playerPos.getX(), playerPos.getZ());
        int sampleY = Math.max(level.getMinBuildHeight(), surfaceY - 1);
        return new BlockPos(playerPos.getX(), sampleY, playerPos.getZ());
    }

    private static String getBiomeId(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos)
                .unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unknown");
    }

    private record RaycastResult(double radsPerSec, double radiationPool, double skyEmission) {
    }
}
