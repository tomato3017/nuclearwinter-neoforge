package net.tomato3017.nuclearwinter.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.data.NWAttachmentTypes;
import net.tomato3017.nuclearwinter.data.WorldDataAttachment;
import net.tomato3017.nuclearwinter.stage.StageBase;
import net.tomato3017.nuclearwinter.stage.StageFactory;
import net.tomato3017.nuclearwinter.stage.StageManager;
import net.tomato3017.nuclearwinter.stage.StageType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Map;

public class NuclearWinterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nuclearwinter")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("start")
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .executes(NuclearWinterCommand::executeStart)))
                .then(Commands.literal("stop")
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .executes(NuclearWinterCommand::executeStop)))
                .then(Commands.literal("status")
                        .executes(NuclearWinterCommand::executeStatusAll)
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .executes(NuclearWinterCommand::executeStatusDimension)))
                .then(Commands.literal("setstage")
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .then(Commands.argument("stage", IntegerArgumentType.integer(0, StageType.MAX_INDEX))
                                        .executes(NuclearWinterCommand::executeSetStageByIndex))
                                .then(Commands.argument("stageName", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            Arrays.stream(StageType.values())
                                                    .map(StageType::name)
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(NuclearWinterCommand::executeSetStageByName))))
                .then(Commands.literal("stages")
                        .executes(NuclearWinterCommand::executeListStages))
                .then(Commands.literal("advancetime")
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(1))
                                        .executes(NuclearWinterCommand::executeAdvanceTime))))
        );
    }

    // TODO Make more of a grand announcement for the start command.
    private static int executeStart(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
        StageManager mgr = NuclearWinter.getStageManager();
        StageBase current = mgr.getStageForWorld(level.dimension());
        if (current != null && current.getStageIndex() > 0) {
            ctx.getSource().sendFailure(Component.literal("Apocalypse already active in " + level.dimension().location()));
            return 0;
        }
        mgr.startApocalypse(level);
        ctx.getSource().sendSuccess(() -> Component.literal("Apocalypse started in " + level.dimension().location()), true);
        return 1;
    }

    private static int executeStop(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
        StageManager mgr = NuclearWinter.getStageManager();
        mgr.stopApocalypse(level);
        ctx.getSource().sendSuccess(() -> Component.literal("Apocalypse stopped in " + level.dimension().location()), true);
        return 1;
    }

    private static int executeStatusAll(CommandContext<CommandSourceStack> ctx) {
        StageManager mgr = NuclearWinter.getStageManager();
        Map<ResourceKey<Level>, StageBase> stages = mgr.getAllStages();
        if (stages.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No dimensions tracked."), false);
            return 1;
        }
        
        for (var entry : stages.entrySet()) {
            StageBase stage = entry.getValue();
            String name = StageFactory.getStageName(stage.getStageIndex());
            ctx.getSource().sendSuccess(() -> Component.literal(
                    entry.getKey().location() + ": " + name +
                    " (sky emission: " + stage.getSkyEmission() + " Rads/sec)"
            ), false);
        }
        return 1;
    }

    private static int executeStatusDimension(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
        StageManager mgr = NuclearWinter.getStageManager();
        StageBase stage = mgr.getStageForWorld(level.dimension());
        if (stage == null) {
            ctx.getSource().sendFailure(Component.literal("No stage data for " + level.dimension().location()));
            return 0;
        }
        String name = StageFactory.getStageName(stage.getStageIndex());
        long elapsed = level.getGameTime() - stage.getInitTick();
        long remaining = stage.getDuration() > 0 ? stage.getDuration() - elapsed : -1;
        String remainStr = stage.isShouldStageExpire() ? String.format("%.0fs", remaining / 20.0) : "Infinite";

        ctx.getSource().sendSuccess(() -> Component.literal(
                level.dimension().location() + ": " + name +
                " | Sky: " + stage.getSkyEmission() + " Rads/sec" +
                " | Time remaining: " + remainStr
        ), false);
        return 1;
    }

    private static int executeSetStageByIndex(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
        int stageIndex = IntegerArgumentType.getInteger(ctx, "stage");
        StageManager mgr = NuclearWinter.getStageManager();
        mgr.setStage(level, stageIndex);
        String name = StageFactory.getStageName(stageIndex);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Set " + level.dimension().location() + " to " + name
        ), true);
        return 1;
    }

    private static int executeSetStageByName(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
        String stageName = StringArgumentType.getString(ctx, "stageName");
        StageType type;
        try {
            type = StageType.fromName(stageName);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal(
                    "Unknown stage \"" + stageName + "\". Use /nuclearwinter stages to list valid stages."
            ));
            return 0;
        }
        NuclearWinter.getStageManager().setStage(level, type.getIndex());
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Set " + level.dimension().location() + " to " + type.getDisplayName()
        ), true);
        return 1;
    }

    private static int executeListStages(CommandContext<CommandSourceStack> ctx) {
        for (StageType type : StageType.values()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "[" + type.getIndex() + "] " + type.name() + "-" + type.getDisplayName()
            ), false);
        }
        return 1;
    }

    private static int executeAdvanceTime(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
        StageManager mgr = NuclearWinter.getStageManager();
        StageBase stage = mgr.getStageForWorld(level.dimension());

        if (stage == null) {
            ctx.getSource().sendFailure(Component.literal("No stage data for " + level.dimension().location()));
            return 0;
        }

        long newInitTick = stage.getInitTick() - ticks;
        stage.setInitTick(newInitTick);
        level.setData(NWAttachmentTypes.WORLD_DATA, new WorldDataAttachment(stage.getStageIndex(), newInitTick));

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Advanced stage timer by " + ticks + " ticks (" + String.format("%.1f", ticks / 20.0) + " seconds)"
        ), true);
        return 1;
    }
}
