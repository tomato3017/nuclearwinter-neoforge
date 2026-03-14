package net.tomato3017.nuclearwinter.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.tomato3017.nuclearwinter.Config;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.PlayerDataAttachment;
import net.tomato3017.nuclearwinter.radiation.RadiationEmitter;
import net.tomato3017.nuclearwinter.stage.StageBase;

import java.util.ArrayList;

/**
 * Debug subcommands registered under {@code /nuclearwinter debug}.
 * Provides {@code resetrad} (clear a player's radiation pool) and
 * {@code raycast} (print current exposure and pool fill percentage) for in-game testing.
 */
public class DebugCommand {
    private record RaycastResult(double radsPerSec, double radiationPool, double skyEmission) {
    }

    private static ArrayList<ServerPlayer> raycastAutoPlayers = new ArrayList<>();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("debug")
                .then(Commands.literal("resetrad")
                        .executes(ctx -> executeResetRad(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> executeResetRad(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("raycast")
                        .executes(DebugCommand::executeRaycast).then(
                                Commands.literal("auto").executes(DebugCommand::executeRaycastAuto))
                        .then(Commands.literal("reset").executes(DebugCommand::executeRaycastReset)));
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

    private static int executeRaycast(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
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
}
