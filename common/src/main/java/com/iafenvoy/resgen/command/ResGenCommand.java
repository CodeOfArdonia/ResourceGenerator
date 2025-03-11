package com.iafenvoy.resgen.command;

import com.iafenvoy.resgen.data.WorldGeneratorState;
import com.iafenvoy.resgen.data.single.*;
import com.iafenvoy.resgen.util.ParticleUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ResGenCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access) {
        dispatcher.register(literal("resgen")
                .requires(src -> !src.getServer().isDedicated() || src.hasPermissionLevel(2))
                .then(argument("pos", Vec3ArgumentType.vec3())
                        .then(literal("add")
                                .then(argument("interval", IntegerArgumentType.integer(0))
                                        .then(literal("item").then(argument("stack", ItemStackArgumentType.itemStack(access))
                                                .then(argument("count", IntegerArgumentType.integer(1))
                                                        .executes(ResGenCommand::addItem)
                                                )))
                                        .then(literal("itemTag").then(argument("tag", IdentifierArgumentType.identifier())
                                                .suggests(ResGenSuggestions.ITEM_TAG)
                                                .executes(ctx -> add(ctx, pos -> new ItemTagGeneratorData(pos, IdentifierArgumentType.getIdentifier(ctx, "tag"))))
                                        ))
                                        .then(literal("lootTable").then(argument("id", IdentifierArgumentType.identifier())
                                                .suggests(ResGenSuggestions.LOOT_TABLE)
                                                .executes(ctx -> add(ctx, pos -> new ItemLootTableGeneratorData(pos, IdentifierArgumentType.getIdentifier(ctx, "id"))))
                                        ))
                                        .then(literal("block").then(argument("block_state", BlockStateArgumentType.blockState(access))
                                                .executes(ResGenCommand::addBlock)
                                        ))
                                        .then(literal("blockTag").then(argument("tag", IdentifierArgumentType.identifier())
                                                .suggests(ResGenSuggestions.BLOCK_TAG)
                                                .executes(ctx -> add(ctx, pos -> new BlockTagGeneratorData(pos, IdentifierArgumentType.getIdentifier(ctx, "tag"))))
                                        ))))
                        .then(literal("remove").executes(ResGenCommand::remove))
                        .then(literal("info").executes(ResGenCommand::info))
                        .then(literal("list")
                                .then(literal("chunk").executes(ctx -> list(ctx, 0)))
                                .then(literal("range")
                                        .then(argument("range", DoubleArgumentType.doubleArg(1))
                                                .executes(ctx -> list(ctx, DoubleArgumentType.getDouble(ctx, "range")))
                                        )))
                        .then(literal("highlight")
                                .then(literal("chunk").executes(ctx -> highlight(ctx, 0)))
                                .then(literal("range")
                                        .then(argument("range", DoubleArgumentType.doubleArg(1))
                                                .executes(ctx -> highlight(ctx, DoubleArgumentType.getDouble(ctx, "range")))
                                        )))
                ));
    }

    private static int addItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int count = IntegerArgumentType.getInteger(context, "count");
        ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "stack").createStack(count, false);
        return add(context, pos -> new SingleItemGeneratorData(pos, stack));
    }

    private static int addBlock(CommandContext<ServerCommandSource> context) {
        BlockState state = BlockStateArgumentType.getBlockState(context, "block_state").getBlockState();
        return add(context, pos -> new SingleBlockGeneratorData(pos, state));
    }

    private static int add(CommandContext<ServerCommandSource> context, Function<BlockPos, GeneratorDataBase> constructor) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = Vec3ArgumentType.getPosArgument(context, "pos").toAbsoluteBlockPos(source);
        GeneratorDataBase data = constructor.apply(pos);
        data.setInterval(IntegerArgumentType.getInteger(context, "interval"));
        if (getState(world).add(data)) {
            ParticleUtils.highlight(world, pos);
            source.sendMessage(Text.literal("Success"));
            return 1;
        } else
            source.sendError(Text.literal("Duplicated!"));
        return 0;
    }

    private static int remove(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = Vec3ArgumentType.getPosArgument(context, "pos").toAbsoluteBlockPos(source);
        Optional<GeneratorDataBase> optional = getState(world).remove(pos);
        if (optional.isPresent()) {
            source.sendMessage(Text.literal("Success"));
            return 1;
        } else
            source.sendError(Text.literal("Not Found!"));
        return 0;
    }

    private static int info(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = Vec3ArgumentType.getPosArgument(context, "pos").toAbsoluteBlockPos(source);
        Optional<GeneratorDataBase> optional = getState(world).get(pos);
        if (optional.isPresent()) {
            ParticleUtils.highlight(world, pos);
            source.sendMessage(optional.get().getInfo());
            return 1;
        } else
            source.sendError(Text.literal("This is not a generator!"));
        return 0;
    }

    private static int list(CommandContext<ServerCommandSource> context, double range) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = Vec3ArgumentType.getPosArgument(context, "pos").toAbsoluteBlockPos(source);
        WorldGeneratorState state = getState(world);
        List<GeneratorDataBase> data = range == 0 ? state.getInChunk(pos) : state.getInRange(Vec3d.of(pos), range);
        if (data.isEmpty()) source.sendError(Text.literal("No generator found!"));
        else
            source.sendMessage(Text.literal(data.stream().map(x -> "[x=%d,y=%d,z=%d] %s".formatted(x.getPos().getX(), x.getPos().getY(), x.getPos().getZ(), x.getType().toString())).collect(Collectors.joining("\n"))));
        return data.size();
    }

    private static int highlight(CommandContext<ServerCommandSource> context, double range) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = Vec3ArgumentType.getPosArgument(context, "pos").toAbsoluteBlockPos(source);
        WorldGeneratorState state = getState(world);
        List<GeneratorDataBase> data = range == 0 ? state.getInChunk(pos) : state.getInRange(Vec3d.of(pos), range);
        if (data.isEmpty()) source.sendError(Text.literal("No generator found!"));
        else {
            data.stream().map(GeneratorDataBase::getPos).forEach(x -> ParticleUtils.highlight(world, x));
            source.sendMessage(Text.literal("Successfully highlight %s generator".formatted(data.size())));
        }
        return data.size();
    }

    private static WorldGeneratorState getState(ServerWorld world) {
        return WorldGeneratorState.getState(world);
    }
}
