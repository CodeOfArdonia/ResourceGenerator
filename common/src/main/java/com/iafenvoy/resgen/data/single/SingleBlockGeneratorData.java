package com.iafenvoy.resgen.data.single;

import com.iafenvoy.resgen.ResourceGenerator;
import com.iafenvoy.resgen.data.GeneratorType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class SingleBlockGeneratorData extends BlockGeneratorDataBase {
    private BlockState state = Blocks.AIR.getDefaultState();

    public SingleBlockGeneratorData(BlockPos pos) {
        super(GeneratorType.SINGLE_BLOCK, pos);
    }

    public SingleBlockGeneratorData(BlockPos pos, BlockState state) {
        super(GeneratorType.SINGLE_BLOCK, pos);
        this.state = state;
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        super.writeToNbt(nbt);
        nbt.put("block", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, this.state).resultOrPartial(ResourceGenerator.LOGGER::error).orElse(new NbtCompound()));
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        super.readFromNbt(nbt);
        this.state = BlockState.CODEC.parse(NbtOps.INSTANCE, nbt.get("block")).resultOrPartial(ResourceGenerator.LOGGER::error).orElse(Blocks.AIR.getDefaultState());
    }

    @Override
    protected BlockState getNextBlock() {
        return this.state;
    }

    @Override
    public MutableText getInfo() {
        return super.getInfo().append(Text.literal("\nBlock: %s".formatted(this.state.toString())));
    }
}
