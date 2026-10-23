package com.bouncyjelly.townsfolk.place;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlaceDebug {
    private static final Set<UUID> VIEWERS = new HashSet<>();
    private static final double RANGE_SQ = 48 * 48;

    /** @return true if now enabled */
    public static boolean toggle(UUID player_id) {
        if (!VIEWERS.add(player_id)) {
            VIEWERS.remove(player_id);
            return false;
        }
        return true;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 10 != 0 || VIEWERS.isEmpty()) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!VIEWERS.contains(player.getUuid())) continue;
                ServerWorld world = player.getServerWorld();
                for (Place p : PlaceRegistry.get(world).getAll()) {
                    if (p.pos().getSquaredDistance(player.getPos()) > RANGE_SQ) continue;
                    world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                            p.pos().getX() + 0.5, p.pos().getY() + 1.0, p.pos().getZ() + 0.5,
                            2, 0.1, 0.6, 0.1, 0.0);
                }
            }
        });
    }
}
