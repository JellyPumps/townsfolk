package com.bouncyjelly.townsfolk.client;

import com.bouncyjelly.townsfolk.client.render.CitizenRenderer;
import com.bouncyjelly.townsfolk.entity.ModEntities;
import com.bouncyjelly.townsfolk.network.ConfigResultPayload;
import com.bouncyjelly.townsfolk.network.OpenConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TownsfolkClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.CITIZEN, CitizenRenderer::new);
		ClientPlayNetworking.registerGlobalReceiver(OpenConfigPayload.ID, (payload, ctx) ->
				ctx.client().setScreen(new CitizenConfigScreen(payload.entityId(), payload.data(), payload.context())));

		ClientPlayNetworking.registerGlobalReceiver(ConfigResultPayload.ID, (payload, ctx) -> {
			if (ctx.client().currentScreen instanceof CitizenConfigScreen s) s.onResult(payload.ok(), payload.message());
		});
	}
}