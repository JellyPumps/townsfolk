package com.bouncyjelly.townsfolk.command;

import com.bouncyjelly.townsfolk.place.PlaceDebug;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.bouncyjelly.townsfolk.citizen.CitizenManager;
import com.bouncyjelly.townsfolk.place.Place;
import com.bouncyjelly.townsfolk.place.PlaceRegistry;
import com.bouncyjelly.townsfolk.place.PlaceType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;

public class PlaceCommands {

    public static final SuggestionProvider<ServerCommandSource> TYPE_SUGGESTIONS = (ctx, b) ->
            CommandSource.suggestMatching(Arrays.stream(PlaceType.values()).map(PlaceType::asString), b);

    public static final SuggestionProvider<ServerCommandSource> PLACE_SUGGESTIONS = (ctx, b) ->
            CommandSource.suggestMatching(
                    PlaceRegistry.get(ctx.getSource().getWorld()).getAll().stream().map(Place::id), b);

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("place")
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("type", StringArgumentType.word())
                                .suggests(TYPE_SUGGESTIONS)
                                .then(CommandManager.argument("id", StringArgumentType.word())
                                        .executes(PlaceCommands::add))))
                .then(CommandManager.literal("list").executes(PlaceCommands::list))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                                .suggests(PLACE_SUGGESTIONS)
                                .executes(PlaceCommands::remove)))
                .then(CommandManager.literal("show").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    boolean on = PlaceDebug.toggle(player.getUuid());
                    ctx.getSource().sendFeedback(() -> Text.literal("Place markers " + (on ? "on" : "off")), false);
                    return 1;
                }));
    }

    private static int add(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayerOrThrow();
        String typeName = StringArgumentType.getString(ctx, "type");
        String id = StringArgumentType.getString(ctx, "id");

        var type = PlaceType.byName(typeName);
        if (type.isEmpty()) {
            src.sendError(Text.literal("Unknown place type: " + typeName));
            return 0;
        }
        if (!id.matches("[a-z0-9_]+")) {
            src.sendError(Text.literal("Place ids may only use lowercase letters, digits and underscores."));
            return 0;
        }
        Place place = new Place(id, type.get(), player.getBlockPos(), player.getYaw());
        if (!PlaceRegistry.get(src.getWorld()).add(place)) {
            src.sendError(Text.literal("A place called '" + id + "' already exists."));
            return 0;
        }
        src.sendFeedback(() -> Text.literal("Added " + type.get().asString() + " '" + id
                + "' at " + place.pos().toShortString()), false);
        return 1;
    }

    private static int list(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        var all = PlaceRegistry.get(src.getWorld()).getAll();
        if (all.isEmpty()) {
            src.sendFeedback(() -> Text.literal("No places."), false);
            return 0;
        }
        for (Place p : all) {
            src.sendFeedback(() -> Text.literal(p.id() + "  " + p.type().asString()
                    + "  " + p.pos().toShortString()), false);
        }
        return all.size();
    }

    private static int remove(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        String id = StringArgumentType.getString(ctx, "id");
        if (!PlaceRegistry.get(src.getWorld()).remove(id)) {
            src.sendError(Text.literal("No place called '" + id + "'."));
            return 0;
        }
        int cleared = CitizenManager.get(src.getWorld()).clearPlaceReferences(id);
        src.sendFeedback(() -> Text.literal("Removed '" + id + "'"
                + (cleared > 0 ? " and cleared it from " + cleared + " citizen(s)." : ".")), false);
        return 1;
    }
}