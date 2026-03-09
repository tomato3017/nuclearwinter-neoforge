package net.tomato3017.nuclearwinter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WastelandDustBlock extends FallingBlock {
    public static final MapCodec<WastelandDustBlock> CODEC = simpleCodec(WastelandDustBlock::new);

    public WastelandDustBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<WastelandDustBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return 0xC8C8C0;
    }
}
