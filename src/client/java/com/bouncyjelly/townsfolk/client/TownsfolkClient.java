package com.bouncyjelly.townsfolk.client;

import com.bouncyjelly.townsfolk.client.render.CitizenRenderer;
import com.bouncyjelly.townsfolk.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TownsfolkClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.CITIZEN, CitizenRenderer::new);
	}
}