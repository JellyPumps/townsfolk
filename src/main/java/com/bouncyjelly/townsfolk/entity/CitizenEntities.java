package com.bouncyjelly.townsfolk.entity;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CitizenEntities {
    private CitizenEntities() {}

    public static List<CitizenEntity> loaded(MinecraftServer server) {
        List<CitizenEntity> result = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            result.addAll(world.getEntitiesByType(ModEntities.CITIZEN, e -> true));
        }
        return result;
    }

    public static int discardFor(MinecraftServer server, UUID citizen_id) {
        int count = 0;
        for (CitizenEntity e : loaded(server)) {
            if (citizen_id.equals(e.getCitizenId())) {
                e.discard();
                count++;
            }
        }
        return count;
    }
}
