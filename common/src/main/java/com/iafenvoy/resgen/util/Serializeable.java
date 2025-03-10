package com.iafenvoy.resgen.util;

import net.minecraft.nbt.NbtCompound;

public interface Serializeable {
    void writeToNbt(NbtCompound nbt);

    default NbtCompound writeToNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeToNbt(nbt);
        return nbt;
    }

    void readFromNbt(NbtCompound nbt);
}
