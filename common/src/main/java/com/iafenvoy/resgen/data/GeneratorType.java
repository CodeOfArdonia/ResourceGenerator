package com.iafenvoy.resgen.data;

import com.iafenvoy.resgen.data.single.*;
import com.iafenvoy.resgen.util.StringUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.function.Function;

public enum GeneratorType {
    SINGLE_ITEM(SingleItemGeneratorData::new, false),
    ITEM_TAG(ItemTagGeneratorData::new, false),
    ITEM_LOOT_TABLE(ItemLootTableGeneratorData::new, false),
    SINGLE_BLOCK(SingleBlockGeneratorData::new, true),
    BLOCK_TAG(BlockTagGeneratorData::new, true);

    private final Function<BlockPos, GeneratorDataBase> dataConstructor;
    private final boolean isBlock;

    GeneratorType(Function<BlockPos, GeneratorDataBase> dataConstructor, boolean isBlock) {
        this.dataConstructor = dataConstructor;
        this.isBlock = isBlock;
    }

    public GeneratorDataBase construct(BlockPos pos, NbtCompound nbt) {
        GeneratorDataBase data = this.dataConstructor.apply(pos);
        data.readFromNbt(nbt);
        return data;
    }

    public boolean isBlock() {
        return this.isBlock;
    }

    @Override
    public String toString() {
        return StringUtils.formatNameString(this.name());
    }
}
