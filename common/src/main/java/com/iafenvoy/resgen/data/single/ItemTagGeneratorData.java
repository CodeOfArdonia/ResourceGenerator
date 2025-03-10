package com.iafenvoy.resgen.data.single;

import com.iafenvoy.resgen.data.GeneratorType;
import com.iafenvoy.resgen.util.RandomHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class ItemTagGeneratorData extends ItemGeneratorDataBase {
    private TagKey<Item> itemTag = TagKey.of(RegistryKeys.ITEM, Identifier.tryParse(""));
    private List<Item> items = List.of();

    public ItemTagGeneratorData(BlockPos pos) {
        super(GeneratorType.ITEM_TAG, pos);
    }

    public ItemTagGeneratorData(BlockPos pos, TagKey<Item> itemTag) {
        super(GeneratorType.ITEM_TAG, pos);
        this.itemTag = itemTag;
        this.collectItems();
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        super.writeToNbt(nbt);
        nbt.putString("itemTag", this.itemTag.id().toString());
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        super.readFromNbt(nbt);
        this.itemTag = TagKey.of(RegistryKeys.ITEM, Identifier.tryParse(nbt.getString("itemTag")));
        this.collectItems();
    }

    private void collectItems() {
        this.items = Registries.ITEM.streamEntries().filter(x -> x.isIn(this.itemTag)).map(RegistryEntry.Reference::value).toList();
    }

    @Override
    public List<ItemStack> getNextItems(ServerWorld world) {
        return List.of(RandomHelper.randomOne(this.items).getDefaultStack());
    }
}
