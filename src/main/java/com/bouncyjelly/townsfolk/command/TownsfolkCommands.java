package com.bouncyjelly.townsfolk.command;

import com.mojang.brigadier.CommandDispatcher;
import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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
                                }))));
    }
}