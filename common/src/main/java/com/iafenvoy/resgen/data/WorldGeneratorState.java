package com.iafenvoy.resgen.data;

import com.iafenvoy.resgen.ResourceGenerator;
import com.iafenvoy.resgen.data.single.GeneratorDataBase;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class WorldGeneratorState extends PersistentState {
    private static final Function<ServerWorld, Function<NbtCompound, WorldGeneratorState>> READER_CONSTRUCTOR = world -> nbt -> {
        WorldGeneratorState state = new WorldGeneratorState(world);
        state.data.clear();
        nbt.getList("data", NbtElement.COMPOUND_TYPE).stream().map(x -> (NbtCompound) x).map(GeneratorDataBase::decode).forEach(state.data::add);
        return state;
    };
    private final ServerWorld world;
    private final List<GeneratorDataBase> data = new LinkedList<>();

    public WorldGeneratorState(ServerWorld world) {
        this.world = world;
    }

    public void tick() {
        this.data.forEach(x -> x.tick(this.world));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("data", this.data.stream().map(GeneratorDataBase::encode).collect(NbtList::new, List::add, (a, b) -> {
        }));
        return nbt;
    }

    public static WorldGeneratorState getState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(READER_CONSTRUCTOR.apply(world), () -> new WorldGeneratorState(world), ResourceGenerator.MOD_ID);
    }
}
