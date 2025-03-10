package com.iafenvoy.resgen.data;

import com.iafenvoy.resgen.ResourceGenerator;
import com.iafenvoy.resgen.data.single.GeneratorDataBase;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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

    public static WorldGeneratorState getState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(READER_CONSTRUCTOR.apply(world), () -> new WorldGeneratorState(world), ResourceGenerator.MOD_ID);
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

    @Override
    public boolean isDirty() {
        return true;
    }

    public Optional<GeneratorDataBase> get(BlockPos pos) {
        return this.data.stream().filter(x -> x.getPos().equals(pos)).findFirst();
    }

    public boolean add(GeneratorDataBase data) {
        if (this.get(data.getPos()).isPresent()) return false;//Disallow duplicate
        this.data.add(data);
        return true;
    }

    public Optional<GeneratorDataBase> remove(BlockPos pos) {
        Optional<GeneratorDataBase> optional = this.get(pos);
        optional.ifPresent(this.data::remove);
        return optional;
    }

    public List<GeneratorDataBase> getInChunk(BlockPos pos) {
        final ChunkPos chunkPos = new ChunkPos(pos);
        return this.data.stream().filter(x -> isInChunk(chunkPos, x.getPos())).toList();
    }

    public List<GeneratorDataBase> getInRange(Vec3d pos, double range) {
        final double rangeSqr = range * range;
        return this.data.stream().filter(x -> pos.squaredDistanceTo(Vec3d.of(x.getPos())) <= rangeSqr).toList();
    }

    private static boolean isInChunk(ChunkPos chunkPos, BlockPos blockPos) {
        return chunkPos.getStartX() <= blockPos.getX() || blockPos.getX() <= chunkPos.getEndX() ||
                chunkPos.getStartZ() <= blockPos.getZ() || blockPos.getZ() <= chunkPos.getEndZ();
    }
}
