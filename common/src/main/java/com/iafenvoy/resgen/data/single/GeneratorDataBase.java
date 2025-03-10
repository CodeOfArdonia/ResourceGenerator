package com.iafenvoy.resgen.data.single;

import com.iafenvoy.resgen.data.GeneratorType;
import com.iafenvoy.resgen.util.Serializeable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public abstract sealed class GeneratorDataBase implements Serializeable permits BlockGeneratorDataBase, ItemGeneratorDataBase {
    private final GeneratorType type;
    protected final BlockPos pos;
    private boolean shouldProtect = false;
    private int interval = 0;
    private int currentTick = 0;

    public GeneratorDataBase(GeneratorType type, BlockPos pos) {
        this.type = type;
        this.pos = pos;
    }

    public GeneratorDataBase protect() {
        this.shouldProtect = true;
        return this;
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putInt("interval", this.interval);
        nbt.putInt("currentTick", this.currentTick);
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        this.interval = nbt.getInt("interval");
        this.currentTick = nbt.getInt("currentTick");
    }

    public abstract void generate(ServerWorld world);

    public GeneratorType getType() {
        return this.type;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void tick(ServerWorld world) {
        BlockState state = world.getBlockState(this.pos);
        if (!state.isOf(Blocks.AIR)) {
            if (this.shouldProtect()) world.setBlockState(this.pos, Blocks.AIR.getDefaultState());
            else return;
        }
        this.currentTick--;
        if (this.currentTick <= 0) {
            this.currentTick = this.interval;
            this.generate(world);
        }
    }

    public boolean shouldProtect() {
        return this.shouldProtect && !this.type.isBlock();
    }

    public static NbtCompound encode(GeneratorDataBase data) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("type", data.type.toString());
        nbt.putLong("pos", data.pos.asLong());
        nbt.put("data", data.writeToNbt());
        return nbt;
    }

    public static GeneratorDataBase decode(NbtCompound nbt) {
        GeneratorType type = GeneratorType.valueOf(nbt.getString("type"));
        return type.construct(BlockPos.fromLong(nbt.getLong("pos")), nbt.getCompound("data"));
    }
}
