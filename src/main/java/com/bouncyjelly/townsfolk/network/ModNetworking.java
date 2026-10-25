package com.bouncyjelly.townsfolk.network;

import com.bouncyjelly.townsfolk.citizen.Citizen;
import com.bouncyjelly.townsfolk.citizen.CitizenManager;
import com.bouncyjelly.townsfolk.config.ConfigService;
import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import com.bouncyjelly.townsfolk.item.CitizenConfigItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;

public class ModNetworking {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenConfigPayload.ID, OpenConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ConfigResultPayload.ID, ConfigResultPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveConfigPayload.ID, SaveConfigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SaveConfigPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            // everything below is re-checked here because a client can send anything
            if (!CitizenConfigItem.canConfigure(player) || !CitizenConfigItem.isHolding(player)) return;

            ServerWorld world = player.getServerWorld();
            if (!(world.getEntityById(payload.entityId()) instanceof CitizenEntity entity)) return;
            if (player.squaredDistanceTo(entity) > 64.0 || entity.getCitizenId() == null) return;

            Citizen record = CitizenManager.get(world).getById(entity.getCitizenId());
            if (record == null) return;

            Optional<String> error = ConfigService.validate(world, record, payload.data());
            if (error.isPresent()) {
                ServerPlayNetworking.send(player, new ConfigResultPayload(false, error.get()));
                return;
            }
            ConfigService.apply(world, record, entity, payload.data());
            ServerPlayNetworking.send(player, new ConfigResultPayload(true, "Saved."));
        });
    }
}