package com.iafenvoy.resgen.data.single;

import com.iafenvoy.resgen.data.GeneratorType;
import com.iafenvoy.resgen.util.RandomHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class BlockTagGeneratorData extends BlockGeneratorDataBase {
    private TagKey<Block> blockTag = TagKey.of(RegistryKeys.BLOCK, Identifier.tryParse(""));
    private List<Block> blocks = List.of();

    public BlockTagGeneratorData(BlockPos pos) {
        super(GeneratorType.BLOCK_TAG, pos);
    }

    public BlockTagGeneratorData(BlockPos pos, Identifier tagId) {
        this(pos, TagKey.of(RegistryKeys.BLOCK, tagId));
    }

    public BlockTagGeneratorData(BlockPos pos, TagKey<Block> blockTag) {
        super(GeneratorType.BLOCK_TAG, pos);
        this.blockTag = blockTag;
        this.collectBlocks();
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        super.writeToNbt(nbt);
        nbt.putString("blockTag", this.blockTag.id().toString());
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        super.readFromNbt(nbt);
        this.blockTag = TagKey.of(RegistryKeys.BLOCK, Identifier.tryParse(nbt.getString("blockTag")));
        this.collectBlocks();
    }

    private void collectBlocks() {
        this.blocks = Registries.BLOCK.streamEntries().filter(x -> x.isIn(this.blockTag)).map(RegistryEntry.Reference::value).toList();
    }

    @Override
    protected BlockState getNextBlock() {
        return RandomHelper.randomOne(this.blocks).getDefaultState();
    }

    @Override
    public MutableText getInfo() {
        return super.getInfo().append(Text.literal("\nBlock Tag: %s".formatted(this.blockTag.id().toString())));
    }
}
