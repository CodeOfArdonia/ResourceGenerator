package com.iafenvoy.resgen.command;

import com.iafenvoy.resgen.ResourceGenerator;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.loot.LootDataType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.List;

public final class ResGenSuggestions {
    public static final SuggestionProvider<ServerCommandSource> ITEM_TAG = SuggestionProviders.register(Identifier.of(ResourceGenerator.MOD_ID, "item_tag"), (context, builder) -> CommandSource.suggestIdentifiers(Registries.ITEM.streamTags().map(TagKey::id), builder));
    public static final SuggestionProvider<ServerCommandSource> BLOCK_TAG = SuggestionProviders.register(Identifier.of(ResourceGenerator.MOD_ID, "block_tag"), (context, builder) -> CommandSource.suggestIdentifiers(Registries.BLOCK.streamTags().map(TagKey::id), builder));
    public static final SuggestionProvider<ServerCommandSource> LOOT_TABLE = SuggestionProviders.register(Identifier.of(ResourceGenerator.MOD_ID, "loot_table"), (context, builder) -> CommandSource.suggestIdentifiers(context.getSource() instanceof ServerCommandSource source ? source.getServer().getLootManager().getIds(LootDataType.LOOT_TABLES) : List.of(), builder));
}
