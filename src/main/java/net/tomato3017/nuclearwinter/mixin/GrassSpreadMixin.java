package net.tomato3017.nuclearwinter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tomato3017.nuclearwinter.NuclearWinter;
import net.tomato3017.nuclearwinter.stage.StageBase;
import net.tomato3017.nuclearwinter.stage.StageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses grass spread (randomTick on {@link SpreadingSnowyDirtBlock}) when the
 * dimension's apocalypse stage is Stage 1 or later (stageIndex >= 2).
 */
@Mixin(SpreadingSnowyDirtBlock.class)
public class GrassSpreadMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (NuclearWinter.getStageManager() == null) return;

        StageBase stage = NuclearWinter.getStageManager().getStageForWorld(level.dimension());
        if (stage != null && stage.getStageType().isAtLeast(StageType.STAGE_1)) {
            ci.cancel();
        }
    }
}
