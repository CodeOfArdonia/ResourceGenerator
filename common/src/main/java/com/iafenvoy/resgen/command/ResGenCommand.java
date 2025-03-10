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
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ResGenCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access) {
        dispatcher.register(literal("resgen")
                .requires(ServerCommandSource::isExecutedByPlayer)
                .requires(src -> !src.getServer().isDedicated() || src.hasPermissionLevel(src.getServer().getOpPermissionLevel()))
                .then(literal("add")
                        .then(argument("interval", IntegerArgumentType.integer(0))
                                .then(literal("item")
                                        .then(argument("stack", ItemStackArgumentType.itemStack(access))
                                                .then(argument("count", IntegerArgumentType.integer(1))
                                                        .executes(ResGenCommand::addItem)
                                                )))
                                .then(literal("itemTag")
                                        .then(argument("tag", IdentifierArgumentType.identifier())
                                                .suggests(ResGenSuggestions.ITEM_TAG)
                                                .executes(ctx -> add(ctx, pos -> new ItemTagGeneratorData(pos, IdentifierArgumentType.getIdentifier(ctx, "tag"))))
                                        ))
                                .then(literal("lootTable")
                                        .then(argument("id", IdentifierArgumentType.identifier())
                                                .suggests(ResGenSuggestions.LOOT_TABLE)
                                                .executes(ctx -> add(ctx, pos -> new ItemLootTableGeneratorData(pos, IdentifierArgumentType.getIdentifier(ctx, "id"))))
                                        ))
                                .then(literal("block")
                                        .then(argument("block_state", BlockStateArgumentType.blockState(access))
                                                .executes(ResGenCommand::addBlock)
                                        ))
                                .then(literal("blockTag")
                                        .then(argument("tag", IdentifierArgumentType.identifier())
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
        );
    }

    private static int addItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int count = IntegerArgumentType.getInteger(context, "count");
        ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "stack").createStack(count, false);
        return add(context, pos -> new SingleItemGeneratorData(pos, stack));
    }

    private static int addBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockState state = BlockStateArgumentType.getBlockState(context, "block_state").getBlockState();
        return add(context, pos -> new SingleBlockGeneratorData(pos, state));
    }

    private static int add(CommandContext<ServerCommandSource> context, Function<BlockPos, GeneratorDataBase> constructor) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (raycast(player) instanceof BlockHitResult result) {
            GeneratorDataBase data = constructor.apply(result.getBlockPos());
            data.setInterval(IntegerArgumentType.getInteger(context, "interval"));
            if (getState(player).add(data)) {
                ParticleUtils.highlight(player.getServerWorld(), result.getBlockPos());
                source.sendMessage(Text.literal("Success"));
                return 1;
            } else
                source.sendError(Text.literal("Duplicated!"));
        } else
            source.sendError(Text.literal("You should look at a block"));
        return 0;
    }

    private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (raycast(player) instanceof BlockHitResult result) {
            Optional<GeneratorDataBase> optional = getState(player).remove(result.getBlockPos());
            if (optional.isPresent()) {
                source.sendMessage(Text.literal("Success"));
                return 1;
            } else
                source.sendError(Text.literal("Not Found!"));
        } else
            source.sendError(Text.literal("You should look at a block"));
        return 0;
    }

    private static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        if (raycast(player) instanceof BlockHitResult result) {
            Optional<GeneratorDataBase> optional = getState(player).get(result.getBlockPos());
            if (optional.isPresent()) {
                ParticleUtils.highlight(player.getServerWorld(), result.getBlockPos());
                source.sendMessage(optional.get().getInfo());
                return 1;
            } else
                source.sendError(Text.literal("This is not a generator!"));
        } else
            source.sendError(Text.literal("You should look at a block"));
        return 0;
    }

    private static int list(CommandContext<ServerCommandSource> context, double range) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        WorldGeneratorState state = getState(player);
        List<GeneratorDataBase> data = range == 0 ? state.getInChunk(player.getBlockPos()) : state.getInRange(player.getPos(), range);
        if (data.isEmpty()) source.sendError(Text.literal("No generator found!"));
        else
            source.sendMessage(Text.literal(data.stream().map(x -> "[x=%d,y=%d,z=%d] %s".formatted(x.getPos().getX(), x.getPos().getY(), x.getPos().getZ(), x.getType().toString())).collect(Collectors.joining("\n"))));
        return data.size();
    }

    private static int highlight(CommandContext<ServerCommandSource> context, double range) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        WorldGeneratorState state = getState(player);
        List<GeneratorDataBase> data = range == 0 ? state.getInChunk(player.getBlockPos()) : state.getInRange(player.getPos(), range);
        if (data.isEmpty()) source.sendError(Text.literal("No generator found!"));
        else {
            data.stream().map(GeneratorDataBase::getPos).forEach(x -> ParticleUtils.highlight(player.getServerWorld(), x));
            source.sendMessage(Text.literal("Successfully highlight %s generator".formatted(data.size())));
        }
        return data.size();
    }

    @Nullable
    private static HitResult raycast(ServerPlayerEntity player) {
        return player.raycast(5, 0, false);
    }

    private static WorldGeneratorState getState(ServerPlayerEntity player) {
        return WorldGeneratorState.getState(player.getServerWorld());
    }
}
