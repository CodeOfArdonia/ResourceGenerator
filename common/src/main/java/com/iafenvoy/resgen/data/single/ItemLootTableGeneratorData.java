package com.iafenvoy.resgen.data.single;

import com.iafenvoy.resgen.data.GeneratorType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Function;

public final class ItemLootTableGeneratorData extends ItemGeneratorDataBase {
    private final Function<ServerWorld, LootContextParameterSet> paramsFunc = world -> new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).build(LootContextTypes.CHEST);
    private Identifier lootTable = Identifier.tryParse("");

    public ItemLootTableGeneratorData(BlockPos pos) {
        super(GeneratorType.ITEM_LOOT_TABLE, pos);
    }

    public ItemLootTableGeneratorData(BlockPos pos, Identifier lootTable) {
        super(GeneratorType.ITEM_LOOT_TABLE, pos);
        this.lootTable = lootTable;
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        super.writeToNbt(nbt);
        nbt.putString("lootTable", this.lootTable.toString());
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        super.readFromNbt(nbt);
        this.lootTable = Identifier.tryParse(nbt.getString("lootTable"));
    }

    @Override
    public List<ItemStack> getNextItems(ServerWorld world) {
        return world.getServer().getLootManager().getLootTable(this.lootTable).generateLoot(this.paramsFunc.apply(world));
    }

    @Override
    public MutableText getInfo() {
        return super.getInfo().append("\nLoot Table: %s".formatted(this.lootTable.toString()));
    }
}
