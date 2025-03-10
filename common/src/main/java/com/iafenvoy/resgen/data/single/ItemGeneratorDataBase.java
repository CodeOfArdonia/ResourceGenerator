package com.iafenvoy.resgen.data.single;

import com.iafenvoy.resgen.data.GeneratorType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public abstract sealed class ItemGeneratorDataBase extends GeneratorDataBase permits ItemLootTableGeneratorData, ItemTagGeneratorData, SingleItemGeneratorData {
    public ItemGeneratorDataBase(GeneratorType type, BlockPos pos) {
        super(type, pos);
    }

    @Override
    public void generate(ServerWorld world) {
        Vec3d pos = this.pos.toCenterPos();
        for (ItemStack stack : this.getNextItems(world))
            world.spawnEntity(new ItemEntity(world, pos.x, pos.y, pos.z, stack.copy()));
    }

    public abstract List<ItemStack> getNextItems(ServerWorld world);
}
