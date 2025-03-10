package com.iafenvoy.resgen.data.single;

import com.iafenvoy.resgen.ResourceGenerator;
import com.iafenvoy.resgen.data.GeneratorType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class SingleItemGeneratorData extends ItemGeneratorDataBase {
    private ItemStack stack = ItemStack.EMPTY;

    public SingleItemGeneratorData(BlockPos pos) {
        super(GeneratorType.SINGLE_ITEM, pos);
    }

    public SingleItemGeneratorData(BlockPos pos, ItemStack stack) {
        super(GeneratorType.SINGLE_ITEM, pos);
        this.stack = stack;
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        super.writeToNbt(nbt);
        nbt.put("item", ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, this.stack).resultOrPartial(ResourceGenerator.LOGGER::error).orElse(new NbtCompound()));
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        super.readFromNbt(nbt);
        this.stack = ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt.get("item")).resultOrPartial(ResourceGenerator.LOGGER::error).orElse(ItemStack.EMPTY);
    }

    @Override
    public List<ItemStack> getNextItems(ServerWorld world) {
        return List.of(this.stack);
    }

    @Override
    public MutableText getInfo() {
        return super.getInfo().append(Text.literal("\nItem: %s".formatted(this.stack.toString())).fillStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(this.stack)))));
    }
}
