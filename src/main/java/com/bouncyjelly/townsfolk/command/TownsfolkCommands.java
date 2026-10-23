package com.bouncyjelly.townsfolk.command;

import com.bouncyjelly.townsfolk.citizen.Citizen;
import com.bouncyjelly.townsfolk.citizen.CitizenManager;
import com.bouncyjelly.townsfolk.entity.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

public class TownsfolkCommands {

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
                .then(CommandManager.literal("list")
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            var all = CitizenManager.get(src.getWorld()).getAll();
                            if (all.isEmpty()) {
                                src.sendFeedback(() -> Text.literal("No citizens."), false);
                                return 0;
                            }
                            for (Citizen c : all) {
                                src.sendFeedback(() -> Text.literal(c.getName() + "  " + c.getId().toString().substring(0, 8)), false);
                            }
                            return all.size();
                        })));
    }
}