package com.bouncyjelly.townsfolk.command;

import com.bouncyjelly.townsfolk.citizen.Citizen;
import com.bouncyjelly.townsfolk.citizen.CitizenManager;
import com.bouncyjelly.townsfolk.entity.CitizenEntities;
import com.bouncyjelly.townsfolk.entity.ModEntities;
import com.bouncyjelly.townsfolk.place.Place;
import com.bouncyjelly.townsfolk.place.PlaceRegistry;
import com.bouncyjelly.townsfolk.place.PlaceType;
import com.bouncyjelly.townsfolk.schedule.ScheduleResolver;
import com.mojang.brigadier.CommandDispatcher;
import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TownsfolkCommands {

    static final SuggestionProvider<ServerCommandSource> CITIZEN_SUGGESTIONS = (ctx, b) ->
            CommandSource.suggestMatching(
                    CitizenManager.get(ctx.getSource().getWorld()).getAll().stream().map(Citizen::getName), b);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("townsfolk")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("walk")
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    ServerPlayerEntity player = src.getPlayerOrThrow();
                                    BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");

                                    var nearest = src.getWorld()
                                            .getEntitiesByClass(CitizenEntity.class,
                                                    player.getBoundingBox().expand(32), e -> true)
                                            .stream()
                                            .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)));

                                    if (nearest.isEmpty()) {
                                        src.sendError(Text.literal("No citizen within 32 blocks."));
                                        return 0;
                                    }
                                    nearest.get().setWalkTarget(pos);
                                    src.sendFeedback(() -> Text.literal("Citizen walking to " + pos.toShortString()), false);
                                    return 1;
                                })))
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    ServerPlayerEntity player = src.getPlayerOrThrow();
                                    ServerWorld world = src.getWorld();
                                    String name = StringArgumentType.getString(ctx, "name");
                                    if (CitizenManager.get(world).getByName(name) != null) {
                                        src.sendError(Text.literal("A citizen called '" + name + "' already exists."));
                                        return 0;
                                    }

                                    CitizenEntity entity = ModEntities.CITIZEN.create(world);
                                    if (entity == null) {
                                        src.sendError(Text.literal("Could not create citizen entity."));
                                        return 0;
                                    }
                                    Citizen citizen = CitizenManager.get(world).create(name);
                                    entity.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0);
                                    entity.bindToCitizen(citizen);
                                    world.spawnEntity(entity);

                                    src.sendFeedback(() -> Text.literal("Created citizen " + name), false);
                                    return 1;
                                })))
                .then(CommandManager.literal("list").executes(ctx -> {
                    ServerCommandSource src = ctx.getSource();
                        var all = CitizenManager.get(src.getWorld()).getAll();
                        var loadedEntities = CitizenEntities.loaded(src.getServer());
                        Set<UUID> loadedIds = loadedEntities.stream()
                                .map(CitizenEntity::getCitizenId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
                        long unlinked = loadedEntities.stream().filter(e -> e.getCitizenId() == null).count();

                        src.sendFeedback(() -> Text.literal("Citizens: " + all.size()
                                + "  (loaded entities: " + loadedEntities.size()
                                + ", unlinked: " + unlinked + ")"), false);
                        for (Citizen c : all) {
                            src.sendFeedback(() -> Text.literal(c.getName()
                                    + "  " + c.getId().toString().substring(0, 8)
                                    + "  home=" + c.getHomeId()
                                    + "  work=" + c.getWorkId()
                                    + "  " + (loadedIds.contains(c.getId()) ? "loaded" : "unloaded")), false);
                        }
                        return all.size();
                    }))
                .then(CommandManager.literal("assign")
                        .then(CommandManager.argument("citizen", StringArgumentType.string())
                                .suggests((ctx, b) -> CommandSource.suggestMatching(
                                        CitizenManager.get(ctx.getSource().getWorld()).getAll().stream().map(Citizen::getName), b))
                                .then(CommandManager.argument("slot", StringArgumentType.word())
                                        .suggests((ctx, b) -> CommandSource.suggestMatching(java.util.stream.Stream.of("home", "work"), b))
                                        .then(CommandManager.argument("place", StringArgumentType.word())
                                                .suggests(PlaceCommands.PLACE_SUGGESTIONS)
                                                .executes(ctx -> {
                                                    ServerCommandSource src = ctx.getSource();
                                                    CitizenManager citizens = CitizenManager.get(src.getWorld());
                                                    PlaceRegistry places = PlaceRegistry.get(src.getWorld());

                                                    Citizen citizen = citizens.getByName(StringArgumentType.getString(ctx, "citizen"));
                                                    if (citizen == null) {
                                                        src.sendError(Text.literal("No citizen with that name."));
                                                        return 0;
                                                    }
                                                    Place place = places.get(StringArgumentType.getString(ctx, "place"));
                                                    if (place == null) {
                                                        src.sendError(Text.literal("No place with that id."));
                                                        return 0;
                                                    }
                                                    String slot = StringArgumentType.getString(ctx, "slot");
                                                    if (slot.equals("home")) {
                                                        if (place.type() != PlaceType.HOME) {
                                                            src.sendError(Text.literal("That place is not a home."));
                                                            return 0;
                                                        }
                                                        citizens.assignHome(citizen, place.id());
                                                    } else if (slot.equals("work")) {
                                                        if (place.type() != PlaceType.WORKPLACE && place.type() != PlaceType.SHOP_COUNTER) {
                                                            src.sendError(Text.literal("That place is not a workplace or shop counter."));
                                                            return 0;
                                                        }
                                                        citizens.assignWork(citizen, place.id());
                                                    } else {
                                                        src.sendError(Text.literal("Slot must be 'home' or 'work'."));
                                                        return 0;
                                                    }
                                                    src.sendFeedback(() -> Text.literal(citizen.getName() + " " + slot
                                                            + " -> " + place.id()), false);
                                                    return 1;
                                                })))))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("citizen", StringArgumentType.string())
                                .suggests(CITIZEN_SUGGESTIONS)
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    CitizenManager manager = CitizenManager.get(src.getWorld());
                                    Citizen citizen = manager.getByName(StringArgumentType.getString(ctx, "citizen"));
                                    if (citizen == null) {
                                        src.sendError(Text.literal("No citizen with that name."));
                                        return 0;
                                    }
                                    manager.remove(citizen.getId());
                                    int entities = CitizenEntities.discardFor(src.getServer(), citizen.getId());
                                    src.sendFeedback(() -> Text.literal("Removed " + citizen.getName()
                                            + " (" + entities + " loaded entity removed)."), false);
                                    return 1;
                                })))
                .then(CommandManager.literal("clear")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(() -> Text.literal(
                                    "This removes ALL citizens and citizen entities. Places are kept. "
                                            + "Run '/townsfolk clear confirm' to proceed."), false);
                            return 0;
                        })
                        .then(CommandManager.literal("confirm").executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            int records = CitizenManager.get(src.getWorld()).removeAll();
                            int entities = 0;
                            for (CitizenEntity e : CitizenEntities.loaded(src.getServer())) {
                                e.discard();
                                entities++;
                            }
                            int removedEntities = entities;
                            src.sendFeedback(() -> Text.literal("Removed " + records + " citizen(s) and "
                                    + removedEntities + " loaded entity(ies)."), false);
                            return records;
                        })))
                .then(CommandManager.literal("schedule")
                        .then(CommandManager.argument("citizen", StringArgumentType.string())
                                .suggests(CITIZEN_SUGGESTIONS)
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    ServerWorld world = src.getWorld();
                                    Citizen citizen = CitizenManager.get(world)
                                            .getByName(StringArgumentType.getString(ctx, "citizen"));
                                    if (citizen == null) {
                                        src.sendError(Text.literal("No citizen with that name."));
                                        return 0;
                                    }
                                    long tick = world.getTimeOfDay() % 24000L;
                                    ScheduleResolver.Destination d = ScheduleResolver.resolve(world, citizen);
                                    if (d == null) {
                                        src.sendFeedback(() -> Text.literal("Tick " + tick + ": no schedule entry, stays put."), false);
                                    } else if (d.place() == null) {
                                        src.sendFeedback(() -> Text.literal("Tick " + tick + ": " + d.activity().asString()
                                                + ", but no place is available."), false);
                                    } else {
                                        src.sendFeedback(() -> Text.literal("Tick " + tick + ": " + d.activity().asString()
                                                + " at " + d.place().id() + " " + d.place().pos().toShortString()), false);
                                    }
                                    return 1;
                                })))
                .then(PlaceCommands.build()));
    }
}