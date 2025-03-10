package com.iafenvoy.resgen.data.single;

import com.iafenvoy.resgen.data.GeneratorType;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public abstract sealed class BlockGeneratorDataBase extends GeneratorDataBase permits SingleBlockGeneratorData, BlockTagGeneratorData {
    public BlockGeneratorDataBase(GeneratorType type, BlockPos pos) {
        super(type, pos);
    }

    @Override
    public void generate(ServerWorld world) {
        world.setBlockState(this.pos, this.getNextBlock());
    }

    protected abstract BlockState getNextBlock();
}
